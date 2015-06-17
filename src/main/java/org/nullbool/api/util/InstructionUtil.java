package org.nullbool.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public class InstructionUtil implements Opcodes {

//	IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, 
//	IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, 
//	IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
	
	public static boolean isConditional(int op) {
		//return CONDITIONAL_OPCODES.contains(op);
		return (op >= IFEQ && op <= IF_ACMPNE) || (op == IFNULL || op == IFNONNULL);
	}
	
	public static boolean isUnconditional(int op) {
		//return UNCONDITIONAL_OPCODES.contains(op);
		return op >= GOTO && op <= RET;
	}
	
	public static boolean isExit(int op) {
		//return EXIT_OPCODES.contains(op);
		return (op >= IRETURN && op <= RETURN) || op == ATHROW;
	}

	public static boolean isSwitch(int op) {
		//return SWITCH_OPCODES.contains(op);
		return op == TABLESWITCH || op == LOOKUPSWITCH;
	}
	
	public static void print(Collection<AbstractInsnNode[]> coll) {
		for (AbstractInsnNode[] ains : coll) {
		    System.out.println(Arrays.toString(ains));
		}
	}
	
	public static <T extends Collection<LabelNode>> T calcSwitchTargets(TableSwitchInsnNode tsin, T col) {
		col.add(tsin.dflt);
		col.addAll(tsin.labels);
		return col;
	}
	
	public static <T extends Collection<LabelNode>> T calcSwitchTargets(LookupSwitchInsnNode tsin, T col) {
		col.add(tsin.dflt);
		col.addAll(tsin.labels);
		return col;
	}
	
	public static List<LabelNode> calcSwitchTargets(TableSwitchInsnNode tsin) {
		List<LabelNode> list = new ArrayList<LabelNode>();
		list.add(tsin.dflt);
		list.addAll(tsin.labels);
		return list;
	}
	
	public static List<LabelNode> calcSwitchTargets(LookupSwitchInsnNode lsin) {
		List<LabelNode> list = new ArrayList<LabelNode>();
		list.add(lsin.dflt);
		list.addAll(lsin.labels);
		return list;
	}
	
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
				return -1;
//				throw new IllegalArgumentException(Printer.OPCODES[ain.opcode()]);
		}
	}

	public static boolean isPossibleDummy(String d) {
		return d.equals("I") || d.equals("B") || d.equals("S");
	}
}