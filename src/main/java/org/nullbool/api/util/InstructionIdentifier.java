package org.nullbool.api.util;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class InstructionIdentifier {

	private List<String> instList;
	private List<String> instCleanList;

	public InstructionIdentifier(final AbstractInsnNode... instructions) {
		try {
			String nm, iDesc = "";
			this.instList = new LinkedList<String>();
			this.instCleanList = new LinkedList<String>();
			for (AbstractInsnNode instruction : instructions) {
				if (instruction.getOpcode() != -1) {
					nm = Printer.OPCODES[instruction.getOpcode()].toLowerCase();
					if (instruction instanceof FieldInsnNode) {
						FieldInsnNode f = ((FieldInsnNode) instruction);
						iDesc = " " + f.owner + "." + f.name + " " + f.desc;
					}

					if (instruction instanceof MethodInsnNode) {
						MethodInsnNode m = ((MethodInsnNode) instruction);
						iDesc = " " + m.owner + "." + m.name + " " + m.desc;
					}

					if (instruction instanceof VarInsnNode) {
						VarInsnNode m = ((VarInsnNode) instruction);
						iDesc = " " + String.valueOf(m.var);
					}

					if (instruction instanceof LdcInsnNode) {
						LdcInsnNode m = ((LdcInsnNode) instruction);
						iDesc = " " + String.valueOf(m.cst);
					}

					if (instruction instanceof IntInsnNode) {
						IntInsnNode m = ((IntInsnNode) instruction);
						iDesc = " " + String.valueOf(m.operand);
					}

					this.instList.add(nm + iDesc);
					this.instCleanList.add(nm);
					iDesc = "";
				}
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
