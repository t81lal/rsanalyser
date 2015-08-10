package org.nullbool.impl.redeob;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ComparisonReorderer extends NodeVisitor {

	private final Set<OperandSwap> swaps = new HashSet<OperandSwap>();

	@Override
	public void visitJump(JumpNode jn) {   
		/*
		 * aconst_null
		 * getstatic Client.jz:Widget
		 * if_acmpeq L10
		 * 
		 *     to
		 * 
		 * getstatic Client.jz:Widget
		 * aconst_null
		 */
		AbstractNode first = jn.child(0);
		if(jn.opcode() == IF_ACMPEQ || jn.opcode() == IF_ACMPNE) {
			if(first.opcode() == ACONST_NULL) {
				OperandSwap swap = new OperandSwap(first.method(), new AbstractInsnNode[]{first.insn(), jn.child(1).insn()}, OperandSwapType.NULL);
				swaps.add(swap);
			}
		} else if(jn.opcode() == IF_ICMPEQ || jn.opcode() == IF_ICMPNE) {
			NumberNode nn = jn.firstNumber();
			if(nn != null && nn == first) { // ref check
				OperandSwap swap = new OperandSwap(first.method(), new AbstractInsnNode[]{first.insn(), jn.child(1).insn()}, OperandSwapType.CST);
				swaps.add(swap);
			}
		}/* else if(jn.opcode() == IFNONNULL) {
			*
			 * L1: aconst_null
			 *     ifnonnull L3
			 * L2: do stuff
			 * L3: thing thing
			 * 
			 *  if(null != null)
			 *      thing thing
			 *  else
			 *      do stuff
			 *  
			 *  
			 *  L1: aconst_null
			 *      ifnull L2
			 *  
			 *
			LabelNode target = jn.insn().label;
			
		}*/
	}

	public void output() {
		int n = 0;
		int c = 0;
		for(OperandSwap swap : swaps) {
			/* Remove the aconst_null and add it after. */
			swap.method.instructions.remove(swap.insns[0]);
			swap.method.instructions.insert(swap.insns[1], swap.insns[0]);
			
			if(swap.type == OperandSwapType.NULL) {
				n++;
			} else {
				c++;
			}
		}
		
		swaps.clear();
	}

	public static class OperandSwap {
		private final MethodNode method;
		private final AbstractInsnNode[] insns;
		private final OperandSwapType type;

		public OperandSwap(MethodNode method, AbstractInsnNode[] insns, OperandSwapType type) {
			this.method = method;
			this.insns = insns;
			this.type = type;
		}
	}
	
	public static enum OperandSwapType {
		NULL, CST;
	}
}