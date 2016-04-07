package org.nullbool.api.obfuscation;

import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 12 Sep 2015 23:35:27
 */
public class ConstantAppropriator extends NodeVisitor {

	@Override
	public void visitOperation(ArithmeticNode an) {
		if(an.negating()) {
			
		}
	}
}