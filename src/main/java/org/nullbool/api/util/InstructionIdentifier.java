package org.nullbool.api.util;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

public class InstructionIdentifier {

	private List<String> instList;
	private List<String> instCleanList;

	public InstructionIdentifier(final AbstractInsnNode... instructions) {
		try {
			this.instList = new LinkedList<String>();
			this.instCleanList = new LinkedList<String>();
			for (AbstractInsnNode instruction : instructions) {
				if(instruction.getOpcode() == -1)
					continue;
				String name = Printer.OPCODES[instruction.getOpcode()].toLowerCase();
				String extra = "";
				if (instruction instanceof FieldInsnNode) {
					FieldInsnNode f = ((FieldInsnNode) instruction);
					extra = " " + f.owner + "." + f.name + " " + f.desc;
				}

				if (instruction instanceof MethodInsnNode) {
					MethodInsnNode m = ((MethodInsnNode) instruction);
					extra = " " + m.owner + "." + m.name + " " + m.desc;
				}

				if (instruction instanceof VarInsnNode) {
					VarInsnNode m = ((VarInsnNode) instruction);
					extra = " " + String.valueOf(m.var);
				}

				if (instruction instanceof LdcInsnNode) {
					LdcInsnNode m = ((LdcInsnNode) instruction);
					extra = " " + String.valueOf(m.cst);
				}

				if (instruction instanceof IntInsnNode) {
					IntInsnNode m = ((IntInsnNode) instruction);
					extra = " " + String.valueOf(m.operand);
				}

				if (instruction instanceof TypeInsnNode) {
					TypeInsnNode m = ((TypeInsnNode) instruction);
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
