package org.nullbool.api.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.util.Printer;

public class InstructionUtil implements Opcodes {

	public static AbstractInsnNode next(AbstractInsnNode ain) {
		if (!(ain instanceof LabelNode))
			return ain;
		while (ain != null && !(ain instanceof LabelNode)) {
			ain = ain.getNext();
		}
		return ain;
	}

	public static int resolve(AbstractInsnNode ain) {
		switch (ain.opcode()) {
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
				return ain.opcode() - 3;
			case BIPUSH:
			case SIPUSH:
				return ((IntInsnNode) ain).operand;
			case LDC:
				return (int) ((LdcInsnNode) ain).cst;
			default:
				throw new IllegalArgumentException(Printer.OPCODES[ain.opcode()]);
		}
	}

	public static boolean isPossibleDummy(String d) {
		return d.equals("I") || d.equals("B") || d.equals("S");
	}
}