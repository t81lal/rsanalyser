package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.util.MethodUtil;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 30 May 2015
 */
public class OpaquePredicateRemover extends NodeVisitor {

	private final Set<ComparisonPair> pairs = new HashSet<ComparisonPair>();
	private MethodNode method;
	private int targetVar;

	private int count;

	public boolean methodEnter(MethodNode m) {
		Object[] objs = MethodUtil.getLastDummyParameter(m);
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
				ComparisonPair pair = new ComparisonPair(nn.number(), vn.var(), jump);

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
					if(block == null) {
						b = true;
						break;
					}

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
					}
					count++;
				}
			}
		}

		pairs.clear();
		method    = null;
		targetVar = 0;
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

	public void output() {
		System.err.printf("Removed %d opaque predicates.%n", count);
	}

	private static class ComparisonPair {
		private int num;
		private int var;
		private Jump jump;

		public ComparisonPair(int num, int var, Jump jump) {
			this.num  = num;
			this.var  = var;
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
}