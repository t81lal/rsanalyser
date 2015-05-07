package org.nullbool.api.obfuscation.dummyparam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.IntMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPrinter;
import org.topdank.byteengineer.commons.data.JarContents;

public class EmptyParamVisitor extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		int count = 0;
		int nCount = 0;
		int cCount = 0;
		int wtfCount = 0;

		StringBuilder results = new StringBuilder();

		Map<String, MethodNode> changed = new HashMap<String, MethodNode>();
		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				if (m.name.length() > 2)
					continue;
				Type[] args = Type.getArgumentTypes(m.desc);
				if (args.length == 0)
					continue;
				IntMap map = new IntMap();
				boolean isStatic = (m.access & ACC_STATIC) == ACC_STATIC;
				if (isStatic) {
					map.put0(args.length - 1);
				} else {
					map.put0(args.length);
				}

				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain instanceof VarInsnNode) {
						int var = ((VarInsnNode) ain).var;
						map.inc(var);
					} else if (ain instanceof IincInsnNode) {
						int var = ((IincInsnNode) ain).var;
						map.inc(var);
					}
				}
				Set<Integer> empty = map.findEmptyVars();
				if (empty.size() == 1) {
					if (!isStatic) {
						ClassStructure cs = (ClassStructure) cn;
						if (!cs.isInherited(m)) {
							if (changed.containsKey(m.key())) {
								System.err.println("ERRORRRRRRRRr(1) " + m.key());
							}
							changed.put(m.key(), m);
							m.desc = recreateDesc(args, Type.getReturnType(m.desc));
							nCount++;
						} else {
							int[] k = cleanVirtual(contents.getClassContents(), m, recreateDesc(args, Type.getReturnType(m.desc)), results);
							wtfCount += k[0];
							cCount += k[1];
						}
					} else {
						if (changed.containsKey(m.key())) {
							System.err.println("ERRORRRRRRRRR(2) " + m.key());
						}
						changed.put(m.key(), m);
						m.desc = recreateDesc(args, Type.getReturnType(m.desc));
						count++;
					}
				}
			}
		}

		cCount += fix(contents.getClassContents(), changed, results);

		try {

			String name = Context.current().getRevision().getName();
			File file = new File("out/" + name + "/emptyparam" + name + ".txt");

			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));

			for (MethodNode m : changed.values()) {
				bw.write(m.key());
				bw.newLine();
				for (String s : InstructionPrinter.getLines(m)) {
					bw.write(s);
					bw.newLine();
				}
				bw.newLine();
				bw.newLine();
				bw.newLine();
			}

			bw.write(results.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.err.println("Unused parameter fixer");
		System.out.printf("   %d empty parameter static methods changed.%n", count);
		System.out.printf("   %d empty parameter uninherited virtual methods unchanged.%n", nCount);
		System.out.printf("   %d empty parameter inherited methods changed.%n", wtfCount);
		System.out.printf("   Replaced %d calls + pop.%n", cCount);
	}

	public static int[] cleanVirtual(Collection<? extends ClassNode> classes, MethodNode m, String newDesc, StringBuilder results) {
		String oldHalfKey = m.halfKey();
		m.desc = newDesc;

		Map<String, MethodNode> changed = new HashMap<String, MethodNode>();
		ClassStructure owner = (ClassStructure) m.owner;
		// System.out.println(m.key());

		for (ClassStructure superCs : owner.supers) {
			// System.out.println("   sup:  " + superCs.name);
			for (MethodNode m1 : superCs.methods) {
				// if ((m1.access & ACC_STATIC) == 0)
				// continue; // virtual != static
				if (m1.halfKey().equals(oldHalfKey)) {
					changed.put(m1.key(), m1);
					m1.desc = newDesc;
				}
			}
		}

		for (ClassStructure delCs : owner.delegates) {
			// System.out.println("   del:  " + delCs.name);
			for (MethodNode m1 : delCs.methods) {
				// if ((m1.access & ACC_STATIC) == 0)
				// continue; // virtual != static
				if (m1.halfKey().equals(oldHalfKey)) {
					changed.put(m1.key(), m1);
					m1.desc = newDesc;
				}
			}
		}

		// System.out.println("   map: " + changed.size());

		return new int[] { changed.size(), fix(classes, changed, results) };
	}

	public static int fix(Collection<? extends ClassNode> classes, Map<String, MethodNode> changed, StringBuilder results) {
		int cCount = 0;
		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						MethodNode method = changed.get(min.key());
						if (method != null) {
							results.append(min.key() + "   ->   " + method.key() + "  in  " + m.key());
							results.append("\n");
							min.desc = method.desc;
							m.instructions.insertBefore(min, new InsnNode(POP));
							cCount++;
						}
					}
				}
			}
		}
		return cCount;
	}

	public static String recreateDesc(Type[] args, Type ret) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < args.length - 1; i++) {
			Type arg = args[i];
			sb.append(arg.getDescriptor());
		}
		sb.append(")");
		sb.append(ret.getDescriptor());
		return sb.toString();
	}
}