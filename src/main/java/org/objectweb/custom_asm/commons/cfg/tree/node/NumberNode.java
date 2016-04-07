package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.Opcodes;

public class NumberNode extends AbstractNode {

	public NumberNode(NodeTree tree, org.objectweb.custom_asm.tree.AbstractInsnNode insn, int collapsed, int producing) {
		super(tree, insn, collapsed, producing);
	}

	public long longNumber() {
		org.objectweb.custom_asm.tree.AbstractInsnNode insn = insn();
		int op = insn.getOpcode();
		switch (op) {
			case Opcodes.NEWARRAY:
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH: {
				return ((IntInsnNode) insn).operand;
			}
			case Opcodes.ICONST_M1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5: {
				return op - Opcodes.ICONST_0;
			}
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1: {
				return op - Opcodes.LCONST_0;
			}
			case Opcodes.FCONST_0:
			case Opcodes.FCONST_1:
			case Opcodes.FCONST_2: {
				return op - Opcodes.FCONST_0;
			}
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1: {
				return op - Opcodes.DCONST_0;
			}
			case Opcodes.LDC: {
				Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) insn).cst;
				if (cst instanceof Number) {
					return ((Number)cst).longValue();
				}
			}
			default: {
				return -1;
			}
		}
	}

	public Class<?> type() {
		org.objectweb.custom_asm.tree.AbstractInsnNode insn = insn();
		int op = insn.getOpcode();
		switch (op) {
			case Opcodes.NEWARRAY:
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH:
			case Opcodes.ICONST_M1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5:
				return Integer.TYPE;
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1: {
				return Long.TYPE;
			}
			case Opcodes.FCONST_0:
			case Opcodes.FCONST_1:
			case Opcodes.FCONST_2:
				return Float.TYPE;
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1:
				return Double.TYPE;
			case Opcodes.LDC:
				Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) insn).cst;
				if(cst instanceof Integer) {
					return Integer.TYPE;
				} else if(cst instanceof Long) {
					return Long.TYPE;
				} else if(cst instanceof Float) {
					return Float.TYPE;
				} else if(cst instanceof Double) {
					return Double.TYPE;
				}
			default: {
				return null;
			}
		}
	}

	public int number() {
		org.objectweb.custom_asm.tree.AbstractInsnNode insn = insn();
		int op = insn.getOpcode();
		switch (op) {
			case Opcodes.NEWARRAY:
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH: {
				return ((IntInsnNode) insn).operand;
			}
			case Opcodes.ICONST_M1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5: {
				return op - Opcodes.ICONST_0;
			}
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1: {
				return op - Opcodes.LCONST_0;
			}
			case Opcodes.FCONST_0:
			case Opcodes.FCONST_1:
			case Opcodes.FCONST_2: {
				return op - Opcodes.FCONST_0;
			}
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1: {
				return op - Opcodes.DCONST_0;
			}
			case Opcodes.LDC: {
				Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) insn).cst;
				if (cst instanceof Number) {
					return ((Number) cst).intValue();
				}
			}
			default: {
				return -1;
			}
		}
	}

	public void setNumber(int number) {
		org.objectweb.custom_asm.tree.AbstractInsnNode ain = insn();
		if (ain instanceof IntInsnNode) {
			((IntInsnNode) insn()).operand = number;
			((IntInsnNode) ain).operand = number;
		} else if (ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode) {
			((org.objectweb.custom_asm.tree.LdcInsnNode) insn()).cst = number;
			((org.objectweb.custom_asm.tree.LdcInsnNode) ain).cst = number;
		}
	}

	public void setNumber(Number num) {
		org.objectweb.custom_asm.tree.AbstractInsnNode ain = insn();
		if(!(ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode)) {
			setInstruction(new org.objectweb.custom_asm.tree.LdcInsnNode(num));
		} else{
			((org.objectweb.custom_asm.tree.LdcInsnNode) ain).cst = num;
		}
	}

	@Override
	public String toString() {
		return insn().toString();
	}
}