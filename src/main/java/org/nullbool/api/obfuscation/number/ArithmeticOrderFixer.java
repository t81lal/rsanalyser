package org.nullbool.api.obfuscation.number;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 29 May 2015
 */
public class ArithmeticOrderFixer extends NodeVisitor {
	
	@Override
	public void visitOperation(ArithmeticNode expr) {

	}
}