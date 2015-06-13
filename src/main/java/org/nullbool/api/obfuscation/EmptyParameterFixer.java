package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.Context;
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
 * <p>
 * A transformer that finds dummy parameters on methods that are
 * inserted by Jagex's obfuscater. Strict parameter validation
 * is done to ensure that the parameter is indeed a dummy and
 * is not actually a real parameter.
 * </p>
 * 
 * <p>
 * Firstly all of the methods in the gamepack are checked to see if
 * their last parameter is a either a byte, short or integer as these
 * are currently the only dummy parameter types that the obfuscater
 * adds.
 * </p>
 * 
 * <p>
 * We then have to calculate the index of the local parameter in the
 * locals depending on whether the method is static or not. Then we
 * go through the method and check for inc instructions or var
 * instructions to see if the parameter is used. If the parameter is
 * used even once, we remove it from the Set. <br>
 * Note that some dummy
 * parameters are used to fulfil opaque predicate checks and so these
 * must be removed before calling this transformer.
 * </p>
 * 
 * <p>
 * Then a chain is built of all of the methods so that each method is
 * linked with it's parent method (if it is overridden). This allows us
 * to check if the parameter is used in a super or subclass. If this
 * fails, the entire method chain is dropped and none of those methods
 * are changed.
 * </p>
 * 
 * <p>
 * Then once we are sure that we can change the method description,
 * we remove the last parameter and then search the entire code for
 * calls to the old method. For each call we find, we change the 
 * description of the method and then add a POP before the call to 
 * make sure that the value that was previously passed is taken from
 * the stack before the method is called. <br>
 * </p>
 * 
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class EmptyParameterFixer extends Visitor {

	private int startSize, endSize;
	private int callnames, unchanged, nulls;
	private final List<MethodNode> skipped = new ArrayList<MethodNode>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		boolean print = Context.current().getFlags().getOrDefault("basicout", true);
		
		if(print) {
			System.err.println("Running empty parameter fixer.");
		}
		
		Map<MethodNode, String> map = new HashMap<MethodNode, String>();

		for(ClassNode cn : contents.getClassContents()) {
			for(MethodNode m : cn.methods) {
				if(m.name.equals("<init>") || m.desc.equals("<clinit>")) 
					continue;
				Object[] objs = MethodUtil.getLastDummyParameter(m);
				if(objs == null)
					continue;
				
				if(m.name.length() > 2) {
					skipped.add(m);
					continue;
				}
				
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
				add(invalid, m, ms);
			}
		}
		
		if(print) {
			System.out.printf("   Check 1: discarding %d methods.%n", invalid.size());
		}
		topFor: for(Entry<MethodNode, String> e : map.entrySet()) {
			MethodNode m = e.getKey();
			String newKey = m.name + e.getValue();
			
			Set<MethodNode> ms = mmp.getData(m).getAggregates();

			Set<MethodNode> collisions = checkCollisions(map, m, newKey);
			if(!collisions.isEmpty()) {
				add(invalid, m, collisions, ms);
				//continue topFor;
			}
			
			if(ms.size() > 0) {
				for(MethodNode m1 : ms) {
					/* Collision, don't rename any of the methods. */
					collisions = checkCollisions(map, m1, newKey);
					if(!collisions.isEmpty()) {
						add(invalid, m1, collisions, ms);
						//continue topFor;
					}
				}
			}
		}
		
		if(print) {
			System.out.printf("   Check 2: discarding %d methods.%n", invalid.size());
			System.out.println("   Got: " + invalid);
		}

		for(MethodNode m : invalid) {
			map.remove(m);
		}
		
		endSize = map.size();

		invalid.clear();

		//System.out.printf("start=%d, end=%d.%n", startSize, map.size());
		fix(tree, mmp, cache, classes, map);
		
		if(print) {
			System.out.printf("   Skipped methods: %s.%n", skipped);
			System.out.printf("   map.start=%d, map.end=%d.%n", startSize, endSize);
			System.out.printf("   %d empty parameter methods changed.%n", endSize);
			System.out.printf("   %d calls changed.%n", callnames);
			System.out.printf("   %d unchanged calls and %d nulls.%n", unchanged, nulls);
		}
	}
	
	@SafeVarargs
	public static <T> void add(Set<T> set, T t, Set<T>... sets) {
		for(Set<T> s : sets) {
			set.addAll(s);
		}
		set.add(t);
	}
	
	private Set<MethodNode> checkCollisions(Map<MethodNode, String> remapped, MethodNode m, String newHalfKey) {
		Set<MethodNode> set = new HashSet<MethodNode>();
		for(MethodNode m2 : m.owner.methods) {
			String halfKey = m2.halfKey();
			if(halfKey.equals(newHalfKey) && m2 != m) {
				set.add(m2);
			}
			
			
			String remappedDesc = remapped.get(m2);
			if(remappedDesc != null) {
				String remappedKey = m2.name + remappedDesc;
				if(remappedKey.equals(newHalfKey) && m2 != m) {
					set.add(m2);
				}
			}
			
		}
		return set;
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

		//System.err.printf("%d methods.%n", mcount);
		//System.err.printf("%d calls (%d).%n", callname, unchanged);
	}

	/**
	 * Resolves a method call to the method it is calling.
	 * If class A extends class B, then calling A.method()
	 * when the method is actually defined in B will be 
	 * indirectly calling B.method(). In this case, there
	 * will be no method in A and so we have to work backwards
	 * through the class hierarchy to find it.
	 * 
	 * @param min
	 * @param tree
	 * @param cache
	 * @return
	 */
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

	/**
	 * Removes the last parameter in a methods descriptor 
	 * (which must be a byte, short or int (B, S, I).
	 * 
	 * @param desc
	 * @return
	 */
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

	/**
	 * Counts the amount of times that the targetVar is used and
	 * returns a boolean value depending on whether it was used
	 * or not.
	 * 
	 * @param m
	 * @param targetVar
	 * @return
	 */
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
}