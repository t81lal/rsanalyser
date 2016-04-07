package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.LdcInsnNode;

/**
 * @author Tyler Sedlar
 */
public class ConstantNode extends AbstractNode {

	public ConstantNode(NodeTree tree, org.objectweb.custom_asm.tree.AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}

    @Override
    public LdcInsnNode insn() {
        return (LdcInsnNode) super.insn();
    }

	public Object cst() {
        return insn().cst;
	}
}
