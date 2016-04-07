package org.objectweb.custom_asm.commons.cfg.tree;

import java.util.Arrays;

import org.objectweb.custom_asm.commons.cfg.Block;
import org.objectweb.custom_asm.commons.cfg.tree.node.AbstractNode;

/**
 * @author Tyler Sedlar
 */
public class NodeTree extends AbstractNode {

    private final org.objectweb.custom_asm.tree.MethodNode mn;

    public NodeTree(org.objectweb.custom_asm.tree.MethodNode mn) {
        super(null, null, -1, -1);
        this.mn = mn;
    }

    public NodeTree(Block block) {
        this(block.owner);
    }

    @Override
	public org.objectweb.custom_asm.tree.MethodNode method() {
        return mn;
    }

    @Override
	public void accept(NodeVisitor nv) {
        if (!nv.validate()) return;
        nv.visitCode();
        for (AbstractNode node : this)
            accept(nv, node);
        nv.visitEnd();
    }

    private void accept(NodeVisitor nv, AbstractNode n) {
        if (!nv.validate()) return;
        n.accept(nv);
        for (AbstractNode node : n)
            accept(nv, node);
    }

    @Override
	public org.objectweb.custom_asm.tree.AbstractInsnNode[] collapse() {
        org.objectweb.custom_asm.tree.AbstractInsnNode[] instructions = super.collapse();
        int i = instructions.length > 1 && instructions[instructions.length - 2].getType() == org.objectweb.custom_asm.tree.AbstractInsnNode.LABEL ? 2 : 1;
        return Arrays.copyOf(instructions, instructions.length - i);
    }
}