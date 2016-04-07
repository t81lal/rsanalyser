package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.TypeInsnNode;

/**
 * @author Tyler Sedlar
 */
public class TypeNode extends AbstractNode {

	public TypeNode(NodeTree tree, org.objectweb.custom_asm.tree.AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}

	public String type() {
		return ((TypeInsnNode) insn()).desc;
	}
}
