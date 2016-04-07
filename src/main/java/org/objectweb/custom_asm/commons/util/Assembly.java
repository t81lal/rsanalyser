package org.objectweb.custom_asm.commons.util;

import java.util.Arrays;

import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.JumpInsnNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.LineNumberNode;
import org.objectweb.custom_asm.tree.LookupSwitchInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 * @author Bibl
 */
public class Assembly {

    public static final String[] OPCODES = {"NOP", "ACONST_NULL", "ICONST_M1", "ICONST_0", "ICONST_1", "ICONST_2", "ICONST_3", "ICONST_4", "ICONST_5", "LCONST_0", "LCONST_1", "FCONST_0", "FCONST_1", "FCONST_2", "DCONST_0", "DCONST_1", "BIPUSH", "SIPUSH", "LDC", "", "", "ILOAD", "LLOAD", "FLOAD", "DLOAD", "ALOAD", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "IALOAD", "LALOAD", "FALOAD", "DALOAD", "AALOAD", "BALOAD", "CALOAD", "SALOAD", "ISTORE", "LSTORE", "FSTORE", "DSTORE", "ASTORE", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "IASTORE", "LASTORE", "FASTORE", "DASTORE", "AASTORE", "BASTORE", "CASTORE", "SASTORE", "POP", "POP2", "DUP", "DUP_X1", "DUP_X2", "DUP2", "DUP2_X1", "DUP2_X2", "SWAP", "IADD", "LADD", "FADD", "DADD", "ISUB", "LSUB", "FSUB", "DSUB", "IMUL", "LMUL", "FMUL", "DMUL", "IDIV", "LDIV", "FDIV", "DDIV", "IREM", "LREM", "FREM", "DREM", "INEG", "LNEG", "FNEG", "DNEG", "ISHL", "LSHL", "ISHR", "LSHR", "IUSHR", "LUSHR", "IAND", "LAND", "IOR", "LOR", "IXOR", "LXOR", "IINC", "I2L", "I2F", "I2D", "L2I", "L2F", "L2D", "F2I", "F2L", "F2D", "D2I", "D2L", "D2F", "I2B", "I2C", "I2S", "LCMP", "FCMPL", "FCMPG", "DCMPL", "DCMPG", "IFEQ", "IFNE", "IFLT", "IFGE", "IFGT", "IFLE", "IF_ICMPEQ", "IF_ICMPNE", "IF_ICMPLT", "IF_ICMPGE", "IF_ICMPGT", "IF_ICMPLE", "IF_ACMPEQ", "IF_ACMPNE", "GOTO", "JSR", "RET", "TABLESWITCH", "LOOKUPSWITCH", "IRETURN", "LRETURN", "FRETURN", "DRETURN", "ARETURN", "RETURN", "GETSTATIC", "PUTSTATIC", "GETFIELD", "PUTFIELD", "INVOKEVIRTUAL", "INVOKESPECIAL", "INVOKESTATIC", "INVOKEINTERFACE", "INVOKEDYNAMIC", "NEW", "NEWARRAY", "ANEWARRAY", "ARRAYLENGTH", "ATHROW", "CHECKCAST", "INSTANCEOF", "MONITORENTER", "MONITOREXIT", "", "MULTIANEWARRAY", "IFNULL", "IFNONNULL"};
    public static final int LONGEST_OPCODE_NAME = getLongest(OPCODES);
    
    public static int getLongest(String[] strings) {
    	String longest = "";
    	for(String s : strings) {
    		if(s.length() > longest.length())
    			longest = s;
    	}
    	return longest.length();
    }
    
    public static String pad(String s, int size) {
    	if(s.length() >= size)
    		return s;
    	StringBuilder sb = new StringBuilder(s);
    	int diff = size - s.length();
    	for(int i=0; i < diff; i++) {
    		sb.append(" ");
    	}
    	return sb.toString();
    }
    
    public static boolean instructionsEqual(org.objectweb.custom_asm.tree.AbstractInsnNode insn1, org.objectweb.custom_asm.tree.AbstractInsnNode insn2) {
        if (insn1 == insn2) {
            return true;
        }
        if (insn1 == null || insn2 == null || insn1.getType() != insn2.getType() ||
                insn1.getOpcode() != insn2.getOpcode()) {
            return false;
        }
        int size;
        switch (insn1.getType()) {
            case org.objectweb.custom_asm.tree.AbstractInsnNode.INSN:
                return true;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.INT_INSN:
                IntInsnNode iin1 = (IntInsnNode) insn1, iin2 = (IntInsnNode) insn2;
                return iin1.operand == iin2.operand;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.VAR_INSN:
                VarInsnNode vin1 = (VarInsnNode) insn1, vin2 = (VarInsnNode) insn2;
                return vin1.var == vin2.var;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.TYPE_INSN:
                org.objectweb.custom_asm.tree.TypeInsnNode tin1 = (org.objectweb.custom_asm.tree.TypeInsnNode) insn1, tin2 = (org.objectweb.custom_asm.tree.TypeInsnNode) insn2;
                return tin1.desc.equals(tin2.desc);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.FIELD_INSN:
                FieldInsnNode fin1 = (FieldInsnNode) insn1, fin2 = (FieldInsnNode) insn2;
                return fin1.desc.equals(fin2.desc) && fin1.name.equals(fin2.name) && fin1.owner.equals(fin2.owner);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.METHOD_INSN:
                org.objectweb.custom_asm.tree.MethodInsnNode min1 = (org.objectweb.custom_asm.tree.MethodInsnNode) insn1, min2 = (org.objectweb.custom_asm.tree.MethodInsnNode) insn2;
                return min1.desc.equals(min2.desc) && min1.name.equals(min2.name) && min1.owner.equals(min2.owner);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                org.objectweb.custom_asm.tree.InvokeDynamicInsnNode idin1 = (org.objectweb.custom_asm.tree.InvokeDynamicInsnNode) insn1, idin2 = (org.objectweb.custom_asm.tree.InvokeDynamicInsnNode) insn2;
                return idin1.bsm.equals(idin2.bsm) && Arrays.equals(idin1.bsmArgs, idin2.bsmArgs) &&
                        idin1.desc.equals(idin2.desc) && idin1.name.equals(idin2.name);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.JUMP_INSN:
                JumpInsnNode jin1 = (JumpInsnNode) insn1, jin2 = (JumpInsnNode) insn2;
                return instructionsEqual(jin1.label, jin2.label);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LABEL:
                org.objectweb.custom_asm.Label label1 = ((org.objectweb.custom_asm.tree.LabelNode) insn1).getLabel(), label2 = ((org.objectweb.custom_asm.tree.LabelNode) insn2).getLabel();
                return label1 == null ? label2 == null : label1.info == null ? label2.info == null :
                        label1.info.equals(label2.info);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LDC_INSN:
                LdcInsnNode lin1 = (LdcInsnNode) insn1, lin2 = (LdcInsnNode) insn2;
                return lin1.cst.equals(lin2.cst);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.IINC_INSN:
                org.objectweb.custom_asm.tree.IincInsnNode iiin1 = (org.objectweb.custom_asm.tree.IincInsnNode) insn1, iiin2 = (org.objectweb.custom_asm.tree.IincInsnNode) insn2;
                return iiin1.incr == iiin2.incr && iiin1.var == iiin2.var;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.TABLESWITCH_INSN:
                org.objectweb.custom_asm.tree.TableSwitchInsnNode tsin1 = (org.objectweb.custom_asm.tree.TableSwitchInsnNode) insn1, tsin2 = (org.objectweb.custom_asm.tree.TableSwitchInsnNode) insn2;
                size = tsin1.labels.size();
                if (size != tsin2.labels.size()) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    if (!instructionsEqual(tsin1.labels.get(i), tsin2.labels.get(i))) {
                        return false;
                    }
                }
                return instructionsEqual(tsin1.dflt, tsin2.dflt) && tsin1.max == tsin2.max && tsin1.min == tsin2.min;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN:
                LookupSwitchInsnNode lsin1 = (LookupSwitchInsnNode) insn1, lsin2 = (LookupSwitchInsnNode) insn2;
                size = lsin1.labels.size();
                if (size != lsin2.labels.size()) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    if (!instructionsEqual(lsin1.labels.get(i), lsin2.labels.get(i))) {
                        return false;
                    }
                }
                return instructionsEqual(lsin1.dflt, lsin2.dflt) && lsin1.keys.equals(lsin2.keys);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN:
                org.objectweb.custom_asm.tree.MultiANewArrayInsnNode manain1 = (org.objectweb.custom_asm.tree.MultiANewArrayInsnNode) insn1, manain2 = (org.objectweb.custom_asm.tree.MultiANewArrayInsnNode) insn2;
                return manain1.desc.equals(manain2.desc) && manain1.dims == manain2.dims;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.FRAME:
                org.objectweb.custom_asm.tree.FrameNode fn1 = (org.objectweb.custom_asm.tree.FrameNode) insn1, fn2 = (org.objectweb.custom_asm.tree.FrameNode) insn2;
                return fn1.local.equals(fn2.local) && fn1.stack.equals(fn2.stack);
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LINE:
                LineNumberNode lnn1 = (LineNumberNode) insn1, lnn2 = (LineNumberNode) insn2;
                return lnn1.line == lnn2.line && instructionsEqual(lnn1.start, lnn2.start);
        }
        return false;
    }

    public static boolean instructionsEqual(org.objectweb.custom_asm.tree.AbstractInsnNode[] insns, org.objectweb.custom_asm.tree.AbstractInsnNode[] insns2) {
        if (insns == insns2) {
            return true;
        }
        if (insns == null || insns2 == null) {
            return false;
        }
        int length = insns.length;
        if (insns2.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            org.objectweb.custom_asm.tree.AbstractInsnNode insn1 = insns[i], insn2 = insns2[i];
            if (!(insn1 == null ? insn2 == null : instructionsEqual(insn1, insn2))) {
                return false;
            }
        }
        return true;
    }

    public static String toString(org.objectweb.custom_asm.tree.AbstractInsnNode insn) {
        if (insn == null) {
            return "null";
        }
        int op = insn.getOpcode();
        if (op == -1) {
            return insn.toString();
        }
        StringBuilder sb = new StringBuilder();
        /* pad the opcode name so that all the extra information for the instructions is aligned on the column.
         * TODO: maybe change the column length to the longest opcode name in the instruction set rather than
         * out of all the possible ones(statically, the longest opcode name is invokedynamic).*/
        sb.append(pad(OPCODES[op].toLowerCase(), LONGEST_OPCODE_NAME));
        
        switch (insn.getType()) {
            case org.objectweb.custom_asm.tree.AbstractInsnNode.INT_INSN:
                sb.append(((IntInsnNode) insn).operand);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.VAR_INSN:
                sb.append('#').append(((VarInsnNode) insn).var);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.TYPE_INSN:
                sb.append(((org.objectweb.custom_asm.tree.TypeInsnNode) insn).desc);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.FIELD_INSN:
                FieldInsnNode fin = (FieldInsnNode) insn;
                sb.append(fin.owner).append('.').append(fin.name).append(' ').append(fin.desc);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.METHOD_INSN:
                org.objectweb.custom_asm.tree.MethodInsnNode min = (org.objectweb.custom_asm.tree.MethodInsnNode) insn;
                sb.append(min.owner).append('.').append(min.name).append(' ').append(min.desc);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.JUMP_INSN:
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LDC_INSN:
                Object cst = ((LdcInsnNode) insn).cst;
                sb.append(cst).append("(").append(cst.getClass().getName()).append(")");
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.IINC_INSN:
                org.objectweb.custom_asm.tree.IincInsnNode iin = (org.objectweb.custom_asm.tree.IincInsnNode) insn;
                sb.append('#').append(iin.var).append(' ').append(iin.incr);
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.TABLESWITCH_INSN:
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN:
                break;
            case org.objectweb.custom_asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN:
                org.objectweb.custom_asm.tree.MultiANewArrayInsnNode m = (org.objectweb.custom_asm.tree.MultiANewArrayInsnNode) insn;
                sb.append(m.desc).append(' ').append(m.dims);
                break;
        }
        return sb.toString();
    }
}
