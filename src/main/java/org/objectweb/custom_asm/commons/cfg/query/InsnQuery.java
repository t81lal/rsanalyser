package org.objectweb.custom_asm.commons.cfg.query;

import org.objectweb.custom_asm.Opcodes;

/**
 * @author Tyler Sedlar
 */
public class InsnQuery implements Opcodes {

    public int distance = -1;

    public final int opcode;
    protected org.objectweb.custom_asm.tree.AbstractInsnNode insn;

    public InsnQuery(int opcode) {
        this.opcode = opcode;
    }

    public boolean matches(org.objectweb.custom_asm.tree.AbstractInsnNode ain) {
        return ain.getOpcode() == opcode;
    }

    public void setInstruction(org.objectweb.custom_asm.tree.AbstractInsnNode insn) {
        this.insn = insn;
    }

    public org.objectweb.custom_asm.tree.AbstractInsnNode insn() {
        return insn;
    }

    public InsnQuery distance(int distance) {
        this.distance = distance;
        return this;
    }
}
