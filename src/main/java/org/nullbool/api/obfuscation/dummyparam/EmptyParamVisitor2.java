package org.nullbool.api.obfuscation.dummyparam;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class EmptyParamVisitor2 extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		// ClassNode c = contents.getClassContents().namedMap().get("ax");
		// (I)Lcy;
		// for (MethodNode me : c.methods) {
		// if (me.halfKey().equals("a(I)Lcy;")) {
		// System.out.println("at   " + me.key());
		// Set<MethodNode> tree = buildValidTree(me, 2);
		// System.out.println(tree);
		// }
		// }
		//
		// if ("".equals(""))
		// return;

		int delegatesCount = 0;
		int callFixCount = 0;
		// int mergeFixCount = 0;
		int inheritedFixCount = 0;
		int descFixCount = 0;
		List<? extends ClassNode> classes = contents.getClassContents();
		Set<MethodNode> visited = new HashSet<MethodNode>();
		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				if (visited.contains(m) || !looksLikeDummy(m))
					continue;

				Type[] args = Type.getArgumentTypes(m.desc);
				boolean isStatic = (m.access & ACC_STATIC) == ACC_STATIC;
				int targetVar = isStatic ? args.length - 1 : args.length;

				Set<MethodNode> set = buildValidTree(m, targetVar);
				if (set != null) {
					set.add(m);

					delegatesCount += set.size();
					Type retType = Type.getReturnType(m.desc);
					String newDesc = recreateDesc(args, retType);

					if (m.key().equals("bz.ae(I)V")) {
						// System.out.println("EmptyParamVisitor2.visit()");
					}

					callFixCount += fixCalls(classes, keysAsSet(set), newDesc);
					descFixCount += fixDescs(set, m, newDesc);

					// mergeFixCount += mergeShiftVars(set, hk, newDesc, targetVar);

					if (set.size() > 1) {
						inheritedFixCount += set.size();
						// System.out.println(m.key() + ": " + set);
					}

					visited.addAll(set);
				}
			}
		}

		System.err.printf("Unused parameter fixer%n");
		System.out.printf("   %d empty parameter methods (%d of which are inherited).%n", delegatesCount, inheritedFixCount);
		System.out.printf("   %d descriptors fixed.%n", descFixCount);
		// System.out.printf("   %d var shifts.%n", mergeFixCount);
		System.out.printf("   Fixed %d calls.%n", callFixCount);

	}

	private static Set<String> keysAsSet(Set<MethodNode> methods) {
		Set<String> set = new HashSet<String>();
		for (MethodNode m : methods) {
			set.add(m.key());
		}
		return set;
	}

	private static int fixDescs(Set<MethodNode> methods, MethodNode om, String newDesc) {
		om.desc = newDesc;
		if (Modifier.isStatic(om.access))
			return 1;
		for (MethodNode m : methods) {
			m.desc = newDesc;
		}
		return methods.size();
		// Set<MethodNode> set2 = new HashSet<MethodNode>();
		// set2.addAll(findSame(((ClassStructure) first).supers, om));
		// set2.addAll(findSame(((ClassStructure) first).delegates, om));
		// for (MethodNode m : set2) {
		// m.desc = newDesc;
		// }
		// return set2.size();
	}

	private static Set<MethodNode> findSame(Collection<? extends ClassNode> classes, MethodNode om) {
		String hk = om.halfKey();
		Set<MethodNode> methods = new HashSet<MethodNode>();
		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				if (!Modifier.isStatic(m.access) && m.halfKey().equals(hk)) {
					methods.add(m);
				}
			}
		}
		return methods;
	}

	private static int fixCalls(List<? extends ClassNode> classes, Set<String> keys, String newDesc) {
		int count = 0;
		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						if (keys.contains(min.key())) {
							min.desc = newDesc;
							m.instructions.insertBefore(min, new InsnNode(POP));
							count++;
						}
					}
				}
			}
		}
		return count;
	}

	private static Set<MethodNode> buildValidTree(MethodNode m, int targetVar) {
		if (!isDummyParametered(m, targetVar))
			return null;
		if (Modifier.isStatic(m.access)) {
			Set<MethodNode> methods = new HashSet<MethodNode>();
			methods.add(m);
			return methods;
		}

		Set<MethodNode> methods = findAllVirtualMethods((ClassStructure) m.owner, m);
		boolean b = validAll(methods, targetVar);
		if (b)
			return methods;
		return null;
	}

	private static boolean validAll(Set<MethodNode> methods, int targetVar) {
		for (MethodNode m : methods) {
			if (!isDummyParametered(m, targetVar))
				return false;
		}
		return true;
	}

	private static Set<MethodNode> findAllVirtualMethods(ClassStructure cn, MethodNode m) {
		Set<MethodNode> totalSet = new HashSet<MethodNode>();
		String halfKey = m.halfKey();
		// totalSet.add(m);
		totalSet.addAll(findValidVirtualMethods(cn.supers, halfKey));
		// if (!Modifier.isFinal(m.access))
		totalSet.addAll(findValidVirtualMethods(cn.delegates, halfKey));
		return totalSet;
	}

	private static Set<MethodNode> findValidVirtualMethods(Collection<ClassStructure> classes, String halfKey) {
		Set<MethodNode> set = new HashSet<MethodNode>();
		for (ClassStructure cn : classes) {
			for (MethodNode m : cn.methods) {
				if (!Modifier.isStatic(m.access) && m.halfKey().equals(halfKey)) {
					set.add(m);
				}
			}
		}
		return set;
	}

	private static String recreateDesc(Type[] args, Type ret) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		Iterator<Type> it = Arrays.asList(args).iterator();
		while (it.hasNext()) {
			Type t = it.next();
			if (it.hasNext()) {
				sb.append(t.getDescriptor());
			}
		}
		sb.append(")");
		sb.append(ret.getDescriptor());
		return sb.toString();
	}

	private static boolean isDummyParametered(MethodNode m, int targetVar) {
		return looksLikeDummy(m) && countVars(m, targetVar) == 0;
	}

	private static int countVars(MethodNode m, int target) {
		int count = 0;
		for (AbstractInsnNode ain : m.instructions.toArray()) {
			if (ain instanceof VarInsnNode) {
				int var = ((VarInsnNode) ain).var;
				if (var == target)
					count++;
			} else if (ain instanceof IincInsnNode) {
				int var = ((IincInsnNode) ain).var;
				if (var == target)
					count++;
			}
		}
		return count;
	}

	private static boolean looksLikeDummy(MethodNode m) {
		if (m.name.length() > 2)
			return false;
		Type[] args = Type.getArgumentTypes(m.desc);
		if (args.length == 0)
			return false;
		return InstructionUtil.isPossibleDummy(args[args.length - 1].getDescriptor());
	}
}