package org.objectweb.custom_asm.commons.cfg.query;

import org.objectweb.custom_asm.tree.FieldInsnNode;

/**
 * @author Tyler Sedlar
 */
public class MemberQuery extends InsnQuery {

    protected final String owner, name, desc;

    public MemberQuery(int opcode, String owner, String name, String desc) {
        super(opcode);
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public MemberQuery(int opcode, String owner, String desc) {
        this(opcode, owner, null, desc);
    }

    public MemberQuery(int opcode, String desc) {
        this(opcode, null, desc);
    }

    public MemberQuery(String desc) {
        this(-1, desc);
    }

    public MemberQuery(int opcode) {
        this(opcode, null, null, null);
    }

    @Override
    public boolean matches(org.objectweb.custom_asm.tree.AbstractInsnNode ain) {
        if (!(ain instanceof FieldInsnNode) && !(ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode)) return false;
        int opcode = ain.getOpcode();
        String owner, name, desc;
        if (ain instanceof FieldInsnNode) {
            FieldInsnNode fin = (FieldInsnNode) ain;
            owner = fin.owner;
            name = fin.name;
            desc = fin.desc;
        } else {
            org.objectweb.custom_asm.tree.MethodInsnNode min = (org.objectweb.custom_asm.tree.MethodInsnNode) ain;
            owner = min.owner;
            name = min.name;
            desc = min.desc;
        }
        if (this.opcode == -1 || this.opcode == opcode) {
            if (this.owner == null || this.owner.equals(owner)) {
                if (this.name == null || this.name.equals(name)) {
                    if (this.desc == null || this.desc.equals(desc) || desc.matches(this.desc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
