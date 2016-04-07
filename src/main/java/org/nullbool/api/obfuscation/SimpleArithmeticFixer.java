package org.nullbool.api.obfuscation;

import java.util.HashSet;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 29 May 2015
 */
public class SimpleArithmeticFixer extends NodeVisitor {

	private final Set<InstructionSwap> inserts = new HashSet<InstructionSwap>();

	private int generalWtfs;
	private int addMins, subMins;
	
	private int complexAddSwitch, addSwitch = 0;
	private int simpleAddSwap, correctAdds = 0;

	private int complexSubSwitch;
	private int unswitchableSubs, correctSubs = 0;
	private int awtfs;

	private int multiplyByOne, swappedMultis, correctMultis, multiWtfs;

	@Override
	public void visitOperation(ArithmeticNode expr) {
		if(expr.children() != 2)
			return;

		AbstractNode a1 = expr.child(0);
		AbstractNode a2 = expr.child(1);
		if(a1 == null || a2 == null)
			return;

		NumberNode nn = expr.firstNumber();
		if(nn == null)
			return;

		if(a1 instanceof NumberNode && a2 instanceof NumberNode) {
			System.out.println("ArithmeticFixer2.visitOperation(): double const?");
			return;
		}
		
		if(a2.opcode() == -1) {
			generalWtfs++;
			System.out.printf("a2.opcode == -1, type=[%s].%n", a2.insn().getClass().getSimpleName());
			//if(Context.current().getFlags().getOrDefault("basicout", true))
			//	System.err.printf("   %s [%s, %d] [children=%d] at %s.%n", Printer.OPCODES[expr.opcode()], Printer.OPCODES[a1.opcode()], a2.opcode(), expr.children(), expr.method());
			return;
		}
		
		boolean fnum = a1 == nn;
		if(!fnum && !(a2 == nn)) {
			// if this operation isn't a const +-*/ something
			return;
		}

		/* Possible adding combinations :
		 *    variable + const   = variable + const  (don't reorder)
		 *    const + variable   = variable + const  (reorder)
		 *    
		 *    variable + -const  = variable - +const (switch operation)
		 *    -const + variable  = variable - +const (switch operation and reorder)
		 *    -variables are impossible (as far as I know)
		 *    
		 * Examples:
		 *    5  + 10  = 15
		 *    10 + 5   = 15
		 *    
		 *    5   + -10  =     5 - (+10)   = -5        = -5
		 *   -10  + 5    = (+10) - 5       = 5 - (+10) = -5
		 *    
		 * Note that there will (should) be one NumberNode.
		 * The other node could be either a FieldMemberNode
		 * or a VariableNode but in either case it doesn't
		 * matter because we cannot change the value of those.
		 * 
		 * Strategies:
		 *    We know that if there is a -const then the opcode
		 *    of the operation WILL be switched to a - (sub) and
		 *    the const's sign will be changed to a + (positive).
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * Possible subtracting combinations:
		 *    variable -  const   = variable - const (don't reorder)
		 *    variable - -const   = variable + const (merge and switch operation) 
		 *    
		 *     const - variable   =                  (can't switch?)
		 *    -const - variable   =                  (can't switch?)
		 *    
		 *    5 -  10  = -5
		 *    5 - -10  = 5 + 10 = 15
		 *    
		 *    10 - 5   =  5
		 *   -10 - 5   = -15
		 *    ^ the above seems to be unchangeable, however, I can only see this
		 *      being used in real calculations so it doesn't really matter.
		 *      
		 * Strategies:
		 *    We need to get rid of the - -const and replace it with
		 *    a (+const).
		 *    Then we just switch around the left and right of the operator
		 *    to get a cleaner expression.
		 *    
		 */
		if(expr.adding()) {
			/* Switching operation and operand:
			 * 
			 *  variable +   -const
			 *          and
			 *  -const   +   variable */
			Number val = constVal(nn.insn());

			if(val.intValue() == Integer.MIN_VALUE) {
				if(Context.current().getFlags().getOrDefault("basicout", true)) {
					addMins++;
					System.err.println("   SimpleArithmeticFixer.visitOperation(add)");
				}
				return;
			}

			if(shouldSwitchOperation(val)) {
				/* Switch the numbers sign and the opcode. */
				Number newVal = abs(val);
				nn.setNumber(newVal);
				expr.insn().setOpcode(newOpcode(ISUB, newVal));

				if(fnum) {
					/* -const   +   variable
					 *  =
					 * variable + -const
					 *  =
					 * variable - const 
					 * 
					 * 
					 * So if the constant comes first, we swap the order of operations
					 * by adding an InstructionSwap that will swap them after. */
					AbstractInsnNode a1ain = a1.insn();
					AbstractInsnNode a2ain = a2.insn();
					InstructionSwap insert = new InstructionSwap();
					insert.method = expr.method();
					insert.marker = a2ain;
					insert.insn   = a1ain;
					inserts.add(insert);
					complexAddSwitch++;
				} else {
					/* variable + -const 
					 *  =
					 * variable - +const 
					 * 
					 * If the constant comes second, we don't need to swap the
					 * operands and we've already changed the sign of the number
					 * and the opcode. */
					addSwitch++;
				}
			} else {
				/* Here the sign doesn't have to be changed nor does
				 * the operation but the order of the operands might. 
				 * 
				 * const + variable
				 *  =
				 * variable + const */

				if(fnum) {
					InstructionSwap swap = new InstructionSwap();
					swap.method = expr.method();
					swap.marker = a2.insn();
					swap.insn   = nn.insn();
					simpleAddSwap++;
				} else {
					// Already correct
					correctAdds++;
				}
			}
		} else if(expr.subtracting()) {
			Number val = constVal(nn.insn());

			if(val.intValue() == Integer.MIN_VALUE) {
				/* y = INTEGER.MIN_VALUE - x;
				 * y = x - Integer.MIN_VALUE;
				 * 
				 * when x = +ve
				 * 
				 * y = Integer.MIN_VALUE - (+x);
				 * y = (+x) - Integer.MIN_VALUE;
				 * 
				 * when x = -ve
				 * 
				 * y = Integer.MIN_VALUE - (-x);
				 *    so
				 * y = Integer.MIN_VALUE + x;
				 * 
				 * y = (-x) - Integer.MIN_VALUE;
				 * 
				 * 
				 */
				
				
				if(Context.current().getFlags().getOrDefault("basicout", true)) {
					subMins++;
					System.err.println("   SimpleArithmeticFixer.visitOperation(sub)");
				}
				return;
			}

			if(shouldSwitchOperation(val)) {
				if(fnum) {
					unswitchableSubs++;
				} else {
					/* variable - -const
					 *  =
					 * variable + +const
					 * 
					 * If the constant comes second, we don't need to swap the
					 * operands.
					 * 
					 * Switch the numbers sign and the opcode. */
					Number newVal = abs(val);
					nn.setNumber(newVal);
					expr.insn().setOpcode(newOpcode(IADD, newVal));
					complexSubSwitch++;
				}
			} else {
				/* This means we don't need to change the sign
				 * of the number but we might need to swap the 
				 * order of the operands. 
				 * 
				 * variable -  const
				 *  and
				 * const - variable 
				 * 
				 * Can we change these? */
				correctSubs++;
			}
		} else if(expr.multiplying()) {
			/* If the first number is the constant, we move it after the other 
			 * operand. */
			Class<?> type = nn.type();
			if((type.equals(Integer.TYPE)|| type.equals(Long.TYPE)) && nn.longNumber() == 1) {
				// MethodNode method = expr.method();
				// System.out.printf("%s : %s.%n", expr.insn(), first.insn());
				// method.instructions.remove(expr.insn());
				// method.instructions.remove(first.insn());
				multiplyByOne++;
			} else if(a1.equals(nn)) {
				if(a2.insn() instanceof FieldInsnNode || a2.insn() instanceof VarInsnNode) {
					InstructionSwap swap = new InstructionSwap();
					swap.method = expr.method();
					swap.insn   = a1.insn();
					swap.marker = a2.insn();
					inserts.add(swap);
					swappedMultis++;
				} else {
					multiWtfs++;
				}
			} else {
				correctMultis++;
			}
		}
	}

	private static Number abs(Number n) {
		if(n instanceof Integer) {
			return Math.abs(n.intValue());
		} else if(n instanceof Long) {
			return Math.abs(n.longValue());
		} else if(n instanceof Float) {
			return Math.abs(n.floatValue());
		} else if(n instanceof Double) {
			return Math.abs(n.doubleValue());
		} else {
			throw new RuntimeException("um...");
		}
	}

	private static int newOpcode(int base, Number val) {
		if(val instanceof Integer) {
			return base + 0;
		} else if(val instanceof Long) {
			return base + 1;
		} else if(val instanceof Float) {
			return base + 2;
		} else if(val instanceof Double) {
			return base + 3;
		}
		throw new RuntimeException("Wat.");
	}

	private static Number constVal(AbstractInsnNode ain) {
		if(ain instanceof LdcInsnNode) {
			LdcInsnNode ldc = (LdcInsnNode) ain;
			Object o = ldc.cst;
			if(o instanceof Number) {
				return (Number) o;
			} else {
				return 0;
			}
		}

		int num = InstructionUtil.resolve(ain);

		// returning 0 means we don't need to change it
		if(num == -1)
			return 0;

		return num;
	}

	private static boolean shouldSwitchOperation(Number num) {
		if(num.intValue() == 0)
			return false;
		return num.intValue() < 0 || num.longValue() < 0 || num.doubleValue() < 0 || num.floatValue() < 0;
	}

	public void output() {
		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.out.println();
		
		/* As the non constant operand of the operation may be calculated using more
		 * than 1 instruction, we use can a ghetto hack and instead of swapping the
		 * instructions by index, we simply add the constant after the other operands
		 * instruction(s). */
		for(InstructionSwap a : inserts) {
			//if(a.method.owner.name.equals("dh") && a.method.name.equals("s")) {
			//	System.out.println("ArithmeticFixer.output() " + a.insn.getClass() + " " + a.marker.getClass());
			//}
			a.method.instructions.remove(a.insn);
			a.method.instructions.insert(a.marker, a.insn);
		}

		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.out.printf("   Hit %d general wtfs...%n", generalWtfs);
			System.out.printf("   Found %d addition Integer.MIN_VALUE's.%n", addMins);
			System.out.printf("   Found %d subtraction Integer.MIN_VALUE's.%n", subMins);
			System.out.printf("   Switched %4d negative addition constants             (variable + -const).%n", addSwitch);
			System.out.printf("   Switched %4d complex negative addition constants     (-const + variable).%n", complexAddSwitch);
			System.out.printf("   Switched %4d simple operand orders                   (const + variable).%n", simpleAddSwap);
			System.out.printf("   Found    %4d already correct add operations          (variable + const).%n", correctAdds);
			System.out.println();
			System.out.printf("   Found    %4d unswitchable subtraction operations     (-const - variable).%n", unswitchableSubs);
			System.out.printf("   Switched %4d subtraction constant signs              (variable - -const).%n", complexSubSwitch);
			System.out.printf("   Found    %4d already correct subtraction operations  (variable - const).%n", correctSubs);
			System.out.printf("   Hit a few (%d) wtfs...%n", awtfs);
			System.out.println();
			System.out.printf("   Removed %4d redundant multiplications (*1)%n", multiplyByOne);
			System.out.printf("   Swapped  %4d constant multiplication expressions     (const * variable).%n", swappedMultis);
			System.out.printf("   Found    %4d preferable CME's                        (variable * const).%n", correctMultis);
			System.out.printf("   Hit a few (%d) wtfs...%n", multiWtfs);
		}
		
		inserts.clear();
	}

	private static class InstructionSwap {
		private MethodNode method;
		private AbstractInsnNode marker;
		private AbstractInsnNode insn;
	}
}