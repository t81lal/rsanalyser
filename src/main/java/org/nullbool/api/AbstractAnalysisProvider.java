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
import org.nullbool.api.obfuscation.call.CallVisitor;
import org.nullbool.api.obfuscation.call.HierarchyVisitor;
import org.nullbool.api.obfuscation.dummyparam.EmptyParamVisitor;
import org.nullbool.api.obfuscation.dummyparam.EmptyParamVisitor2;
import org.nullbool.api.obfuscation.dummyparam.OpaquePredicateVisitor;
import org.nullbool.api.obfuscation.field.FieldOpener;
import org.nullbool.api.obfuscation.field.UnusedFieldRemover;
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
	private ClassTree classTree;
	private List<ClassAnalyser> analysers;
	private MultiplierHandler multiplierHandler;

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
		classTree         = new ClassTree(contents.getClassContents());
		multiplierHandler = new MultiplierHandler();
		deobfuscate();

		dumpDeob();

		if (!flags.getOrDefault("justdeob", false)) {
			analysers = registerAnalysers();
			if (analysers != null && analysers.size() != 0)
				analyse();

			output();
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
		
		dumpJar(hookMap);
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
		
		// runEmptyParamVisitor2(contents);
		// checkRecursion(contents);
		// removeUnusedParams(contents);
		// removeOpaquePredicates(contents);
		// // run again after and see if any more methods are swept after opaques are removed
		// int[] last = new int[4];
		// EmptyParamVisitor k = null;
		// do {
		// k = quiet_removeUnusedParams(contents);
		// int[] j = k.getCounts();
		// last[0] += j[0];
		// last[1] += j[1];
		// last[2] += j[2];
		// last[3] += j[3];
		// } while (k != null && k.getCounts() != null && k.getCounts()[3] != 0);
		//
		// EmptyParamVisitor.print(last, " #2");
		//
		// this.contents.getClassContents().clear();
		// this.contents.getClassContents().addAll(contents.getClassContents());
		// this.contents.getClassContents().namedMap();
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
	
	private void runEmptyParamVisitor2(JarContents<? extends ClassNode> contents) {
		new EmptyParamVisitor2().accept(contents);
	}

	private int[] removeUnusedParams(JarContents<? extends ClassNode> contents) {
		return new EmptyParamVisitor(false).countRemoval(contents);
	}

	//just in case
	private EmptyParamVisitor quiet_removeUnusedParams(JarContents<? extends ClassNode> contents) {
		EmptyParamVisitor visitor = new EmptyParamVisitor(true);
		visitor.countRemoval(contents);
		return visitor;
	}

	private void removeOpaquePredicates(JarContents<? extends ClassNode> contents) {
		new OpaquePredicateVisitor().accept(contents);
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

	protected abstract List<ClassAnalyser> registerAnalysers() throws AnalysisException;

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