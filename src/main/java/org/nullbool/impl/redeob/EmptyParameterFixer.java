package org.nullbool.impl.redeob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.InheritedMethodMap;
import org.nullbool.api.obfuscation.refactor.MethodCache;
import org.nullbool.api.util.MethodUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class EmptyParameterFixer extends Visitor {

	private int startSize, endSize;
	private int callnames, unchanged, nulls;
	private final List<MethodNode> skipped = new ArrayList<MethodNode>();
	private final Set<MethodNode> changed = new HashSet<MethodNode>();

	@SuppressWarnings("unchecked")
	@Override
	public void visit(JarContents<? extends ClassNode> contents) {

		Map<MethodNode, String> map = new HashMap<MethodNode, String>();

		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				if (m.name.equals("<init>") || m.desc.equals("<clinit>"))
					continue;
				Object[] objs = MethodUtil.getLastDummyParameter(m);
				if (objs == null)
					continue;

				if (m.name.length() > 2) {
					skipped.add(m);
					continue;
				}

				int targetVar = (int) objs[0];
				// System.out.printf("%s (%b) -> %d.%n", m, Modifier.isStatic(m.access), targetVar);
				if (isUnused(m, targetVar)) {
					String newDesc = newDesc(m.desc);
					// fixes.put(m.key(), newDesc);
					map.put(m, newDesc);
					// System.out.println(m.desc + " -> " + newDesc);
				}
			}
		}

		Collection<ClassNode> classes = (Collection<ClassNode>) contents.getClassContents();
		ClassTree tree = new ClassTree(classes);
		InheritedMethodMap mmp = new InheritedMethodMap(tree, true);
		MethodCache cache = new MethodCache(classes);

		startSize = map.size();

		/*
		 * We have to validate the methods. Since a method can be overridden or inherited, before we change the description of one method, we have to
		 * check the super and delegate methods to verify that they also have their last parameter as a dummy parameter.
		 */
		Set<MethodNode> invalid = new HashSet<MethodNode>();
		Collection<MethodNode> methods = map.keySet();
		for (MethodNode m : methods) {
			Set<MethodNode> ms = mmp.getData(m).getAggregates();
			if (ms.size() > 0) {
				/*
				 * Since we've gone through all the classes already and found the dummy parameter methods, if any of the aggregate methods aren't in
				 * the collected set, then we can't reduce any of them.
				 */
				add(invalid, m, ms);
			}
		}

		// FIXME: Is something going wrong?

		/*
		 * If we want a deob for src code, then we don't want to be aggressive as we won't get recompilable code (duplicate parameter methods) but for
		 * analysis, it's fine.
		 */
		for (Entry<MethodNode, String> e : map.entrySet()) {
			MethodNode m = e.getKey();
			String newKey = calculateParamKey(m.name, e.getValue());
			Set<MethodNode> ms = mmp.getData(m).getAggregates();

			Set<MethodNode> collisions = checkCollisions(map, m, newKey);
			if (!collisions.isEmpty()) {
				add(invalid, m, collisions, ms);
				// continue topFor;
			}

			if (ms.size() > 0) {
				for (MethodNode m1 : ms) {
					/* Collision, don't rename any of the methods. */
					collisions = checkCollisions(map, m1, newKey);
					if (!collisions.isEmpty()) {
						add(invalid, m1, collisions, ms);
					}
				}
			}
		}

		for (MethodNode m : invalid) {
			map.remove(m);
		}

		endSize = map.size();

		invalid.clear();

		// System.out.printf("start=%d, end=%d.%n", startSize, map.size());
		fix(tree, mmp, cache, classes, map);
	}

	@SafeVarargs
	public static <T> void add(Set<T> set, T t, Set<T>... sets) {
		for (Set<T> s : sets) {
			set.addAll(s);
		}
		set.add(t);
	}

	/*
	 * 02/07/15, 9:01, we need to change this to only take parameters into account and not the return type since we can have two methods which both
	 * have the same parameters but different return types, failing the collision test.
	 */
	public String calculateParamKey(MethodNode m) {
		return calculateParamKey(m.name, m.desc);
	}

	public String calculateParamKey(String name, String desc) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("(");
		for (Type arg : Type.getArgumentTypes(desc)) {
			sb.append(arg.getDescriptor());
		}
		sb.append(")");
		return sb.toString();
	}

	private Set<MethodNode> checkCollisions2(Map<MethodNode, String> remapped, MethodNode m, String newParamKey) {

		System.out.println("Running for " + m.key());
		System.out.println("NewParamkey= " + newParamKey);

		Set<MethodNode> set = new HashSet<MethodNode>();
		for (MethodNode m2 : m.owner.methods) {
			String paramKey = calculateParamKey(m2);
			if (paramKey.equals(newParamKey) && m2 != m) {
				System.out.printf("Match for %s: %s.%n", paramKey, m2.key());
				set.add(m2);
			}

			String remappedDesc = remapped.get(m2);
			if (remappedDesc != null) {
				String remappedKey = calculateParamKey(m2.name, remappedDesc);
				if (remappedKey.equals(newParamKey) && m2 != m) {
					System.out.printf("Match0 for %s: %s.%n", remappedKey, m2.key());
					set.add(m2);
				}
			}

		}
		return set;
	}

	private Set<MethodNode> checkCollisions(Map<MethodNode, String> remapped, MethodNode m, String newParamKey) {
		// if(m.owner.name.equals("s") && m.name.equals("n")) {
		// return checkCollisions2(remapped, m, newParamKey);
		// }

		Set<MethodNode> set = new HashSet<MethodNode>();
		for (MethodNode m2 : m.owner.methods) {
			String paramKey = calculateParamKey(m2);
			if (paramKey.equals(newParamKey) && m2 != m) {
				set.add(m2);
			}

			String remappedDesc = remapped.get(m2);
			if (remappedDesc != null) {
				String remappedKey = calculateParamKey(m2.name, remappedDesc);
				if (remappedKey.equals(newParamKey) && m2 != m) {
					set.add(m2);
				}
			}

		}
		return set;
	}

	private void fix(ClassTree tree, InheritedMethodMap mmp, MethodCache cache, Collection<ClassNode> classes, Map<MethodNode, String> map) {
		/*
		 * We no longer need to do this (counting) due to the prevalidation done above which proves that all of the aggregate methods of each of the
		 * methods in the map is actually a dummy parameter method.
		 * 
		 * 
		 * int count1 = 0;
		 * 
		 * for(Entry<MethodNode, String> e : map.entrySet()) { MethodNode m = e.getKey(); Set<MethodNode> ms = mmp.getData(m).getAggregates();
		 * 
		 * m.desc = e.getValue();
		 * 
		 * for(MethodNode _m : ms) { _m.desc = e.getValue(); }
		 * 
		 * count1 += (ms.size() + 1);
		 * 
		 * count1++; }
		 */

		for (Entry<MethodNode, String> e : map.entrySet()) {
			MethodNode mn = e.getKey();
			mn.desc = e.getValue();
			Set<MethodNode> agg = mmp.getData(e.getKey()).getAggregates();
			for (MethodNode m : agg) {
				m.desc = e.getValue();
			}

			changed.add(mn);
			changed.addAll(agg);
		}

		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						MethodNode oldm = findMethod(min, tree, cache);

						if (oldm == null && min.owner.lastIndexOf('/') == -1) {
							// System.out.printf("%s is null (%s).%n", min.key(), m.key());
							nulls++;
						} else if (oldm != null) {
							if (!min.desc.equals(oldm.desc)) {
								m.instructions.insertBefore(min, new InsnNode(POP));
								callnames++;
							} else {
								unchanged++;
							}
							min.desc = oldm.desc;
						}
					}
				}
			}
		}

		// System.err.printf("%d methods.%n", mcount);
		// System.err.printf("%d calls (%d).%n", callname, unchanged);
	}

	/**
	 * Resolves a method call to the method it is calling. If class A extends class B, then calling A.method() when the method is actually defined in
	 * B will be indirectly calling B.method(). In this case, there will be no method in A and so we have to work backwards through the class
	 * hierarchy to find it.
	 * 
	 * @param min
	 * @param tree
	 * @param cache
	 * @return
	 */
	private MethodNode findMethod(MethodInsnNode min, ClassTree tree, MethodCache cache) {
		MethodNode oldm = cache.get(min.owner, min.name, min.desc);
		if (oldm != null)
			return oldm;

		ClassNode cn = tree.getClass(min.owner);

		if (cn == null) {
			// System.out.println(min.owner + " is nulllll");
			return null;
		}

		for (ClassNode sup : tree.getSupers(cn)) {
			oldm = cache.get(sup.name, min.name, min.desc);
			if (oldm != null)
				return oldm;
		}

		return null;
	}

	/**
	 * Removes the last parameter in a methods descriptor (which must be a byte, short or int (B, S, I).
	 * 
	 * @param desc
	 * @return
	 */
	private String newDesc(String desc) {
		/*
		 * Since this method is only called for methods which have a valid dummy parameter type (I, B, S) and their length is 1, we take 1 away before
		 * the ')'.
		 * 
		 * 
		 * ([BIII)V ^ dummy parameter.
		 * 
		 * sub: ([BII aft: )V
		 * 
		 * final: ([BII)V
		 */

		int index = desc.indexOf(")");
		String sub = desc.substring(0, index - 1);
		String aft = desc.substring(index);
		return sub + aft;
	}

	/**
	 * Counts the amount of times that the targetVar is used and returns a boolean value depending on whether it was used or not.
	 * 
	 * @param m
	 * @param targetVar
	 * @return
	 */
	private boolean isUnused(MethodNode m, int targetVar) {
		// if(m.owner.name.equals("bf") && m.name.equals("j")) {
		// System.out.println(m.desc + " targ= " + targetVar + " " + Modifier.isStatic(m.access));
		// }
		int count = 0;
		for (AbstractInsnNode ain : m.instructions.toArray()) {
			if (ain instanceof VarInsnNode) {
				VarInsnNode vin = (VarInsnNode) ain;
				if (vin.var == targetVar) {
					count++;
				}
			} else if (ain instanceof IincInsnNode) {
				IincInsnNode inc = (IincInsnNode) ain;
				if (inc.var == targetVar) {
					count++;
				}
			}
		}

		// if(m.owner.name.equals("bf") && m.name.equals("j")) {
		// System.out.println("Couunt " + count);
		// }

		return count == 0;
	}

	public Set<MethodNode> getChanged() {
		return changed;
	}
}