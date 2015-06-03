package org.nullbool.api.obfuscation;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Swaps instructions to change null checks as 
 * Jagex's obfuscator often swaps a null check such as <br>
 * 
 * <code>if(obj != null)</code> <br>
 * to <br>
 * <code>if(null != obj)</code> <br>
 * 
 * <p>
 * 
 * We do this as it makes analysis a bit easier.
 * </p>
 * 
 * 
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class NullCheckFixer extends NodeVisitor {

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
		if(jn.opcode() == IF_ACMPEQ || jn.opcode() == IF_ACMPNE) {
			AbstractNode first = jn.child(0);
			if(first.opcode() == ACONST_NULL) {
				OperandSwap swap = new OperandSwap(first.method(), new AbstractInsnNode[]{first.insn(), jn.child(1).insn()});
				swaps.add(swap);
			}
		}
	}

	public void output() {
		for(OperandSwap swap : swaps) {
			/* Remove the aconst_null and add it after. */
			swap.method.instructions.remove(swap.insns[0]);
			swap.method.instructions.insert(swap.insns[1], swap.insns[0]);
		}
		
		System.err.printf("Swapped %d null check operands.%n", swaps.size());
		swaps.clear();
	}

	private static class OperandSwap {
		private final MethodNode method;
		private final AbstractInsnNode[] insns;

		public OperandSwap(MethodNode method, AbstractInsnNode[] insns) {
			this.method = method;
			this.insns = insns;
		}
	}
}