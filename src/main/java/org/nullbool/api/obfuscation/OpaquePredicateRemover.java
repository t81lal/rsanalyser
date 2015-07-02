package org.nullbool.api.obfuscation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.Context;
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

/**
 * Jagex's Obfuscater (at least for a while) has been
 * notoriously inserting opaque predicates into the code. <br>
 * In short, these are checks or conditions that should always
 * equate to true and are usually done by checking a value that
 * if passed as a parameter. Therefore most people that want to
 * call a method must search for the value to pass to ensure
 * the predicates false code is not executed. It is currently not
 * known is Jagex care about these failures, however, the obfuscater
 * guarantees that any calls that it makes will never fail (unless
 * the call is inside a dummy method).
 * 
 * <p>
 * There are currently two (at least known) types of predicate actions
 * that are inserted by the obfuscater. Both involve a simple if
 * statement that check if an integer value, which is passed as a
 * parameter to a method is correct. <br>
 * One such action is throwing an IllegalStateException: <br>
 * <code>
 *	if(var5 >= 1389541124) {
 *		throw new IllegalStateException();
 *	}
 * </code> <br>
 * And the other is: <br>
 * <code>
 *	if(var5 >= 1389541124) {
 *		return;
 *	}
 * </code> <br>
 * 
 * Both of these increase code complexity and confuse someone
 * reading the code. It also masks the dummy parameter that
 * the method uses for the predicate value, making it look
 * as if the parameter is used for a legitimate purpose, when
 * in fact it can be removed.
 * </p>
 * 
 * <p>
 * Removing opaque predicates is relatively simple. The only opaque
 * check that is currently inserted is done by checking the value
 * of an argument passed to the method. The comparison instruction
 * is also the same for all checks in the method and the action code
 * is not randomised or shuffled.
 * </p>
 * 
 * <p>
 * We start by traversing through the AST to collate
 * comparison instructions that check the value of the last
 * parameter (if it is an integer value).
 * </p>
 * 
 * <p>
 * We then verify that all of the comparison instructions are
 * using the same opcode and are comparing the same number. Note that this
 * works now, but if the obfuscater becomes more sophisticated, we will
 * need to change this.
 * </p>
 * 
 * <p>
 * We then check the action of the predicates failure to check that it is
 * either a return instruction (or variant?) or if it throws an IllegalStateException.
 * </p>
 * 
 * <p>
 * Finally, we remove the operand loading instructions and the comparison
 * instruction, as well as the predicate fail case action instructions.
 * We then add a GOTO which jumps to the target of the old jump to correct
 * the flow. There is probably a better way to do this and there is most
 * certainly something that I'm missing, but this shouldn't be a problem
 * once the empty goto remover is fixed.
 * </p>
 * 
 * @author Bibl (don't ban me pls)
 * @created 30 May 2015
 */
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

	public void output() {
		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Removing Opaque Predicates.");
			System.out.printf("   Removed %d opaque predicates (%d methods).%n", count, mcount);
			System.out.printf("   %d method discards and %d type discards.%n", mdiscard, typediscard);
		}
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