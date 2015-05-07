package org.nullbool.api.obfuscation.dummyparam;

import org.nullbool.api.obfuscation.Visitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class DummyParameterVisitor extends Visitor {

	public DummyParameterVisitor() {

	}

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		for (ClassNode cn : contents.getClassContents()) {
			for (MethodNode m : cn.methods) {
				if (isRecursive(m)) {
					System.out.printf("%s is recursive.%n", m.key());
				}
			}
		}
	}

	private boolean isRecursive(MethodNode m) {
		boolean isStatic = (m.access & ACC_STATIC) != ACC_STATIC;
		for (AbstractInsnNode ain : m.instructions.toArray()) {
			if (ain instanceof MethodInsnNode) {
				int opcode = ain.opcode();
				if (opcode == INVOKESTATIC && isStatic) {
					MethodInsnNode min = (MethodInsnNode) ain;
					if (min.owner.equals(m.owner.name) && min.name.equals(m.name) && min.desc.equals(m.desc))
						return true;
				} else if (!isStatic) {
					MethodInsnNode min = (MethodInsnNode) ain;
					if (min.owner.equals(m.owner.name) && min.name.equals(m.name) && min.desc.equals(m.desc))
						return true;
				}
			}
		}
		return false;
	}
}