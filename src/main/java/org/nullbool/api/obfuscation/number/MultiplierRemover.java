package org.nullbool.api.obfuscation.number;

import org.nullbool.api.obfuscation.Visitor;
import org.objectweb.asm.tree.ClassNode;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * @author Bibl (don't ban me pls)
 * @created 27 May 2015
 */
public class MultiplierRemover extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		for(ClassNode cn : contents.getClassContents()) {
			
		}
	}
}