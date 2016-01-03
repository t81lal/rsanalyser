package org.nullbool.api;

import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.obfuscation.*;
import org.nullbool.api.obfuscation.OpaquePredicateRemover.Opaque;
import org.nullbool.api.obfuscation.cfg.CFGCache;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.SaneControlFlowGraph;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.number.MultiplierVisitor;
import org.nullbool.api.obfuscation.refactor.*;
import org.nullbool.api.obfuscation.stuffthatdoesntwork.MultiplicativeModifierDestroyer;
import org.nullbool.api.output.APIGenerator;
import org.nullbool.api.output.NewOutputLogger;
import org.nullbool.api.rs.CaseAnalyser;
import org.nullbool.api.util.InstructionIdentifier;
import org.nullbool.api.util.NodedContainer;
import org.nullbool.api.util.map.ValueCreator;
import org.nullbool.pi.core.hook.api.*;
import org.nullbool.pi.core.hook.serimpl.StaticMapSerialiserImpl;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.out.CompleteJarDumper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings(value = {"all"})
public abstract class AbstractAnalysisProvider {

    private final Revision revision;
    private final LocateableJarContents<ClassNode> contents;
    private final String[] instructions;
    private final Map<String, Boolean> flags;
    private long startTime;
    private long deobTime;
    private long analysisTime;
    private ClassTree classTree;
    private List<ClassAnalyser> analysers;
    private MultiplierHandler multiplierHandler;
    private OpaquePredicateRemover opaqueRemover;
    private EmptyParameterFixer emptyParameterFixer;
    private CFGCache cfgCache;
    private TreeBuilder builder;
    private CaseAnalyser caseAnalyser;
    private ReverseMethodDescCache methodCache;

    private volatile boolean haltRequested;

    public AbstractAnalysisProvider(Revision revision) throws IOException {
        this.revision = revision;
        contents = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(revision.parse().values()), null, null);
        instructions = getAllInstructions();
        flags = new HashMap<String, Boolean>();
    }

    public void run() throws AnalysisException {
        startTime = System.currentTimeMillis();
        classTree = new ClassTree(contents.getClassContents());
        classTree.output();

        methodCache = new ReverseMethodDescCache(contents.getClassContents());

        multiplierHandler = new MultiplierHandler();
        builder = new TreeBuilder();
        deobfuscate();

        deobTime = System.currentTimeMillis() - startTime;

        if (!flags.getOrDefault("nodump", false)) {
            dumpDeob();
        }

        long now = System.currentTimeMillis();

        if (!flags.getOrDefault("justdeob", false)) {
            analysers = registerAnalysers().asList();
            if (analysers != null && analysers.size() != 0)
                analyse();

            if (haltRequested)
                return;

            analysisTime = System.currentTimeMillis() - now;

            try {
                output();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Context.unbind();
    }

    private void output() {
        HookMap hookMap = NewOutputLogger.output();

        for (ClassHook h : hookMap.classes()) {
            for (MethodHook m : h.methods()) {
                if (m.insns() != null)
                    m.insns().reset();
            }
        }

        if (flags.getOrDefault("basicout", true)) {
            APIGenerator.createAPI(hookMap);
            writeLog(hookMap);
        }

        if (!flags.getOrDefault("nodump", false)) {
            dumpJar(hookMap);
        }
    }

    private void writeLog(HookMap map) {
        try {
            File folder = new File("out/" + getRevision().getName() + "/");
            if (folder.exists())
                folder.delete();
            folder.mkdirs();
            File logFile = new File(folder, "log.ser");
            FileOutputStream fos = new FileOutputStream(logFile);
            // write content header type
            fos.write("content-type=bser\n".getBytes());

            MethodCache cache = new MethodCache(contents.getClassContents());
            for (ClassHook ch : map.classes()) {
                for (MethodHook mh : ch.methods()) {
                    // TODO: Get after empty param deob
                    MethodNode m = cache.get(mh.val(Constants.REAL_OWNER), mh.obfuscated(), mh.val(Constants.REFACTORED_DESC));

                    if (m == null) {
                        System.out.println("NULL HOOK CALL?");
                        System.out.println(mh.baseToString());
                        continue;
                    }

                    Opaque op = opaqueRemover.find(m);
                    int num = 0;
                    if (op != null) {
                        num = op.getNum();
                        switch (op.getOpcode()) {
                            case Opcodes.IF_ICMPLE:
                            case Opcodes.IF_ICMPLT:
                                num -= 1;
                                break;
                            // IF_ICMPNE can be any number other than the num
                            case Opcodes.IF_ICMPNE:
                            case Opcodes.IF_ICMPGE:
                            case Opcodes.IF_ICMPGT:
                                num += 1;
                                break;
                            case Opcodes.IF_ICMPEQ:
                                // no change.
                                break;
                        }
                        mh.var(Constants.HAS_OPAQUE, "true");
                        mh.var(Constants.SAFE_OPAQUE, Integer.toString(num));
                    } else {
                        if (emptyParameterFixer.getChanged().contains(m)) {
                            // unused param.
                            mh.var(Constants.HAS_OPAQUE, "true");
                            mh.var(Constants.SAFE_OPAQUE, "0");
                        }
                    }
                }
            }

            new StaticMapSerialiserImpl().serialise(map, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dumpJar(HookMap hookMap) {
        Map<String, ClassHook> classes = new HashMap<String, ClassHook>();
        Map<String, FieldHook> fields = new HashMap<String, FieldHook>();
        Map<String, MethodHook> methods = new HashMap<String, MethodHook>();

        for (ClassHook h : hookMap.classes()) {
            classes.put(h.obfuscated(), h);
            for (FieldHook f : h.fields()) {
                fields.put(f.val(Constants.REAL_OWNER) + "." + f.obfuscated() + " " + f.val(Constants.DESC), f);
            }

            for (MethodHook m : h.methods()) {
                methods.put(m.val(Constants.REAL_OWNER) + "." + m.obfuscated() + m.val(Constants.DESC), m);
            }
        }


        JarContents<ClassNode> contents = new JarContents<ClassNode>();
        contents.getClassContents().addAll(getClassNodes().values());
        Map<String, ClassNode> nodes = contents.getClassContents().namedMap();
        MethodCache cache = new MethodCache(contents.getClassContents());

        IRemapper remapper = new IRemapper() {
            @Override
            public String resolveMethodName(String owner, String name, String desc, boolean isStatic) {
                if (true)
                    return name;

                if (name.length() > 2)
                    return name;

                if (owner.lastIndexOf('/') == -1 && isStatic) {
                    ClassNode cn = nodes.get(owner);
                    if (cn != null) {
                        Set<MethodNode> matches = new HashSet<MethodNode>();

                        MethodNode mn = null;
                        for (MethodNode m : cn.methods) {
                            if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                    mn = m;
                                } else {
                                    matches.add(m);
                                }
                            }
                        }

                        for (ClassNode _cn : classTree.getSupers(cn)) {
                            for (MethodNode m : _cn.methods) {
                                if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                    if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                        mn = m;
                                    } else {
                                        matches.add(m);
                                    }
                                }
                            }
                        }
                        for (ClassNode _cn : classTree.getDelegates(cn)) {
                            for (MethodNode m : _cn.methods) {
                                if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                    if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                        mn = m;
                                    } else {
                                        matches.add(m);
                                    }
                                }
                            }
                        }

                        if (mn == null)
                            throw new RuntimeException(String.format("%s.%s %s : ", owner, name, desc) + matches.toString());

                        Type ret = Type.getReturnType(mn.desc);
                        for (MethodNode m : matches) {
                            Type ret1 = Type.getReturnType(m.desc);
                            if (!ret.getDescriptor().equals(ret1.getDescriptor())) {
                                // System.out.println(String.format("Renaming %s.%s %s (%b) : ", owner, name, desc, Modifier.isStatic(mn.access)) + matches.toString());
                                return "m_" + cn.methods.indexOf(mn);
                            }
                        }
                    }
                }

                if (name.equals("if") || name.equals("do")) {
                    MethodNode m = cache.get(owner, name, desc);
                    if (Modifier.isStatic(m.access)) {
                        // System.out.println(m.key() + " is static.");
                    } else {
                        // System.out.println(m.key() + " isn't static.");
                    }
                    return "m1_" + name;
                }

                return name;
            }

            @Override
            public String resolveFieldName(String owner, String name, String desc, boolean isStatic) {
                String key = owner + "." + name + " " + desc;
                if (fields.containsKey(key)) {
                    return fields.get(key).refactored();
                }

                if (name.equals("do") || name.equals("if")) {
                    return "f_" + name;
                }
                //let the refactorer do it's own thang if we can't quick-find it
                //  ie. it will do a deep search.
                return null;
            }

            @Override
            public String resolveClassName(String owner) {
                ClassHook ref = classes.get(owner);
                if (ref != null)
                    return "rs/" + ref.refactored();

                if (owner.indexOf('/') == -1) {
                    if (owner.equals("do") || owner.equals("if")) {
                        owner = "klass_" + owner;
                    }

                    return "rs/" + owner;
                } else {
                    return owner;
                }
            }
        };

        BytecodeRefactorer refactorer = new BytecodeRefactorer((Collection<ClassNode>) contents.getClassContents(), remapper);
        refactorer.start();

        //TODO: reorder
        if (flags.getOrDefault("reorderfields", true))
            reorderFields();

        CompleteJarDumper dumper = new CompleteJarDumper(contents);
        String name = getRevision().getName();
        File file = new File("out/" + name + "/refactor" + name + ".jar");
        if (file.exists())
            file.delete();
        file.mkdirs();
        try {
            dumper.dump(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpDeob() {
        JarContents<ClassNode> contents = new JarContents<ClassNode>();
        contents.getClassContents().addAll(getClassNodes().values());
        Map<String, ClassNode> nodes = contents.getClassContents().namedMap();
        MethodCache cache = new MethodCache(contents.getClassContents());

        if (flags.getOrDefault("justdeob", false)) {
            IRemapper remapper = new IRemapper() {
                @Override
                public String resolveMethodName(String owner, String name, String desc, boolean isStatic) {
                    if (name.length() > 2)
                        return name;

                    if (owner.lastIndexOf('/') == -1 && isStatic) {
                        ClassNode cn = nodes.get(owner);
                        if (cn != null) {
                            Set<MethodNode> matches = new HashSet<MethodNode>();

                            MethodNode mn = null;
                            for (MethodNode m : cn.methods) {
                                if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                    if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                        mn = m;
                                    } else {
                                        matches.add(m);
                                    }
                                }
                            }

                            for (ClassNode _cn : classTree.getSupers(cn)) {
                                for (MethodNode m : _cn.methods) {
                                    if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                        if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                            mn = m;
                                        } else {
                                            matches.add(m);
                                        }
                                    }
                                }
                            }
                            for (ClassNode _cn : classTree.getDelegates(cn)) {
                                for (MethodNode m : _cn.methods) {
                                    if (m.name.equals(name) && paramsMatch(desc, m.desc)) {
                                        if (m.desc.equals(desc) && mn == null && Modifier.isStatic(m.access) == isStatic) {
                                            mn = m;
                                        } else {
                                            matches.add(m);
                                        }
                                    }
                                }
                            }

                            if (mn == null)
                                throw new RuntimeException(String.format("%s.%s %s : ", owner, name, desc) + matches.toString());

                            Type ret = Type.getReturnType(mn.desc);
                            for (MethodNode m : matches) {
                                Type ret1 = Type.getReturnType(m.desc);
                                if (!ret.getDescriptor().equals(ret1.getDescriptor())) {
                                    // System.out.println(String.format("Renaming %s.%s %s (%b) : ", owner, name, desc, Modifier.isStatic(mn.access)) + matches.toString());
                                    return "m_" + cn.methods.indexOf(mn);
                                }
                            }
                        }
                    }

                    if (name.equals("if") || name.equals("do")) {
                        MethodNode m = cache.get(owner, name, desc);
                        if (Modifier.isStatic(m.access)) {
                            // System.out.println(m.key() + " is static.");
                        } else {
                            // System.out.println(m.key() + " isn't static.");
                        }
                        return "m1_" + name;
                    }

                    return name;
                }

                @Override
                public String resolveFieldName(String owner, String name, String desc, boolean isStatic) {
                    if (name.equals("do") || name.equals("if")) {
                        return "f_" + name;
                    }
                    //let the refactorer do it's own thang if we can't quick-find it
                    //  ie. it will do a deep search.
                    return null;
                }

                @Override
                public String resolveClassName(String owner) {
                    if (owner.indexOf('/') == -1) {
                        if (owner.equals("do") || owner.equals("if")) {
                            owner = "klass_" + owner;
                        }

                        return "rs/" + owner;
                    } else {
                        return owner;
                    }
                }
            };

            BytecodeRefactorer refactorer = new BytecodeRefactorer(contents.getClassContents(), remapper);
            refactorer.start();

            //			IRemapper rm = new IRemapper() {
            //				@Override
            //				public String resolveMethodName(String owner, String name, String desc, boolean isStatic) {
            //					if(name.equals("<init>") || name.equals("<clinit>"))
            //						return name;
            //
            //					if(owner.indexOf('/') == -1) {
            //						return "_" + name;
            //					}
            //					return name;
            //				}
            //
            //				@Override
            //				public String resolveFieldName(String owner, String name, String desc) {
            //					if(owner.indexOf('/') == -1) {
            //						return "_" + name;
            //					}
            //					return name;
            //				}
            //
            //				@Override
            //				public String resolveClassName(String oldName) {
            //					if(oldName.indexOf('/') == -1) {
            //						return "_" + oldName;
            //					}
            //					return oldName;
            //				}
            //			};
            //
            //			BytecodeRefactorer refactorer = new BytecodeRefactorer(contents.getClassContents(), rm);
            //			refactorer.start();
        }


        CompleteJarDumper dumper = new CompleteJarDumper(contents);
        String name = getRevision().getName();
        File file = new File("out/" + name + "/deob.jar");
        if (file.exists())
            file.delete();
        file.mkdirs();

        try {
            dumper.dump(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean paramsMatch(String d1, String d2) {
        Type[] args1 = Type.getArgumentTypes(d1);
        Type[] args2 = Type.getArgumentTypes(d2);
        if (args1.length == args2.length) {
            for (int i = 0; i < args1.length; i++) {
                if (!args1[i].getDescriptor().equals(args2[i].getDescriptor()))
                    return false;
            }
            return true;
        }
        return false;
    }

    private void buildCases() {
        Map<String, ClassNode> classNodes = contents.getClassContents().namedMap();
        caseAnalyser = new CaseAnalyser();
        for (ClassNode cn : classNodes.values()) {
            for (MethodNode m : cn.methods) {
                if (m.instructions.size() >= 11000) {//jesus christ
                    try {
                        if (caseAnalyser.analyse(m, cfgCache.get(m)))
                            return;
                    } catch (ControlFlowException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void analyse() throws AnalysisException {

        buildCases();

        Map<String, ClassNode> classNodes = contents.getClassContents().namedMap();
        for (ClassAnalyser a : analysers) {
            if (haltRequested)
                return;

            // System.out.println("Running " + a.getName());

            try {
                a.preRun(classNodes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (a.getFoundClass() == null || a.getFoundHook() == null)
                //System.err.println("Couldn't find " + a.getName());
                throw new AnalysisException("Couldn't find " + a.getName());

            if (haltRequested)
                return;
        }

        if (haltRequested)
            return;

        for (ClassAnalyser a : analysers) {
            if (haltRequested)
                return;

            try {
                a.runSubs();
            } catch (Exception e) {
                System.err.println(a.getClass().getCanonicalName() + " -> " + e.getClass().getSimpleName());
            }

            if (haltRequested)
                return;
        }

        if (haltRequested)
            return;
    }

    private void deobfuscate() {
        JarContents<ClassNode> contents = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(this.contents.getClassContents()), null, null);

        if (flags.getOrDefault("reorderfields", true))
            reorderFields();
        openfields();

        analyseMultipliers();
        removeDummyMethods(contents);
        removeUnusedFields();
        //TODO: fix flow
        //fixFlow();

        reorderOperations();
        reorderNullChecks();
        deobOpaquePredicates();

        if (flags.getOrDefault("paramdeob", false)) {
            fixEmptyParams();
        }

        inlinestrings();
        removeEmptyPops();

        CatchBlockFixer.rek(contents.getClassContents());
        // not really needed + a bit slow
        // replaceCharStringBuilders();

        // destroyMultis();
        // removeMultis();
//		buildCfgs();
//		 reorderBlocks();
        buildCfgs();
//		transformRedundantGotos();
        // transformGotos();
        // reorderBlocks();
    }

    private void inlinestrings() {
        StringInliner inliner = new StringInliner();
        inliner.accept(contents);
    }

    private void transformRedundantGotos() {
        RedunantGotoTransformer transformer = new RedunantGotoTransformer();

        for (ClassNode cn : contents.getClassContents()) {
            for (MethodNode m : cn.methods) {
                if (m.instructions.size() > 0) {
                    try {
                        transformer.restructure(m, cfgCache.get(m));
                    } catch (ControlFlowException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        transformer.output();
    }

    private void destroyMultis() {
        MultiplicativeModifierDestroyer destroyer = new MultiplicativeModifierDestroyer();
        visit(destroyer);
    }

    private void removeMultis() {
        ComplexNumberVisitor visitor = new ComplexNumberVisitor();
        visitor.run(contents.getClassContents(), builder);
    }

    private void buildCfgs() {
        cfgCache = new CFGCache(new ValueCreator<IControlFlowGraph>() {
            @Override
            public IControlFlowGraph create() {
                return new SaneControlFlowGraph();
            }
        });
        for (ClassNode cn : contents.getClassContents()) {
            for (MethodNode m : cn.methods) {
                if (m.instructions.size() > 0) {
                    try {
                        cfgCache.get(m);
                    } catch (ControlFlowException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (Context.current().getFlags().getOrDefault("basicout", true))
            System.err.printf("Built %d control flow graphs.%n", cfgCache.size());
    }

    private void replaceCharStringBuilders() {
        StringBuilderCharReplacer replacer = new StringBuilderCharReplacer();
        visit(replacer);
        replacer.output();
    }

    private void removeEmptyPops() {
        EmptyPopRemover remover = new EmptyPopRemover();
        visit(remover);
        remover.output();
    }

    private void fixEmptyParams() {
        emptyParameterFixer = new EmptyParameterFixer();
        emptyParameterFixer.visit(contents);

        //System.exit(0);
    }

    private void deobOpaquePredicates() {
        opaqueRemover = new OpaquePredicateRemover();

        for (ClassNode cn : contents.getClassContents()) {
            for (MethodNode m : cn.methods) {
                if (m.instructions.size() > 0) {
                    if (opaqueRemover.methodEnter(m)) {
                        builder.build(m).accept(opaqueRemover);
                        opaqueRemover.methodExit();
                    }
                }
            }
        }

        opaqueRemover.output();
    }

    private void reorderNullChecks() {
        ComparisonReorderer fixer = new ComparisonReorderer();
        visit(fixer);
        fixer.output();
    }

    private void reorderOperations() {
        ConstantAppropriator propagator = new ConstantAppropriator();
        SimpleArithmeticFixer fixer = new SimpleArithmeticFixer();

        if (Context.current().getFlags().getOrDefault("basicout", true)) {
            System.err.println("Running Simple Arithmetic Fixer.");
        }

        visit(propagator);
        visit(fixer);
        fixer.output();
    }

    private void removeUnusedFields() {
        new UnusedFieldRemover().visit(contents);
    }

    private void openfields() {
        new FieldOpener().visit(contents);
        if (flags.getOrDefault("basicout", true))
            System.err.printf("Opened fields.%n");
    }

    private void reorderFields() {
        int count = 0;

        for (ClassNode cn : contents.getClassContents()) {
            List<FieldNode> fields = cn.fields;
            Collections.sort(fields, new Comparator<FieldNode>() {
                @Override
                public int compare(FieldNode o1, FieldNode o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
            count += fields.size();
        }

        if (flags.getOrDefault("basicout", true))
            System.err.printf("Reordered %d fields.%n", count);
    }

    private void removeDummyMethods(JarContents<? extends ClassNode> contents) {
        new HierarchyVisitor().accept(contents);
        new CallVisitor().accept(contents);
    }

    private void analyseMultipliers() {
        MultiplierVisitor mutliVisitor = new MultiplierVisitor(multiplierHandler);
        visit(mutliVisitor);
        mutliVisitor.log();
    }

    private void visit(NodeVisitor nv) {
        for (ClassNode cn : contents.getClassContents()) {
            visit(cn, nv);
        }
    }

    private void visit(ClassNode cn, NodeVisitor nv) {
        for (MethodNode m : cn.methods) {
            visit(m, nv);
        }
    }

    private void visit(MethodNode m, NodeVisitor nv) {
        if (m.instructions.size() > 0) {
            builder.build(m).accept(nv);
            ;
        }
    }

    protected abstract Builder<ClassAnalyser> registerAnalysers() throws AnalysisException;

    public Revision getRevision() {
        return revision;
    }

    public Map<String, ClassNode> getClassNodes() {
        return contents.getClassContents().namedMap();
    }

    public List<ClassAnalyser> getAnalysers() {
        return analysers;
    }

    public MultiplierHandler getMultiplierHandler() {
        return multiplierHandler;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public String[] getInstructions() {
        return instructions;
    }

    public ClassTree getClassTree() {
        return classTree;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDeobTime() {
        return deobTime;
    }

    public long getAnalysisTime() {
        return analysisTime;
    }

    public CFGCache getCFGCache() {
        return cfgCache;
    }

    private String[] getAllInstructions() {
        InsnList aIns = null;
        List<String> mI = new ArrayList<String>();
        List<String> ins = new ArrayList<String>();
        Iterator<ClassNode> it = contents.getClassContents().iterator();
        while (it.hasNext())
            for (Object m : it.next().methods) {
                aIns = ((MethodNode) m).instructions;
                mI = new InstructionIdentifier(aIns.toArray()).getInstList();
                Collections.addAll(ins, mI.toArray(new String[mI.size()]));
            }
        return ins.toArray(new String[ins.size()]);
    }

    public boolean isHaltRequested() {
        return haltRequested;
    }

    public OpaquePredicateRemover getOpaqueRemover() {
        return opaqueRemover;
    }

    public void requestHalt() {
        this.haltRequested = true;
    }

    public CaseAnalyser getCaseAnalyser() {
        return caseAnalyser;
    }

    public ReverseMethodDescCache getMethodCache() {
        return methodCache;
    }
}