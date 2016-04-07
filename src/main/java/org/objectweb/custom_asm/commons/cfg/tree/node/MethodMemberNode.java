package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;

/**
 * @author Tyler Sedlar
 */
public class MethodMemberNode extends ReferenceNode {

	public MethodMemberNode(NodeTree tree, org.objectweb.custom_asm.tree.AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}

    public org.objectweb.custom_asm.tree.MethodInsnNode min() {
        return (org.objectweb.custom_asm.tree.MethodInsnNode) insn();
    }
}
