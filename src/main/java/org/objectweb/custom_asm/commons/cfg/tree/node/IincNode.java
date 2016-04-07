package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.IincInsnNode;

/**
 * @author Tyler Sedlar
 */
public class IincNode extends AbstractNode {

	public IincNode(NodeTree tree, AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}

	public int increment() {
		return ((IincInsnNode) insn()).incr;
	}

	public int var() {
		return ((IincInsnNode) insn()).var;
	}
}
