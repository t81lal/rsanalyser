package org.objectweb.custom_asm.commons.cfg.tree.node;

import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.tree.FieldInsnNode;

public class ReferenceNode extends AbstractNode {

    public ReferenceNode(NodeTree tree, org.objectweb.custom_asm.tree.AbstractInsnNode insn, int collapsed, int producing) {
        super(tree, insn, collapsed, producing);
    }

    public boolean isStatic() {
        return opcode() == GETSTATIC || opcode() == PUTSTATIC || opcode() == INVOKESTATIC;
    }

    public String key() {
        org.objectweb.custom_asm.tree.AbstractInsnNode ain = insn();
        if (ain instanceof FieldInsnNode) {
            FieldInsnNode fin = (FieldInsnNode) ain;
            return fin.owner + "." + fin.name;
            //return fin.key();
            //return fin.owner + "." + fin.name + fin.desc;
        } else if (ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
            org.objectweb.custom_asm.tree.MethodInsnNode min = (org.objectweb.custom_asm.tree.MethodInsnNode) ain;
            return min.owner + "." + min.name + min.desc;
        }
        return null;
    }

    public String owner() {
        org.objectweb.custom_asm.tree.AbstractInsnNode insn = insn();
        if (this instanceof FieldMemberNode) {
            return ((FieldInsnNode) insn).owner;
        } else if (this instanceof MethodMemberNode) {
            return ((org.objectweb.custom_asm.tree.MethodInsnNode) insn).owner;
        }
        return null;
    }

    public String name() {
        org.objectweb.custom_asm.tree.AbstractInsnNode ain = insn();
        if (ain instanceof FieldInsnNode) {
            return ((FieldInsnNode) ain).name;
        } else if (ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
            return ((org.objectweb.custom_asm.tree.MethodInsnNode) ain).name;
        }
        return null;
    }

    public String desc() {
        org.objectweb.custom_asm.tree.AbstractInsnNode ain = insn();
        if (this instanceof FieldMemberNode) {
            return ((FieldInsnNode) ain).desc;
        } else if (this instanceof MethodMemberNode) {
            return ((org.objectweb.custom_asm.tree.MethodInsnNode) ain).desc;
        }
        return null;
    }

    public boolean referenced(org.objectweb.custom_asm.tree.MethodNode mn) {
        for (org.objectweb.custom_asm.tree.AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof FieldInsnNode) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (key().equals(fin.owner + "." + fin.name)) return true;
            } else if (ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
                org.objectweb.custom_asm.tree.MethodInsnNode min = (org.objectweb.custom_asm.tree.MethodInsnNode) ain;
                if (key().equals(min.owner + "." + min.name + min.desc)) return true;
            }
        }
        return false;
    }
}