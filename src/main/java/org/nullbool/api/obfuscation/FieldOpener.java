package org.nullbool.api.obfuscation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.topdank.byteengineer.commons.data.JarContents;

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