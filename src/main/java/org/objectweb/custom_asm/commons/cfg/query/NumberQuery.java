package org.objectweb.custom_asm.commons.cfg.query;

import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 */
public class NumberQuery extends InsnQuery {

    private int number = -1;

    public NumberQuery(int opcode) {
        super(opcode);
    }

    public NumberQuery(int opcode, int number) {
        this(opcode);
        this.number = number;
    }

    @Override
    public boolean matches(org.objectweb.custom_asm.tree.AbstractInsnNode ain) {
        if (!(ain instanceof IntInsnNode) && !(ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode) && !(ain instanceof VarInsnNode))
            return false;
        if (ain instanceof IntInsnNode) {
            return number == -1 || ((IntInsnNode) ain).operand == number;
        } else if (ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode) {
            Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) ain).cst;
            return number == -1 || cst instanceof Number && ((Number) cst).intValue() == number;
        } else {
            return number == -1 || ((VarInsnNode) ain).var == number;
        }
    }
}
