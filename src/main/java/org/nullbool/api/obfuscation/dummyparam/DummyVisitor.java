package org.nullbool.api.obfuscation.dummyparam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.util.IntMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.topdank.byteengineer.commons.data.JarContents;

public class DummyVisitor extends Visitor {

	private static final List<Integer> CMP_INSNS = new ArrayList<Integer>();

	static {
		CMP_INSNS.add(IF_ICMPEQ);
		CMP_INSNS.add(IF_ICMPGE);
		CMP_INSNS.add(IF_ICMPGT);
		CMP_INSNS.add(IF_ICMPLE);
		CMP_INSNS.add(IF_ICMPLT);
		CMP_INSNS.add(IF_ICMPNE);
	}

	public DummyVisitor() {

	}

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		AtomicInteger c = new AtomicInteger();

		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				if (m.name.length() > 2)
					continue;
				Type[] args = Type.getArgumentTypes(m.desc);
				if (args == null || args.length == 0 || !isPossibleDummy(args[args.length - 1].getDescriptor()))
					continue;
				boolean isStatic = (m.access & ACC_STATIC) == ACC_STATIC;
				int targetVar = isStatic ? args.length - 1 : args.length;
				// key, val
				// key = number
				// val = count
				IntMap countMap = new IntMap();
				run(m, targetVar, new Actor() {
					@Override
					public void act(AbstractInsnNode ain) {
						countMap.inc(resolve(ain));
					}
				});

				// iload1
				// bipush 8
				// if_icmplt L7
				// new java/lang/IllegalStateException
				// dup
				// invokespecial java/lang/IllegalStateException <init>(()V);
				// athrow

				if (countMap.size() == 1) {
					run(m, targetVar, new Actor() {
						@Override
						public void act(AbstractInsnNode ain) {
							m.instructions.remove(ain.getNext().getNext().getNext().getNext().getNext());
							m.instructions.remove(ain.getNext().getNext().getNext().getNext());
							m.instructions.remove(ain.getNext().getNext().getNext());
							m.instructions.remove(ain.getNext().getNext());
							m.instructions.remove(ain.getNext());
							m.instructions.remove(ain.getPrevious());
							m.instructions.remove(ain);

							// System.out.println("Removed: " + m.key());
						}
					});
					c.incrementAndGet();
				}
				// int largestKey = countMap.getLargestKey();
				// if (largestKey != 0) {
				//
				// }
				// System.out.println("Dummy param for " + m.key() + " = " + largestKey);
			}
		}

		System.out.println("Changed: " + c);
	}

	private void run(MethodNode m, int targetVar, Actor actor) {
		for (AbstractInsnNode ain : m.instructions.toArray()) {
			if (ain.getOpcode() == NEW) {
				TypeInsnNode tin = (TypeInsnNode) ain;
				String desc = tin.desc;
				if (desc.equals("java/lang/IllegalStateException")) {
					AbstractInsnNode prev = ain.getPrevious();
					if (prev != null && CMP_INSNS.contains(prev.getOpcode())) {
						AbstractInsnNode numberNode = prev.getPrevious();
						if (numberNode != null) {
							AbstractInsnNode varNode = numberNode.getPrevious();
							if (varNode instanceof VarInsnNode) {
								int var = ((VarInsnNode) varNode).var;
								if (var == targetVar) {
									actor.act(numberNode);
								}
							}
						}
					}
				}
			}
		}
	}

	private static int resolve(AbstractInsnNode ain) {
		switch (ain.getOpcode()) {
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
				return ain.getOpcode() - 3;
			case BIPUSH:
			case SIPUSH:
				return ((IntInsnNode) ain).operand;
			case LDC:
				return (int) ((LdcInsnNode) ain).cst;
			default:
				throw new IllegalArgumentException(Printer.OPCODES[ain.getOpcode()]);
		}
	}

	private boolean isPossibleDummy(String d) {
		return d.equals("I") || d.equals("B") || d.equals("S");
	}

	public static abstract interface Actor {
		public abstract void act(AbstractInsnNode ain);
	}
}