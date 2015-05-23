package org.nullbool.api.analysis;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.util.InstructionIdentifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.DynamicDesc;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;
import org.zbot.hooks.ObfuscatedData;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public abstract class ClassAnalyser implements Opcodes {

	private final String name;
	private ClassNode foundClass;
	private ClassHook foundHook;

	public ClassAnalyser(String name) {
		this.name = name;
	}

	public void preRun(Map<String, ClassNode> classes) throws AnalysisException {
		foundClass = analyse(classes);
		foundHook = new ClassHook(foundClass.name, name);
	}

	public ClassNode getFoundClass() {
		return foundClass;
	}

	public ClassHook getFoundHook() {
		return foundHook;
	}

	public void runSubs() {
		List<IFieldAnalyser> fs = registerFieldAnalysers();
		if (fs != null) {
			for (IFieldAnalyser f : fs) {
				try {
					foundHook.getFields().addAll(f.find(foundClass));
				} catch (Exception e) {
					System.err.println(f.getClass().getCanonicalName() + " -> " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}
		}

		List<IMethodAnalyser> ms = registerMethodAnalysers();
		if (ms != null) {
			for (IMethodAnalyser m : ms) {
				try {
					foundHook.getMethods().addAll(m.find(foundClass));
				} catch (Exception e) {
					System.err.println(m.getClass().getCanonicalName() + " -> " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}
		}
	}

	private SupportedHooks getSupportedHooksAnno() {
		SupportedHooks anno = getClass().getAnnotation(SupportedHooks.class);
		return anno;
	}

	public String[] supportedFields() {
		return getSupportedHooksAnno().fields();
	}

	public String[] supportedMethods() {
		return getSupportedHooksAnno().methods();
	}

	private ClassNode analyse(Map<String, ClassNode> classes) throws AnalysisException {
		ClassNode f_cn = null;
		for (ClassNode cn : classes.values()) {
			boolean match = matches(cn);
			if (match && f_cn != null) {
				throw new AnalysisException("Found twice.");
			} else if (match) {
				return cn;
			}
		}
		return null;
	}

	protected abstract boolean matches(ClassNode cn);

	protected abstract List<IFieldAnalyser> registerFieldAnalysers();

	protected abstract List<IMethodAnalyser> registerMethodAnalysers();

	public long getMethodDescCount(ClassNode cn, String regex) {
		Stream<MethodNode> s = cn.methods.stream();
		return s.filter(m -> ((MethodNode) m).desc.matches((regex))).count();
	}

	public boolean containMethodWithName(ClassNode classnode, String t) {
		Stream<MethodNode> stream = classnode.methods.stream();
		return stream.filter(m -> ((MethodNode) m).name.equals(t)).count() != 0;
	}

	private List<String> getCleanList(MethodNode node) {
		List<String> opList = new ArrayList<String>();
		AbstractInsnNode[] ins = node.instructions.toArray();
		Stream<String> s = new InstructionIdentifier(ins).getInstCleanList().stream();
		s = s.filter(n -> (n != null) && !n.trim().equalsIgnoreCase("f_new"));
		s.forEach(n -> opList.add(n));
		return opList;
	}

	public String findObfClassName(String n) {
		AbstractAnalysisProvider provider = Context.current();
		// System.out.println(provider.getAnalysers());
		Stream<ClassAnalyser> stream = provider.getAnalysers().stream();
		stream = stream.filter(a -> a.foundHook.getRefactored().equals(n));
		ClassAnalyser a = stream.findFirst().orElse(null);
		return a != null ? a.foundHook.getObfuscated() : null;
	}

	public ClassAnalyser getAnalyser(String name) {
		for (ClassAnalyser a : Context.current().getAnalysers()) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public ClassNode getClassNodeByRefactoredName(String name) {
		AbstractAnalysisProvider provider = Context.current();
		Stream<ClassAnalyser> stream = provider.getAnalysers().stream();
		stream = stream.filter(a -> a.foundHook.getRefactored().equals(name));
		ClassAnalyser a = stream.findFirst().orElse(null);
		return provider.getClassNodes().get(a.foundHook.getObfuscated());
	}

	public boolean containFieldOfType(ClassNode classnode, String t) {
		Stream<FieldNode> s = classnode.fields.stream();
		return s.filter(n -> ((FieldNode) n).desc.equals(t)).count() != 0;
	}

	public long getFieldOfTypeCount(ClassNode cn, String t) {
		Stream<FieldNode> s = cn.fields.stream();
		return s.filter(m -> ((FieldNode) m).desc.matches((t))).count();
	}

	public long getFieldOfTypeCount(ClassNode cn, String t, boolean is) {
		Stream<FieldNode> s = cn.fields.stream();
		s = s.filter(m -> ((FieldNode) m).desc.matches((t)));
		s = s.filter(f -> Modifier.isStatic(((FieldNode) f).access) == is);
		return s.count();
	}
	
	public int getFieldCount(ClassNode cn, Filter<FieldNode> filter) {
		int count = 0;
		for(FieldNode f : cn.fields) {
			if(filter.accept(f)) {
				count++;
			}
		}
		return count;
	}

	public boolean hasFieldValue(ClassNode classnode, Object v) {
		Stream<FieldNode> stream = classnode.fields.stream();
		return stream.filter(m -> (((FieldNode) m).value != null) && ((FieldNode) m).value.equals(v)).count() != 0;
	}

	public String getFieldOfType(ClassNode classnode, String t, boolean s) {
		for (Object node : classnode.fields)
			if (((FieldNode) node).desc.equalsIgnoreCase(t))
				if (Modifier.isStatic(((FieldNode) node).access) == s)
					return classnode.name + "." + ((FieldNode) node).name;
		return null;
	}

	public boolean containMethodOfType(ClassNode classnode, String t) {
		Stream<MethodNode> stream = classnode.methods.stream();
		stream = stream.filter(n -> ((MethodNode) n).desc.equalsIgnoreCase(t));
		return stream.findFirst().orElse(null) != null;
	}

	public MethodNode searchMethodDesc(MethodNode[] nodes, String t) {
		Stream<MethodNode> stream = Arrays.stream(nodes);
		stream = stream.filter(m -> m.desc.equalsIgnoreCase(t));
		return stream.findFirst().orElse(null);
	}

	public MethodNode searchMethod(MethodNode[] nodes, String t) {
		Stream<MethodNode> stream = Arrays.stream(nodes);
		stream = stream.filter(m -> m.name.equalsIgnoreCase(t));
		return stream.findFirst().orElse(null);
	}

	public MethodNode[] getMethodNodes(Object[] array) {
		MethodNode[] n = new MethodNode[array.length];
		IntStream stream = IntStream.range(0, n.length);
		stream.forEach(x -> n[x] = (MethodNode) array[x]);
		return n;
	}

	public String identifyField(Map<String, ClassNode> nodes, String desc) {
		Iterator<ClassNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			ClassNode cn = it.next();
			for (Object f : cn.fields)
				if (((FieldNode) f).desc.equalsIgnoreCase(desc))
					if (Modifier.isStatic(((FieldNode) f).access))
						return cn.name + "." + ((FieldNode) f).name;
		}
		return null;
	}

	public MethodNode identifyMethod(MethodNode[] methodNodes, boolean clean, String... pattern) {
		int count = 0;
		InstructionIdentifier i;
		String firstIn, secondIn;
		MethodNode result = null;
		List<String> opcodeList;
		int size = pattern.length;

		for (MethodNode methodNode : methodNodes) {
			i = new InstructionIdentifier(methodNode.instructions.toArray());
			opcodeList = clean ? i.getInstCleanList() : i.getInstList();
			if ((opcodeList.size() > 0) && ((opcodeList.size() - size) >= 0)) {
				for (int x = 0; x <= (opcodeList.size() - size); x++) {
					for (int index = 0; index < size; index++) {
						firstIn = opcodeList.get(x + index);
						secondIn = pattern[index];
						count += secondIn.equals(firstIn) ? 1 : 0;
					}
					result = size == count ? methodNode : result;
					count = 0;
				}
			}
		}
		return result;
	}

	public boolean findMethod(ClassNode cn, String mName, String... pattern) {
		int count = 0;
		boolean p = false;
		String firstIn, secondIn;
		List<String> opcodeList;
		int size = pattern.length;

		for (Object node : cn.methods.toArray()) {
			if (((MethodNode) node).name.toLowerCase().contains(mName)) {
				opcodeList = getCleanList(((MethodNode) node));
				if ((opcodeList.size() > 0) && ((opcodeList.size() - size) >= 0)) {
					for (int x = 0; x <= (opcodeList.size() - size); x++) {
						for (int index = 0; index < size; index++) {
							firstIn = opcodeList.get(x + index);
							secondIn = pattern[index];
							count += secondIn.equals(firstIn) ? 1 : 0;
						}
						p = size == count ? true : p;
						count = 0;
					}
				}
			}
		}
		return p;
	}

	public String identify(ClassNode cn, String type, char c) {
		for (Object methodNode : cn.methods) {
			MethodNode m = (MethodNode) methodNode;
			InstructionIdentifier i = new InstructionIdentifier(m.instructions.toArray());
			String hook;
			Stream<String> s = i.getInstList().stream();
			s = s.filter(k -> k.startsWith("put" + c) | k.startsWith("get" + c));
			s = s.filter(ins -> ins.split(" ")[2].trim().equalsIgnoreCase(type));
			if ((hook = s.findFirst().orElse(null)) != null)
				return hook.split(" ")[1].trim();
		}
		return null;
	}

	public FieldHook asFieldHook(String classAndName, String realName) {
		return asFieldHook(classAndName, realName, 1);
	}

	public FieldHook asFieldHook(String classAndName, String realName, long multiplier) {
		String[] parts = classAndName.split("\\.");
		ClassNode cn = Context.current().getClassNodes().get(parts[0]);
		for (Object oF : cn.fields) {
			FieldNode f = (FieldNode) oF;
			if (parts[1].equals(f.name)) {
				return new FieldHook(foundHook, new ObfuscatedData(f.name, realName), new DynamicDesc(f.desc, false), Modifier.isStatic(f.access), multiplier);
			}
		}
		return null;
	}
	
	public FieldHook asFieldHook(FieldInsnNode f, String realName) {
		boolean isStatic = f.opcode() == PUTSTATIC || f.opcode() == GETSTATIC;
		return asFieldHook(f, realName, isStatic, findMultiplier(source(f), isStatic));
	}
	
	public FieldHook asFieldHook(FieldInsnNode f, String realName, long multiplier) {
		return asFieldHook(f, realName, f.opcode() == PUTSTATIC || f.opcode() == GETSTATIC, multiplier);
	}
	
	public FieldHook asFieldHook(FieldInsnNode f, String realName, boolean isStatic, long multiplier) {
		return new FieldHook(foundHook, new ObfuscatedData(f.name, realName), new DynamicDesc(f.desc, false), isStatic, multiplier);
	}

	public MethodHook asMethodHook(MethodType type, MethodInsnNode min, String realName) {
		ClassNode cn = Context.current().getClassNodes().get(min.owner);
		for (Object oM : cn.methods) {
			MethodNode m = (MethodNode) oM;
			if (min.name.equals(m.name) && min.desc.equals(m.desc)) {
				return new MethodHook(type, foundHook, new ObfuscatedData(m.name, realName), new DynamicDesc(m.desc, true), Modifier.isStatic(m.access), null);
			}
		}
		return null;
	}
	
	// TODO:
	public MethodHook asMethodHook(MethodType type, String classAndName, String realName) {
		String[] parts = classAndName.split("\\.");
		// System.out.println("methods in " + Arrays.toString(parts));
		ClassNode cn = Context.current().getClassNodes().get(parts[0]);
		for (Object oM : cn.methods) {
			MethodNode m = (MethodNode) oM;
			// System.out.println("   method " + m.name + " " + m.desc);
			if (parts[1].equals(m.name)) {
				// System.out.println(classAndName);
				return new MethodHook(type, foundHook, new ObfuscatedData(m.name, realName), new DynamicDesc(m.desc, true), Modifier.isStatic(m.access), null);
			}
		}
		return null;
	}

	public ClassHook getNew(String k) {
		for (ClassAnalyser analyser : Context.current().getAnalysers()) {
			ClassHook c = analyser.getFoundHook();
			if (c != null) {
				if (c.getObfuscated().equals(k))
					return c;
			}
		}
		// System.out.println("Making: " + k);
		return new ClassHook(k, null);
	}

	public MethodHook asMethodHook(MethodType type, MethodNode m, String realName) {
		return new MethodHook(type, getNew(m.owner.name), new ObfuscatedData(m.name, realName), new DynamicDesc(m.desc, true), Modifier.isStatic(m.access),
				null);
	}

	public MethodNode[] findMethods(Map<String, ClassNode> nodes, String regularExpression, boolean isStatic) {
		List<MethodNode> methods = new ArrayList<MethodNode>();
		Iterator<ClassNode> n = nodes.values().iterator();
		while (n.hasNext()) {
			ClassNode cn = n.next();
			for (Object methodNodeObject : cn.methods) {
				MethodNode m = (MethodNode) methodNodeObject;
				if (m.desc.replaceAll("[()]", ";").matches(regularExpression))
					if ((Modifier.isStatic(m.access) && isStatic) || !isStatic)
						methods.add(m);
			}
		}
		return methods.toArray(new MethodNode[methods.size()]);
	}

	public MethodNode[] startWithBc(String[] pat, MethodNode... methodsNodes) {
		int insCounter = 0;
		boolean condition = false;
		List<String> opcodeList;
		List<MethodNode> methods = new ArrayList<MethodNode>();
		for (MethodNode methodNode : methodsNodes) {
			insCounter = 0;
			opcodeList = getCleanList(methodNode);
			condition = methodNode.instructions.size() > 10;
			for (int i = 0; (i < pat.length) && condition; i++)
				insCounter += opcodeList.get(i).equals(pat[i]) ? 1 : 0;
			if ((pat.length == insCounter) && condition)
				methods.add(methodNode);
		}
		return methods.toArray(new MethodNode[methods.size()]);
	}

	public String findNearIns(MethodNode mNode, String ins, String s, String e) {
		String firstIns, secondIns, fullIns, fieldSource = null;
		InstructionIdentifier f = new InstructionIdentifier(mNode.instructions.toArray());
		List<String> instructionsList = f.getInstCleanList();
		int index = instructionsList.indexOf(ins);
		int max = (instructionsList.size() / 2) - 1;
		for (int x = 1; x <= max; x++) {
			firstIns = instructionsList.get(index + x);
			secondIns = instructionsList.get(index - x);
			if (firstIns.startsWith(s) || firstIns.startsWith(e)) {
				fullIns = f.getInstList().get(index + x);
				fieldSource = fullIns.split(" ")[1];
				break;
			}
			if (secondIns.startsWith(s) || secondIns.startsWith(e)) {
				fullIns = f.getInstList().get(index - x);
				fieldSource = fullIns.split(" ")[1];
				break;
			}
		}
		return fieldSource;
	}

	public String findField(MethodNode method, boolean rawInsnList, boolean jumpForward, int position, char type, String... pattern) {
		return findField(method.instructions.toArray(), rawInsnList, jumpForward, position, type, pattern);
	}

	public String findField(AbstractInsnNode[] instructions, boolean rawInsnList, boolean jumpForward, int position, char type, String... pattern) {
		int counter = 0;
		String get = "get" + type;
		String put = "put" + type;
		InstructionIdentifier f = new InstructionIdentifier(instructions);
		List<String> insnList = rawInsnList ? f.getInstList() : f.getInstCleanList();
		for (int i = getIndex(insnList, pattern); (i >= 0) && (i < insnList.size()); i += jumpForward ? 1 : -1) {
			if (insnList.get(i).startsWith(get) || insnList.get(i).startsWith(put)) {
				if (++counter == position)
					return f.getInstList().get(i).split(" ")[1];
			}
		}
		return null;
	}

	public String findField(MethodNode m, String insName, String pattern) {
		InstructionIdentifier f = new InstructionIdentifier(m.instructions.toArray());
		List<String> i = f.getInstList();
		for (int x = getIndex(i, pattern); x < i.size(); x++)
			if (i.get(x).matches(insName))
				return f.getInstList().get(x).split(" ")[1];
		return null;
	}

	private int getIndex(List<String> ins, String... pat) {
		String firstIn, secondIn;
		int count = 0, index = -1;
		int size = ins.size() - pat.length;
		for (int x = 0; (x < size) && (index == -1); x++) {
			for (int y = 0; y < pat.length; y++) {
				firstIn = pat[y];
				secondIn = ins.get(x + y);
				count += secondIn.matches(firstIn) ? 1 : 0;
			}
			index = pat.length == count ? x : index;
			count = 0;
		}
		return index;
	}

	public AbstractInsnNode[] followJump(MethodNode node, int maxGoto) {
		return followJump(node, "yolo", maxGoto);
	}

	public AbstractInsnNode[] followJump(MethodNode node, String s, int maxGoto) {
		boolean condition;
		int op, compteur = 0;
		AbstractInsnNode i = node.instructions.toArray()[index(node, s)];
		List<AbstractInsnNode> list = new ArrayList<AbstractInsnNode>();
		while ((i != null) && (compteur <= maxGoto)) {
			condition = (i.opcode() != -1) && (i instanceof JumpInsnNode);
			i = condition ? ((JumpInsnNode) i).label.getNext() : i.getNext();
			op = (i != null) ? i.opcode() : -1;
			compteur += condition ? 1 : 0;
			if ((i != null) && (op != -1) && (op != Opcodes.GOTO))
				list.add(i);
		}
		return list.toArray(new AbstractInsnNode[list.size()]);
	}

	public int index(MethodNode node, String s) {
		int index = 0;
		InstructionIdentifier id = new InstructionIdentifier(node.instructions.toArray());
		List<String> instructions = id.getInstList();
		for (int x = 0; (x < instructions.size()) && (index == 0); x++)
			if (instructions.get(x).matches(s))
				index = x;
		return index;
	}

	public long findMultiplier(String source, boolean isStatic) {
		return Context.current().getMultiplierHandler().getDecoder(source);
	}
	
	public List<AbstractInsnNode[]> findAllOpcodePatterns(MethodNode m, int[] opcodes) {
		List<AbstractInsnNode[]> set = new ArrayList<AbstractInsnNode[]>();
		AbstractInsnNode[] array = new AbstractInsnNode[opcodes.length];
		
		AbstractInsnNode[] insns = m.instructions.toArray();
		int j = 0;
		for(int i=0; i < insns.length; i++) {
			AbstractInsnNode ain = insns[i];
			
			int target = opcodes[j++];
			
			if(target == -1) {					
//				System.out.println("ClassAnalyser.findOpcodePattern() -1 target");
				continue;
			} else if(target == -2) {
				//distance to the next one
				int next = nextDefined(opcodes, j);
				int dist = getDistance(insns, i, next);
				if(dist != -1) {
					System.out.println("next " + next);
					System.out.println("ClassAnalyser.findOpcodePattern() -1 dist");
				}
				
				i = i - 1;
				continue;
			}
			
			if(ain.opcode() != -1) {
				if(ain.opcode() == target) {
					array[j - 1] = ain;
					if(j == opcodes.length) { 
						set.add(array);
						array = new AbstractInsnNode[opcodes.length];
						j = 0;

					}
				} else {
					j = 0;
				}
			} else {
				j -= 1;
				continue;
			}
		}
		
		return set;
	}
	
	public List<AbstractInsnNode> findAllOpcodePatternsStarts(List<AbstractInsnNode[]> list, int[] opcodes) {
		if(list.isEmpty())
			return null;
		List<AbstractInsnNode> list2 = new ArrayList<AbstractInsnNode>();
		
		for(AbstractInsnNode[] ains : list) {
			list2.add(ains[0]);
		}
		
		return list2;
	}
	
	public List<AbstractInsnNode> findAllOpcodePatternsStarts(MethodNode m, int[] opcodes) {
		List<AbstractInsnNode[]> list = findAllOpcodePatterns(m, opcodes);
		if(list.isEmpty())
			return null;
		List<AbstractInsnNode> list2 = new ArrayList<AbstractInsnNode>();
		
		for(AbstractInsnNode[] ains : list) {
			list2.add(ains[0]);
		}
		
		return list2;
	}
	
	public AbstractInsnNode findOpcodePattern(MethodNode m, int[] opcodes) {
		List<AbstractInsnNode[]> list = findAllOpcodePatterns(m, opcodes);
		if(list.isEmpty())
			return null;
		return list.get(0)[0];
	}
	
	public AbstractInsnNode[] findOpcodePatternArr(MethodNode m, int[] opcodes) {
		List<AbstractInsnNode[]> list = findAllOpcodePatterns(m, opcodes);
		if(list.isEmpty())
			return null;
		return list.get(0);
	}
	
	public int countOpcodePatterns(MethodNode m, int[] opcodes) {
		return findAllOpcodePatterns(m, opcodes).size();
	}
	
	public int nextDefined(int[] opcodes, int offset) {
		for(int i=offset; i < opcodes.length; i++) {
			int op = opcodes[i];
			if(op >= 0)
				return i;
		}
		return offset;
	}
	
	public int getDistance(AbstractInsnNode[] insns, int offset, int target) {
		for(int i=offset; i < insns.length; i++) {
			AbstractInsnNode ain = insns[i];
			if(ain.opcode() == target)
				return i;
		}
		
		return -1;
	}
	
	public String source(FieldInsnNode fin) {
		return String.format("%s.%s", fin.owner, fin.name);
	}

	public String getName() {
		return name;
	}
}