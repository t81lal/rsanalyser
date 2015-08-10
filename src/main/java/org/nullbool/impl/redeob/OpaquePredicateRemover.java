package org.nullbool.impl.redeob;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.util.MethodUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class OpaquePredicateRemover extends NodeVisitor {

	private final Map<MethodNode, Opaque> foundPredicates = new HashMap<MethodNode, Opaque>();
	private final Set<ComparisonPair> pairs = new HashSet<ComparisonPair>();
	private MethodNode method;
	private int targetVar;

	private int count, mcount = 0;
	private int mdiscard, typediscard = 0;
	
	public static Object[] getLastParameter(MethodNode m) {
		Type[] args = Type.getArgumentTypes(m.desc);
		if(args.length == 0)
			return null;
		
		// static   = args + 0
		// instance = args + 1

		
		// [last arg index, last arg type]
		return new Object[]{ args.length + (Modifier.isStatic(m.access) ? -1 : 0), args[args.length - 1]};
	}
	
	public static Object[] getLastDummyParameter(MethodNode m) {
		Object[] objs = getLastParameter(m);
		if(objs == null)
			return null;
		
		Type type = (Type) objs[1];
		if(!MethodUtil.isDummy(type))
			return null;
		
		return objs;
	}
	
	public boolean methodEnter(MethodNode m) {
		Object[] objs = getLastDummyParameter(m);
		if(objs == null)
			return false;

		/* void methodEnter(MethodNode m, int dummy)   <- index 2
		 * static methodEnter(MethodNode m, int dummy) <- index 1 */
		targetVar   = (int) objs[0];
		method      = m;

		return true;
	}

	@Override
	public void visitJump(JumpNode jn) {
		if(/*jn.method().key().equals("aj.u(IS)V") && */ jn.opcode() != GOTO && jn.children() == 2) {
			// eg.
			// iload2
			// sipush 1338
			NumberNode nn   = jn.firstNumber();
			VariableNode vn = jn.firstVariable();

			if(nn != null && vn != null && vn.var() == targetVar) {
				Jump jump = new Jump(jn.insn(), nn.insn(), vn.insn());
				ComparisonPair pair = new ComparisonPair(nn.number(), jump);

				pairs.add(pair);
			}
		}
	}

	public void methodExit() {
		if(pairs.size() > 0) {
			if(valid(pairs)) {
				/* iload4
				 * ldc 1797324181 (java.lang.Integer)
				 * if_icmpeq L3
				 * new java/lang/IllegalStateException
				 * dup
				 * invokespecial java/lang/IllegalStateException <init>(()V);
				 * athrow
				 */
				Map<ComparisonPair, List<AbstractInsnNode>> map = new HashMap<ComparisonPair, List<AbstractInsnNode>>();
				boolean b = false;

				for(ComparisonPair pair : pairs) {
					List<AbstractInsnNode> block = block(pair);
					
					/* if(method.key().startsWith("fz.o")) {
						System.out.println("================");
						for(AbstractInsnNode ain : block) {
							System.out.println(ain);
						}
					} */
					
					if(block == null) {
						b = true;
						break;
					}
					//TODO: Account for meta instructions.
					if(block.size() == 1) {
						if(block.get(0).opcode() != RETURN) {
							b = true;
							break;
						}
					} else {
						if(block.get(block.size() - 1).opcode() != ATHROW) {
							b = true;
							break;
						} else {
							AbstractInsnNode t = block.get(0);
							if(t instanceof TypeInsnNode) {
								TypeInsnNode tin = (TypeInsnNode) t;
								if(!tin.desc.equals("java/lang/IllegalStateException")) {
									b = true;
									break;
								}
							} else {
								b = true;
								break;
							}
						}
					}

					map.put(pair, block);
					//System.out.println(block);
				}

				if(!b) {
					//System.out.printf("%s (%b) [%d] is valid.%n", method.key(), Modifier.isStatic(method.access), targetVar);

					for(Entry<ComparisonPair, List<AbstractInsnNode>> e : map.entrySet()) {
						Jump jump = e.getKey().jump;
						
						if(!foundPredicates.containsKey(jump.jin.method)) {
							foundPredicates.put(jump.jin.method, new Opaque(jump.jin.opcode(), e.getKey().num));
						}
						
						/* Redirect the false jump location of the jump and force it
						 * to the target. */
						method.instructions.insert(jump.jin, new JumpInsnNode(GOTO, jump.jin.label));
						method.instructions.remove(jump.jin);
						
						for(AbstractInsnNode a : jump.insns) {
							method.instructions.remove(a);
						}

						for(AbstractInsnNode a : e.getValue()) {
							method.instructions.remove(a);
						}
						count++;
					}
					mcount++;
				} else {
					//if(Context.current().getFlags().getOrDefault("basicout", true))
					//	System.out.println("OpaquePredicateRemover.methodExit(): " + method);
					typediscard++;
				}
			} else {
				//if(Context.current().getFlags().getOrDefault("basicout", true))
				//	System.err.println("OpaquePredicateRemover.methodExit(): " + method);
				mdiscard++;
			}
		}

		pairs.clear();
		method    = null;
		targetVar = 0;
	}
	
	public Opaque find(MethodNode m) {
		return foundPredicates.get(m);
	}

	private static List<AbstractInsnNode> block(ComparisonPair p) {
		List<AbstractInsnNode> ains = new ArrayList<AbstractInsnNode>();
		AbstractInsnNode ain = p.jump.jin.getNext();
		while(true) {
			if(ain == null)
				return null;

			ains.add(ain);

			if(ain.opcode() == ATHROW || ain.opcode() == RETURN) {
				return ains;
			} else if(ain.type() == AbstractInsnNode.JUMP_INSN || ain.type() == AbstractInsnNode.LOOKUPSWITCH_INSN || ain.type() == AbstractInsnNode.TABLESWITCH_INSN) {
				return null;
			}

			ain = ain.getNext();
		}
	}

	private static boolean valid(Set<ComparisonPair> psets) {
		int num = -1;
		int jop = -1;
		/* Check to see if the comparison opcodes and the
		 * number being compared is the same. (we need to 
		 * make sure that the parameter is actually a 
		 * valid opaque). */
		for(ComparisonPair p : psets) {
			if(num == -1) {
				num = p.num;
			} else if(num != p.num) {
				return false;
			}

			if(jop == -1) {
				jop = p.jump.jin.opcode();
			} else if(p.jump.jin.opcode() != jop) {
				return false;
			}
		}
		return true;
	}

	private static class ComparisonPair {
		private int num;
		private Jump jump;

		public ComparisonPair(int num, Jump jump) {
			this.num  = num;
			this.jump = jump;
		}
	}

	private static class Jump {
		private final JumpInsnNode jin;
		private final AbstractInsnNode[] insns;

		public Jump(JumpInsnNode jin, AbstractInsnNode... insns) {
			this.jin = jin;
			this.insns = insns;
		}
	}
	
	public static class Opaque {
		private final int opcode;
		private final int num;
		
		public Opaque(int opcode, int num) {
			this.opcode = opcode;
			this.num = num;
		}

		public int getOpcode() {
			return opcode;
		}

		public int getNum() {
			return num;
		}
	}
}