package org.nullbool.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.obfuscation.BlockReorderer;
import org.nullbool.api.obfuscation.CallVisitor;
import org.nullbool.api.obfuscation.ComparisonReorderer;
import org.nullbool.api.obfuscation.EmptyParameterFixer;
import org.nullbool.api.obfuscation.EmptyPopRemover;
import org.nullbool.api.obfuscation.FieldOpener;
import org.nullbool.api.obfuscation.HierarchyVisitor;
import org.nullbool.api.obfuscation.MultiplicativeModifierCollector;
import org.nullbool.api.obfuscation.MultiplicativeModifierDestroyer;
import org.nullbool.api.obfuscation.MultiplicativeModifierRemover;
import org.nullbool.api.obfuscation.OpaquePredicateRemover;
import org.nullbool.api.obfuscation.OpaquePredicateRemover.Opaque;
import org.nullbool.api.obfuscation.SimpleArithmeticFixer;
import org.nullbool.api.obfuscation.StringBuilderCharReplacer;
import org.nullbool.api.obfuscation.UnusedFieldRemover;
import org.nullbool.api.obfuscation.cfg.CFGCache;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.SaneControlFlowGraph;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.number.MultiplierVisitor;
import org.nullbool.api.obfuscation.refactor.BytecodeRefactorer;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.IRemapper;
import org.nullbool.api.obfuscation.refactor.MethodCache;
import org.nullbool.api.output.APIGenerator;
import org.nullbool.api.output.OutputLogger;
import org.nullbool.api.util.InstructionIdentifier;
import org.nullbool.api.util.NodedContainer;
import org.nullbool.api.util.PatternParser;
import org.nullbool.api.util.map.ValueCreator;
import org.nullbool.pi.core.hook.serimpl.StaticMapSerialiserImpl;
import org.nullbool.zbot.pi.core.hooks.api.ClassHook;
import org.nullbool.zbot.pi.core.hooks.api.FieldHook;
import org.nullbool.zbot.pi.core.hooks.api.HookMap;
import org.nullbool.zbot.pi.core.hooks.api.MethodHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.out.CompleteJarDumper;

@SuppressWarnings(value = { "all" })
public abstract class AbstractAnalysisProvider {

	private final Revision revision;
	private final LocateableJarContents<ClassNode> contents;
	private final String[] instructions;
	private final Map<String, String[]> patternMap;
	private final Map<String, Boolean> flags;
	private long startTime;
	private long deobTime;
	private long analysisTime;
	private ClassTree classTree;
	private List<ClassAnalyser> analysers;
	private MultiplierHandler multiplierHandler;
	private OpaquePredicateRemover opaqueRemover;
	private CFGCache cfgCache;
	private TreeBuilder builder;

	private volatile boolean haltRequested;

	public AbstractAnalysisProvider(Revision revision) throws IOException {
		this.revision = revision;
		contents      = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(revision.parse().values()), null, null);
		instructions  = getAllInstructions();
		Map<String, String[]> patternMap = null;
		try {
			patternMap = new PatternParser().getPatterns();
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		this.patternMap = patternMap;
		flags = new HashMap<String, Boolean>();
	}

	public void run() throws AnalysisException {
		startTime         = System.currentTimeMillis();
		classTree         = new ClassTree(contents.getClassContents());
		classTree.output();

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

			if(haltRequested)
				return;
			
			analysisTime = System.currentTimeMillis() - now;

			try {
				output();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		Context.unbind();
	}

	private void output() {
		HookMap hookMap = OutputLogger.output();

		for (ClassHook h : hookMap.classes()) {
			for (MethodHook m : h.methods()) {
				if (m.insns() != null)
					m.insns().reset();
			}
		}

		if(flags.getOrDefault("basicout", true)) {
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
			for(ClassHook ch : map.classes()) {
				for(MethodHook mh : ch.methods()) {
					// TODO: Get after empty param deob
					MethodNode m = cache.get(mh.owner().obfuscated(), mh.obfuscated(), mh.val(MethodHook.DESC));
					
					if(m == null) {
						System.out.println("NULL " + mh.refactored());
						continue;
					}
					
					Opaque op = opaqueRemover.find(m);
					int num = 0;
					if(op != null) {
						num = op.getNum();
						switch(op.getOpcode()) {
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
					}
					mh.var(MethodHook.SAFE_OPAQUE, Integer.toString(num));
				}
			}
			
			new StaticMapSerialiserImpl().serialise(map, fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dumpJar(HookMap hookMap) {
		Map<String, ClassHook>  classes = new HashMap<String, ClassHook>();
		Map<String, FieldHook>  fields  = new HashMap<String, FieldHook>();
		Map<String, MethodHook> methods = new HashMap<String, MethodHook>();

		for(ClassHook h : hookMap.classes()){
			classes.put(h.obfuscated(), h);
			for(FieldHook f : h.fields()){
				fields.put(f.owner().obfuscated() + "." + f.obfuscated() + " " + f.val(FieldHook.DESC), f);
			}

			for(MethodHook m : h.methods()){
				methods.put(m.owner().obfuscated() + "." + m.obfuscated() + m.val(MethodHook.DESC), m);
			}
		}

		IRemapper remapper = new IRemapper() {
			@Override
			public String resolveMethodName(String owner, String name, String desc) {
				String key = owner + "." + name + desc;
				if(methods.containsKey(key)){
					return methods.get(key).refactored();
				}
				return name;
			}

			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				String key = owner + "." + name + " " + desc;
				if(fields.containsKey(key)){
					return fields.get(key).refactored();
				}
				//let the refactorer do it's own thang if we can't quick-find it
				//  ie. it will do a deep search.
				return null;
			}

			@Override
			public String resolveClassName(String owner) {
				ClassHook ref = classes.get(owner);
				if(ref != null)
					return ref.refactored();
				return owner;
			}
		};

		BytecodeRefactorer refactorer = new BytecodeRefactorer((Collection<ClassNode>) contents.getClassContents(), remapper);
		refactorer.start();

		//TODO: reorder
		if(flags.getOrDefault("reorderfields", true))
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
		CompleteJarDumper dumper = new CompleteJarDumper(contents);
		String name = getRevision().getName();
		File file = new File("out/" + name + "/deob" + name + ".jar");
		if (file.exists())
			file.delete();
		file.mkdirs();
		try {
			dumper.dump(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void analyse() throws AnalysisException {
		Map<String, ClassNode> classNodes = contents.getClassContents().namedMap();
		for (ClassAnalyser a : analysers) {
			if(haltRequested)
				return;
			
			try {
				a.preRun(classNodes);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (a.getFoundClass() == null || a.getFoundHook() == null)
				throw new AnalysisException("Couldn't find " + a.getName());
			
			if(haltRequested)
				return;
		}

		if(haltRequested)
			return;
		
		for (ClassAnalyser a : analysers) {
			if(haltRequested)
				return;
			
			try {
				a.runSubs();
			} catch (Exception e) {
				System.err.println(a.getClass().getCanonicalName() + " -> " + e.getClass().getSimpleName());
			}

			if(haltRequested)
				return;
		}
		
		if(haltRequested)
			return;
	}

	private void deobfuscate() {
		JarContents<ClassNode> contents = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(this.contents.getClassContents()), null, null);
		
		if(flags.getOrDefault("reorderfields", true))
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
		
		if(flags.getOrDefault("paramdeob", false)) {
			//fixEmptyParams();
		}
		
		removeEmptyPops();
		//not really needed + a bit slow
		//replaceCharStringBuilders();
		
		//destroyMultis();
		//TOOD: multis
		//removeMultis();
		buildCfgs();
		reorderBlocks();
		
//		if(true)
//			System.exit(1);
		
	}
	
	private void destroyMultis() {
		MultiplicativeModifierDestroyer destroyer = new MultiplicativeModifierDestroyer();
		
		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(destroyer);
				}
			}
		}
	}

	private void removeMultis() {
		MultiplicativeModifierCollector collector = new MultiplicativeModifierCollector();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(collector);
				}
			}
		}

		collector.output();

		MultiplicativeModifierRemover remover = new MultiplicativeModifierRemover(collector);
		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(remover);
				}
			}
		}

		remover.output();
	}

	private void reorderBlocks() {
		int j = 0;
		
		BlockReorderer reorderer = new BlockReorderer();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0 && m.tryCatchBlocks.size() <= 1) {
					try {
						IControlFlowGraph oldGraph = cfgCache.get(m);
						reorderer.reorder(m, oldGraph);
					} catch (ControlFlowException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		
		System.out.printf("Fixed %d methods.%n", j);
		reorderer.output();
	}
	
	private void buildCfgs() {
		cfgCache = new CFGCache(new ValueCreator<IControlFlowGraph>() {
			@Override
			public IControlFlowGraph create() {
				return new SaneControlFlowGraph();
			}
		});
		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					try {
						cfgCache.get(m);
					} catch (ControlFlowException e) {
						e.printStackTrace();
					}
				}
			}
		}

		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.err.printf("Built %d control flow graphs.%n", cfgCache.size());
	}

	private void replaceCharStringBuilders() {
		StringBuilderCharReplacer replacer = new StringBuilderCharReplacer();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(replacer);
				}
			}
		}

		replacer.output();
	}

	private void removeEmptyPops() {
		EmptyPopRemover remover = new EmptyPopRemover();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(remover);
				}
			}
		}

		remover.output();
	}

	private void fixEmptyParams() {
		EmptyParameterFixer fixer = new EmptyParameterFixer();
		fixer.visit(contents);
		
		//System.exit(0);
	}

	private void deobOpaquePredicates() {
		opaqueRemover = new OpaquePredicateRemover();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					if(opaqueRemover.methodEnter(m)) {
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

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(fixer);
				}
			}
		}

		fixer.output();
	}

	private void reorderOperations() {
		SimpleArithmeticFixer fixer = new SimpleArithmeticFixer();

		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.err.println("Running Simple Arithmetic Fixer.");

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(fixer);
				}
			}
		}

		fixer.output();
	}

	private void removeUnusedFields() {
		new UnusedFieldRemover().visit(contents);
	}

	private void openfields() {
		new FieldOpener().visit(contents);
		if(flags.getOrDefault("basicout", true))
			System.err.printf("Opened fields.%n");
	}

	private void reorderFields(){
		int count = 0;

		for(ClassNode cn : contents.getClassContents()){
			List<FieldNode> fields = cn.fields;
			Collections.sort(fields, new Comparator<FieldNode>() {
				@Override
				public int compare(FieldNode o1, FieldNode o2) {
					return o1.name.compareTo(o2.name);
				}
			});
			count += fields.size();
		}

		if(flags.getOrDefault("basicout", true))
			System.err.printf("Reordered %d fields.%n", count);
	}

	private void removeDummyMethods(JarContents<? extends ClassNode> contents) {
		new HierarchyVisitor().accept(contents);
		new CallVisitor().accept(contents);
	}

	private void analyseMultipliers() {
		MultiplierVisitor mutliVisitor = new MultiplierVisitor(multiplierHandler);
		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				new TreeBuilder().build(m).accept(mutliVisitor);
			}
		}
		mutliVisitor.log();
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

	public String[] getPattern(String key) {
		return patternMap.get(key);
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
}