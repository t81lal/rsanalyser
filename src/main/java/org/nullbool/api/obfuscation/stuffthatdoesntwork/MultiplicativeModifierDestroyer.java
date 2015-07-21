package org.nullbool.api.obfuscation.stuffthatdoesntwork;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 10 Jun 2015 19:38:13
 */
public class MultiplicativeModifierDestroyer extends NodeVisitor {

	@Override
	public void visitOperation(ArithmeticNode an) {

	}

	@Override
	public void visitField(FieldMemberNode f) {
		ArithmeticNode an = f.firstOperation();
		if(an != null) {
			
		}
	}
}