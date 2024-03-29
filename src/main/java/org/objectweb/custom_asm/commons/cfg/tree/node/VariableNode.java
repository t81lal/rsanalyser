package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 */
public class VariableNode extends AbstractNode {

	public VariableNode(NodeTree tree, AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}
	
	public boolean storing() {
		switch(opcode()) {
			case ASTORE:
			case ISTORE:
			case FSTORE:
			case DSTORE:
			case LSTORE:
				return true;
		}
		return false;
	}

	public int var() {
		return ((VarInsnNode) insn()).var;
	}
}
