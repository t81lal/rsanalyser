package org.nullbool.api.obfuscation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.InheritedMethodMap;
import org.nullbool.api.obfuscation.refactor.MethodCache;
import org.nullbool.api.util.MethodUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class EmptyParameterFixer extends Visitor {

	private int startSize, endSize;
	private int callnames, unchanged, nulls;
	
	@SuppressWarnings("unchecked")
	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		Map<MethodNode, String> map = new HashMap<MethodNode, String>();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.name.equals("<init>") || m.desc.equals("<clinit>")) 
					continue;
				Object[] objs = MethodUtil.getLastDummyParameter(m);
				if(objs == null)
					continue;
				int targetVar = (int) objs[0];
				if(isUnused(m, targetVar)) {
					String newDesc = newDesc(m.desc);
					//fixes.put(m.key(), newDesc);
					map.put(m, newDesc);
					//System.out.println(m.desc + " -> " + newDesc);
				}
			}
		}

		Collection<ClassNode> classes = (Collection<ClassNode>) contents.getClassContents();
		ClassTree tree = new ClassTree(classes);
		InheritedMethodMap mmp = new InheritedMethodMap(tree, true);
		MethodCache cache = new MethodCache(classes);

		startSize = map.size();

		/* We have to validate the methods. Since a method can be overridden or 
		 * inherited, before we change the description of one method, we have 
		 * to check the super and delegate methods to verify that they also have
		 * their last parameter as a dummy parameter. */
		Set<MethodNode> invalid = new HashSet<MethodNode>();
		Collection<MethodNode> methods = map.keySet();
		for(MethodNode m : methods) {
			Set<MethodNode> ms = mmp.getData(m).getAggregates();
			if(ms.size() > 0) {
				/* Since we've gone through all the classes already and
				 * found the dummy parameter methods, if any of the 
				 * aggregate methods aren't in the collected set, then
				 * we can't reduce any of them. */
				if(!methods.containsAll(ms)) {
					invalid.add(m);
					for(MethodNode _m : ms) {
						invalid.add(_m);
					}
				}
			}
		}

		for(MethodNode m : invalid) {
			map.remove(m);
		}
		
		endSize = map.size();

		invalid.clear();

		//System.out.printf("start=%d, end=%d.%n", startSize, map.size());
		fix(tree, mmp, cache, classes, map);
	}

	private void fix(ClassTree tree, InheritedMethodMap mmp, MethodCache cache, Collection<ClassNode> classes, Map<MethodNode, String> map) {

		/* We no longer need to do this (counting) due to the 
		 * prevalidation done above which proves that all of the 
		 * aggregate  methods of each of the methods in the map 
		 * is actually a dummy parameter method.
		 * 
		 * 
		 * int count1 = 0;
		 *
		 * for(Entry<MethodNode, String> e : map.entrySet()) {
		 *	MethodNode m = e.getKey();
		 *	Set<MethodNode> ms = mmp.getData(m).getAggregates();
		 *
		 *	m.desc = e.getValue();
		 *	
		 *	for(MethodNode _m : ms) {
		 *		_m.desc = e.getValue();
		 *	}
		 *	
		 *	count1 += (ms.size() + 1);
		 *
		 *  count1++;
		 * }
		 */

		for(Entry<MethodNode, String> e : map.entrySet()) {
			e.getKey().desc = e.getValue();
			for(MethodNode m : mmp.getData(e.getKey()).getAggregates()) {
				m.desc = e.getValue();
			}
		}

		for(ClassNode cn : classes) {
			for(MethodNode m : cn.methods) {
				for(AbstractInsnNode ain : m.instructions.toArray()) {
					if(ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						MethodNode oldm = findMethod(min, tree, cache);

						if(oldm == null && min.owner.lastIndexOf('/') == -1) {
							//System.out.printf("%s is null (%s).%n", min.key(), m.key());
							nulls++;
						} else if(oldm != null) {
							if(!min.desc.equals(oldm.desc)) {
								callnames++;
								m.instructions.insertBefore(min, new InsnNode(POP));
							} else {
								unchanged++;
							}
							min.desc = oldm.desc;
						}
					}
				}
			}
		}

		//System.err.printf("%d methods.%n", mcount);
		//System.err.printf("%d calls (%d).%n", callname, unchanged);
	}

	private MethodNode findMethod(MethodInsnNode min, ClassTree tree, MethodCache cache) {
		MethodNode oldm = cache.get(min.owner, min.name, min.desc);
		if(oldm != null)
			return oldm;

		ClassNode cn = tree.getClass(min.owner);

		if(cn == null) {
			//System.out.println(min.owner + " is nulllll");
			return null;
		}

		for(ClassNode sup : tree.getSupers(cn)) {
			oldm = cache.get(sup.name, min.name, min.desc);
			if(oldm != null)
				return oldm;
		}

		return null;
	}

	private String newDesc(String desc) {
		/* Since this method is only called for methods which have a 
		 * valid dummy parameter type (I, B, S) and their length is 1,
		 * we take 1 away before the ')'. 
		 * 
		 * 
		 * ([BIII)V
		 *      ^ dummy parameter.
		 *      
		 *   sub: ([BII
		 *   aft: )V 
		 *   
		 * final: ([BII)V
		 */

		int index = desc.indexOf(")");
		String sub = desc.substring(0, index - 1);
		String aft = desc.substring(index);
		return sub + aft;
	}

	private boolean isUnused(MethodNode m, int targetVar) {
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
		return count == 0;
	}
	
	public void output() {
		System.err.println("Running empty parameter fixer.");
		System.out.printf("   map.start=%d, map.end=%d.%n", startSize, endSize);
		System.out.printf("   %d empty parameter methods changed, %d calls (%d nulls).%n", callnames, unchanged, nulls);
	}
}