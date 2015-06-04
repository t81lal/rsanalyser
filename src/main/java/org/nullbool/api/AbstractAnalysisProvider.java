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
import org.nullbool.api.obfuscation.CallVisitor;
import org.nullbool.api.obfuscation.EmptyGotoCollapser;
import org.nullbool.api.obfuscation.EmptyParameterFixer;
import org.nullbool.api.obfuscation.EmptyPopRemover;
import org.nullbool.api.obfuscation.FieldOpener;
import org.nullbool.api.obfuscation.HierarchyVisitor;
import org.nullbool.api.obfuscation.MultiplicativeModifierCollector;
import org.nullbool.api.obfuscation.MultiplicativeModifierRemover;
import org.nullbool.api.obfuscation.NullCheckFixer;
import org.nullbool.api.obfuscation.OpaquePredicateRemover;
import org.nullbool.api.obfuscation.SimpleArithmeticFixer;
import org.nullbool.api.obfuscation.StringBuilderCharReplacer;
import org.nullbool.api.obfuscation.UnusedFieldRemover;
import org.nullbool.api.obfuscation.cfg.CFGCache;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.number.MultiplierVisitor;
import org.nullbool.api.obfuscation.refactor.BytecodeRefactorer;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.IRemapper;
import org.nullbool.api.output.APIGenerator;
import org.nullbool.api.output.OutputLogger;
import org.nullbool.api.util.InstructionIdentifier;
import org.nullbool.api.util.NodedContainer;
import org.nullbool.api.util.PatternParser;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.out.CompleteJarDumper;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.MethodHook;

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
	private CFGCache cfgCache;

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

		for (ClassHook h : hookMap.getClasses()) {
			for (MethodHook m : h.getMethods()) {
				if (m.getInstructions() != null)
					m.getInstructions().reset();
			}
		}

		if(flags.getOrDefault("basicout", true)) {
			APIGenerator.createAPI(hookMap);
			writeLog(hookMap);
			// HookMap map = new HookMap();
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
//			map.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dumpJar(HookMap hookMap) {
		Map<String, ClassHook>  classes = new HashMap<String, ClassHook>();
		Map<String, FieldHook>  fields  = new HashMap<String, FieldHook>();
		Map<String, MethodHook> methods = new HashMap<String, MethodHook>();
		
		for(ClassHook h : hookMap.getClasses()){
			classes.put(h.getObfuscated(), h);
			for(FieldHook f : h.getFields()){
				fields.put(f.getOwner().getObfuscated() + "." + f.getName().getObfuscated() + " " + f.getDesc().getObfuscated(), f);
			}
			
			for(MethodHook m : h.getMethods()){
				methods.put(m.getOwner().getObfuscated() + "." + m.getName().getObfuscated() + m.getDesc().getObfuscated(), m);
			}
		}
		
		IRemapper remapper = new IRemapper() {
			@Override
			public String resolveMethodName(String owner, String name, String desc) {
				String key = owner + "." + name + desc;
				if(methods.containsKey(key)){
					return methods.get(key).getName().getRefactored();
				}
				return name;
			}
			
			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				String key = owner + "." + name + " " + desc;
				if(fields.containsKey(key)){
					return fields.get(key).getName().getRefactored();
				}
				//let the refactorer do it's own thang if we can't quick-find it
				//  ie. it will do a deep search.
				return null;
			}
			
			@Override
			public String resolveClassName(String owner) {
				ClassHook ref = classes.get(owner);
				if(ref != null)
					return ref.getRefactored();
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
			try {
				a.preRun(classNodes);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (a.getFoundClass() == null || a.getFoundHook() == null)
				throw new AnalysisException("Couldn't find " + a.getName());
		}
		
		for (ClassAnalyser a : analysers) {
			try {
				a.runSubs();
			} catch (Exception e) {
				System.err.println(a.getClass().getCanonicalName() + " -> " + e.getClass().getSimpleName());
			}
		}
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
		fixEmptyParams();
		removeEmptyPops();
		//not really needed + a bit slow
		//replaceCharStringBuilders();
		//TOOD: multis
		//removeMultis();
		buildCfgs();
		collapseEmptyGotoBlocks();
	}
	
	private void removeMultis() {
		MultiplicativeModifierCollector collector = new MultiplicativeModifierCollector();
		TreeBuilder builder = new TreeBuilder();
		
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
	
	private void collapseEmptyGotoBlocks() {
		EmptyGotoCollapser collapser = new EmptyGotoCollapser();
		for(ClassNode cn : contents.getClassContents()) {
			if(cn.name.equals("am")) {
//			if(cn.name.equals("ae")) {
				for(MethodNode m : cn.methods) {
//					if(m.name.equals("c")) {
					if(m.name.equals("f")) {
						try {
//							HandlerBlockRemover.fix(m, cfgCache.get(m));
							collapser.collapse(m, cfgCache.get(m));
						} catch (ControlFlowException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void buildCfgs() {
		cfgCache = new CFGCache();
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
		
		System.err.printf("Built %d control flow graphs.%n", cfgCache.size());
	}
	
	private void replaceCharStringBuilders() {
		StringBuilderCharReplacer replacer = new StringBuilderCharReplacer();
		TreeBuilder builder = new TreeBuilder();
		
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
		TreeBuilder builder = new TreeBuilder();
		
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
		fixer.output();
	}
	
	private void deobOpaquePredicates() {
		OpaquePredicateRemover remover = new OpaquePredicateRemover();
		
		TreeBuilder builder = new TreeBuilder();
		
		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					if(remover.methodEnter(m)) {
						builder.build(m).accept(remover);
						remover.methodExit();
					}
				}
			}
		}
		
		remover.output();
	}
	
	private void reorderNullChecks() {
		NullCheckFixer fixer = new NullCheckFixer();
		TreeBuilder builder = new TreeBuilder();

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
		TreeBuilder builder = new TreeBuilder();
		
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
	
	private void fixFlow() {
//		ControlFlowGraph graph = new ControlFlowGraph();
//		
//		int c = 0;
//		
//		for(ClassNode cn : contents.getClassContents()) {
//			for(MethodNode m : cn.methods) {
//				if(m.instructions.size() > 0) {
//					c++;
//				}
//			}
//		}
//		
//		System.out.printf("Can build %d graphs.%n", c);
//		
//		for(ClassNode cn : contents.getClassContents()) {
//			for(MethodNode m : cn.methods) {
//				if(m.instructions.size() > 0) {
//					
//					//TODO: FIX
//					//UPDATE: fixed
//					/*if(m.key().equals("aa.am([II)V")) {
//						graph.debug = true;
//					} else {
//						graph.debug = false;
//					}*/
//					
//					if(m.owner.name.equals("dh") && m.name.equals("aj")) {
//						graph.debug = true;
//					} else {
//						graph.debug = false;
//					}
//					
//					try {
//						graph.create(m);
//						graph.fix();
//						graph.result(m);
//					} catch (ControlFlowException e) {
//						e.printStackTrace();
//					} finally {
//						graph.destroy();
//						
//						if(graph.debug)
//							System.exit(1);
//					}
//				}
//			}
//		}
//		
//		System.out.println("Done graphing.");
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
}