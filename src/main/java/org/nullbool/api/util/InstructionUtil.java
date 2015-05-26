package org.nullbool.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class InstructionUtil implements Opcodes {

//	IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, 
//	IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, 
//	IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
	
	public static final Set<Integer> CONDITIONAL_OPCODES   = new HashSet<Integer>();
	public static final Set<Integer> UNCONDITIONAL_OPCODES = new HashSet<Integer>();
	public static final Set<Integer> EXIT_OPCODES          = new HashSet<Integer>();
	public static final Set<Integer> SWITCH_OPCODES        = new HashSet<Integer>();
	static{
		CONDITIONAL_OPCODES.add(IFEQ);
		CONDITIONAL_OPCODES.add(IFNE);
		CONDITIONAL_OPCODES.add(IFLT);
		CONDITIONAL_OPCODES.add(IFGE);
		CONDITIONAL_OPCODES.add(IFGT);
		CONDITIONAL_OPCODES.add(IFLE);
		
		CONDITIONAL_OPCODES.add(IF_ICMPEQ);
		CONDITIONAL_OPCODES.add(IF_ICMPNE);
		CONDITIONAL_OPCODES.add(IF_ICMPLT);
		CONDITIONAL_OPCODES.add(IF_ICMPGE);
		CONDITIONAL_OPCODES.add(IF_ICMPGT);
		CONDITIONAL_OPCODES.add(IF_ICMPLE);
		CONDITIONAL_OPCODES.add(IF_ACMPEQ);
		CONDITIONAL_OPCODES.add(IF_ACMPNE);
		
		CONDITIONAL_OPCODES.add(IFNONNULL);
		CONDITIONAL_OPCODES.add(IFNULL);
		
		UNCONDITIONAL_OPCODES.add(GOTO);
		
		//TODO: check these
		UNCONDITIONAL_OPCODES.add(JSR);
		UNCONDITIONAL_OPCODES.add(RET);
		
		EXIT_OPCODES.add(RETURN);
		EXIT_OPCODES.add(IRETURN);
		EXIT_OPCODES.add(LRETURN);
		EXIT_OPCODES.add(DRETURN);
		EXIT_OPCODES.add(FRETURN);
		EXIT_OPCODES.add(ARETURN);
		EXIT_OPCODES.add(ATHROW);
		
		SWITCH_OPCODES.add(TABLESWITCH);
		SWITCH_OPCODES.add(LOOKUPSWITCH);
	}
	
	public static boolean isConditional(int op) {
		return CONDITIONAL_OPCODES.contains(op);
	}
	
	public static boolean isUnconditional(int op) {
		return UNCONDITIONAL_OPCODES.contains(op);
	}
	
	public static boolean isExit(int op) {
		return EXIT_OPCODES.contains(op);
	}

	public static boolean isSwitch(int op) {
		return SWITCH_OPCODES.contains(op);
	}
	
	public static void print(Collection<AbstractInsnNode[]> coll) {
		for (AbstractInsnNode[] ains : coll) {
		    System.out.println(Arrays.toString(ains));
		}
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