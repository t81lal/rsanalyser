package org.nullbool.api.obfuscation;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public class RecursionVisitor extends NodeVisitor {

	@Override
	public void visitMethod(MethodMemberNode mmn) {
		super.visitMethod(mmn);

		MethodInsnNode min = mmn.min();
	}
}