package org.nullbool.api.util;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.custom_asm.util.Printer;

public class InstructionIdentifier {

	private List<String> instList;
	private List<String> instCleanList;

	public InstructionIdentifier(final org.objectweb.custom_asm.tree.AbstractInsnNode... instructions) {
		try {
			this.instList = new LinkedList<String>();
			this.instCleanList = new LinkedList<String>();
			for (org.objectweb.custom_asm.tree.AbstractInsnNode instruction : instructions) {
				if(instruction.getOpcode() == -1)
					continue;
				String name = Printer.OPCODES[instruction.getOpcode()].toLowerCase();
				String extra = "";
				if (instruction instanceof org.objectweb.custom_asm.tree.FieldInsnNode) {
					org.objectweb.custom_asm.tree.FieldInsnNode f = ((org.objectweb.custom_asm.tree.FieldInsnNode) instruction);
					extra = " " + f.owner + "." + f.name + " " + f.desc;
				}

				if (instruction instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
					org.objectweb.custom_asm.tree.MethodInsnNode m = ((org.objectweb.custom_asm.tree.MethodInsnNode) instruction);
					extra = " " + m.owner + "." + m.name + " " + m.desc;
				}

				if (instruction instanceof org.objectweb.custom_asm.tree.VarInsnNode) {
					org.objectweb.custom_asm.tree.VarInsnNode m = ((org.objectweb.custom_asm.tree.VarInsnNode) instruction);
					extra = " " + String.valueOf(m.var);
				}

				if (instruction instanceof org.objectweb.custom_asm.tree.LdcInsnNode) {
					org.objectweb.custom_asm.tree.LdcInsnNode m = ((org.objectweb.custom_asm.tree.LdcInsnNode) instruction);
					extra = " " + String.valueOf(m.cst);
				}

				if (instruction instanceof org.objectweb.custom_asm.tree.IntInsnNode) {
					org.objectweb.custom_asm.tree.IntInsnNode m = ((org.objectweb.custom_asm.tree.IntInsnNode) instruction);
					extra = " " + String.valueOf(m.operand);
				}

				if (instruction instanceof org.objectweb.custom_asm.tree.TypeInsnNode) {
					org.objectweb.custom_asm.tree.TypeInsnNode m = ((org.objectweb.custom_asm.tree.TypeInsnNode) instruction);
					extra = " " + String.valueOf(m.desc);
				}

				this.instList.add(name + extra);
				this.instCleanList.add(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getInstList() {
		return instList;
	}

	public List<String> getInstCleanList() {
		return instCleanList;
	}
}
