package org.nullbool.api.obfuscation.dummyparam;

import static org.nullbool.api.util.InstructionUtil.isPossibleDummy;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.IntMap;
import org.nullbool.api.util.filtactor.Actor;
import org.nullbool.api.util.filtactor.Filter;
import org.nullbool.api.util.filtactor.IncActor;
import org.nullbool.api.util.filtactor.MethodFilter;
import org.nullbool.api.util.filtactor.OpaquePredicateFilter;
import org.nullbool.api.util.filtactor.RemovePredicateActor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.topdank.byteengineer.commons.data.JarContents;

public class OpaquePredicateVisitor extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		IntMap counter = new IntMap();
		RemovePredicateActor removeActor = new RemovePredicateActor(counter);

		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				if (m.name.length() > 2)
					continue;
				Type[] args = Type.getArgumentTypes(m.desc);
				if (args == null || args.length == 0 || !isPossibleDummy(args[args.length - 1].getDescriptor()))
					continue;
				boolean isStatic = (m.access & ACC_STATIC) == ACC_STATIC;
				int targetVar = isStatic ? args.length - 1 : args.length;
				Filter<AbstractInsnNode> filter = new OpaquePredicateFilter(targetVar);
				// key, val
				// key = number
				// val = count
				IntMap countMap = new IntMap();
				IncActor incActor = new IncActor(countMap);
				run(m, filter, incActor);

				// iload1
				// bipush 8
				// if_icmplt L7
				// new java/lang/IllegalStateException
				// dup
				// invokespecial java/lang/IllegalStateException <init>(()V);
				// athrow

				// length = 7

				// validates that in the tree none of the opaque parameters are anything other than opaque

				// check if the current method is legit first
				if (countMap.size() == 1) {
					// then do the supers and subs(unless static)
					// allow final as this may be at the top of the hierarchy tree
					if (!Modifier.isStatic(m.access)) {
						removeActor.setMethod(m);
						run(m, filter, removeActor);
					} else {
						Set<MethodNode> set = validate(m, filter);
						if (set != null) {
							removeActor.setMethod(m);
							run(m, filter, removeActor);
							for (MethodNode m1 : set) {
								removeActor.setMethod(m1);
								run(m1, filter, removeActor);
							}
						}
					}

					removeActor.setMethod(m);
					run(m, filter, removeActor);
				}
				/*
				 * R78:
				 * 	 Removed 1089 IF_ICMPNE types.
				 * 	 Removed 939 IF_ICMPLT types.
				 * 	 Removed 955 IF_ICMPGT types.
				 * 	 Removed 1356 IF_ICMPEQ types.
				 */
			}
		}

		// We don't get these as opaque comparison instructions
		// IF_ICMPGE
		// IF_ICMPLE

		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Opaque predicate remover");
			for (Entry<Integer, Integer> e : counter.entrySet()) {
				System.out.println("   Removed " + e.getValue() + " " + Printer.OPCODES[e.getKey()] + " types.");
			}
		}
	}

	private static Set<MethodNode> validate(MethodNode m, Filter<AbstractInsnNode> filter) {
		Filter<MethodNode> methodFilter = new MethodFilter(m);
		ClassStructure owner = (ClassStructure) m.owner;
		Set<MethodNode> set = validate(owner.supers, methodFilter, filter);
		if (set == null)
			return null;
		Set<MethodNode> set2 = validate(owner.delegates, methodFilter, filter);
		if (set2 == null)
			return null;
		set.addAll(set2);
		return set;
	}

	private static Set<MethodNode> validate(Collection<ClassStructure> classes, Filter<MethodNode> methodFilter, Filter<AbstractInsnNode> insnFilter) {
		IntMap countMap = new IntMap();
		IncActor actor = new IncActor(countMap);
		Set<MethodNode> set = new HashSet<MethodNode>();
		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				// statics aren't inherited, allow finals here because they can be the edge of the tree (at the top)
				if (Modifier.isStatic(m.access))
					continue;
				countMap.clear();
				if (methodFilter.accept(m) != null) {
					run(m, insnFilter, actor);
					if (countMap.size() != 1)
						return null;
					countMap.clear();
					set.add(m);
				}
			}
		}
		return set;
	}

	private static void run(MethodNode m, Filter<AbstractInsnNode> filter, Actor actor) {
		AbstractInsnNode[] insns = m.instructions.toArray();
		for (int i = 0; i < insns.length; i++) {
			AbstractInsnNode ain = insns[i];
			AbstractInsnNode ret = filter.accept(ain);
			if (ret != null)
				i += actor.act(ret);
		}
	}
}