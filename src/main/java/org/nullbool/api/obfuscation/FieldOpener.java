package org.nullbool.api.obfuscation;

import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * Edits class, field and method access modifiers to make
 * them non-final and public.
 * 
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015 22:28:34 (actually before this)
 */
public class FieldOpener extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		for(ClassNode cn : contents.getClassContents()){
			for(FieldNode f : cn.fields){
				f.access &= ~ACC_FINAL;
				f.access &= ~ACC_PRIVATE;
				f.access &= ~ACC_PROTECTED;
				
				f.access |= ACC_PUBLIC;
			}
		}
	}
	
	public static void main(String[] args) {
		int acc = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		System.out.println(acc + "  " + Integer.toBinaryString(acc));
		acc &= ~Opcodes.ACC_PUBLIC;
		System.out.println(acc + "  " + Integer.toBinaryString(acc));
	}
}