package org.nullbool.api.obfuscation.number;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 29 Jul 2015 00:42:28
 */
public class MultiRemover extends NodeVisitor {

	@Override
	public void visitOperation(ArithmeticNode an) {
		if(!an.multiplying() && !an.adding() && !an.subtracting())
			return;
		NumberNode cst = an.firstNumber();
		AbstractNode other = an.child(0);
		if(other == cst) {
			other = an.child(1);
		}
		
		if(cst != null) {
			
		}
	}
}