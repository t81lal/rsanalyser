package org.nullbool.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.AbstractClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.obfuscation.call.CallVisitor;
import org.nullbool.api.obfuscation.call.HierarchyVisitor;
import org.nullbool.api.obfuscation.dummyparam.DummyParameterVisitor;
import org.nullbool.api.obfuscation.dummyparam.DummyVisitor;
import org.nullbool.api.obfuscation.dummyparam.EmptyParamVisitor;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.number.MultiplierVisitor;
import org.nullbool.api.obfuscation.rename.Refactorer;
import org.nullbool.api.output.APIGenerator;
import org.nullbool.api.output.OutputLogger;
import org.nullbool.api.util.InstructionIdentifier;
import org.nullbool.api.util.NodedContainer;
import org.nullbool.api.util.PatternParser;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.out.CompleteJarDumper;
import org.zbot.hooks.HookMap;

@SuppressWarnings(value = { "all" })
public abstract class AbstractAnalysisProvider {

	private final Revision revision;
	private final LocateableJarContents<ClassNode> contents;
	private final String[] instructions;
	private final Map<String, String[]> patternMap;
	private List<AbstractClassAnalyser> analysers;
	private MultiplierHandler multiplierHandler;
	private final Map<String, Boolean> flags;

	public AbstractAnalysisProvider(Revision revision) throws IOException {
		this.revision = revision;
		contents = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(revision.parse().values()), null, null);
		instructions = getAllInstructions();
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
		multiplierHandler = new MultiplierHandler();
		deobfuscate();

		analysers = registerAnalysers();
		if (analysers != null && analysers.size() != 0)
			analyse();

		output();
	}

	private void output() {
		HookMap hookMap = OutputLogger.output();
		APIGenerator.createAPI(hookMap);
		writeLog(hookMap);
		// HookMap map = new HookMap();
		dumpRawJar();
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
			map.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dumpJar(HookMap hookMap) {
		Refactorer refactorer = new Refactorer(contents, hookMap);
		refactorer.run();
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

	private void dumpRawJar() {
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
		for (AbstractClassAnalyser a : analysers) {
			try {
				a.preRun(classNodes);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (a.getFoundClass() == null || a.getFoundHook() == null)
				throw new AnalysisException("Couldn't find " + a.getName());
		}

		for (AbstractClassAnalyser a : analysers) {
			try {
				a.runSubs();
			} catch (Exception e) {
				System.err.println(a.getName() + " -> " + e.getClass().getSimpleName());
			}
		}
	}

	private void deobfuscate() {
		JarContents<ClassNode> contents = new LocateableJarContents<ClassNode>(new NodedContainer<ClassNode>(this.contents.getClassContents()), null, null);
		analyseMultipliers();
		removeDummyMethods(contents);
		// checkRecursion(contents);
		removeUnusedParams(contents);
		// removeDummyParameters(contents);
		this.contents.getClassContents().clear();
		this.contents.getClassContents().addAll(contents.getClassContents());
		this.contents.getClassContents().namedMap();
	}

	private void removeUnusedParams(JarContents<? extends ClassNode> contents) {
		new EmptyParamVisitor().accept(contents);
	}

	private void checkRecursion(JarContents<? extends ClassNode> contents) {
		new DummyParameterVisitor().accept(contents);
	}

	private void removeDummyParameters(JarContents<? extends ClassNode> contents) {
		new DummyVisitor().accept(contents);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void removeDummyMethods(JarContents<? extends ClassNode> contents) {
		new HierarchyVisitor().accept(contents);
		new CallVisitor().accept(contents);
	}

	private void analyseMultipliers() {
		MultiplierVisitor mutliVisitor = new MultiplierVisitor(multiplierHandler);
		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				TreeBuilder.build(m).accept(mutliVisitor);
			}
		}
		mutliVisitor.log();
	}

	protected abstract List<AbstractClassAnalyser> registerAnalysers() throws AnalysisException;

	public Revision getRevision() {
		return revision;
	}

	public Map<String, ClassNode> getClassNodes() {
		return contents.getClassContents().namedMap();
	}

	public List<AbstractClassAnalyser> getAnalysers() {
		return analysers;
	}

	public MultiplierHandler getMultiplierHandler() {
		return multiplierHandler;
	}

	public Map<String, Boolean> getFlagsMap() {
		return flags;
	}

	public String[] getInstructions() {
		return instructions;
	}

	public String[] getPattern(String key) {
		return patternMap.get(key);
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