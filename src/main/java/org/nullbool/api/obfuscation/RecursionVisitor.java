package org.nullbool.api.obfuscation;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class RecursionVisitor extends NodeVisitor {

	@Override
	public void visitMethod(MethodMemberNode mmn) {
		super.visitMethod(mmn);

		MethodInsnNode min = mmn.min();
	}
}