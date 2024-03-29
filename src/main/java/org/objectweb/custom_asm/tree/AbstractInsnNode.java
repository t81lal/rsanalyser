/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.custom_asm.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.custom_asm.MethodVisitor;

/**
 * A node that represents a bytecode instruction. <i>An instruction can appear
 * at most once in at most one {@link org.objectweb.custom_asm.tree.InsnList} at a time</i>.
 * 
 * @author Eric Bruneton
 */
public abstract class AbstractInsnNode {

	public MethodNode method;
    /**
     * The type of {@link org.objectweb.custom_asm.tree.InsnNode} instructions.
     */
    public static final int INSN = 0;

    /**Jump
     * The type of {@link org.objectweb.custom_asm.tree.IntInsnNode} instructions.
     */
    public static final int INT_INSN = 1;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.VarInsnNode} instructions.
     */
    public static final int VAR_INSN = 2;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.TypeInsnNode} instructions.
     */
    public static final int TYPE_INSN = 3;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.FieldInsnNode} instructions.
     */
    public static final int FIELD_INSN = 4;

    /**
     * The type of {@link MethodInsnNode} instructions.
     */
    public static final int METHOD_INSN = 5;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.InvokeDynamicInsnNode} instructions.
     */
    public static final int INVOKE_DYNAMIC_INSN = 6;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.JumpInsnNode} instructions.
     */
    public static final int JUMP_INSN = 7;

    /**
     * The type of {@link LabelNode} "instructions".
     */
    public static final int LABEL = 8;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.LdcInsnNode} instructions.
     */
    public static final int LDC_INSN = 9;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.IincInsnNode} instructions.
     */
    public static final int IINC_INSN = 10;

    /**
     * The type of {@link TableSwitchInsnNode} instructions.
     */
    public static final int TABLESWITCH_INSN = 11;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.LookupSwitchInsnNode} instructions.
     */
    public static final int LOOKUPSWITCH_INSN = 12;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.MultiANewArrayInsnNode} instructions.
     */
    public static final int MULTIANEWARRAY_INSN = 13;

    /**
     * The type of {@link FrameNode} "instructions".
     */
    public static final int FRAME = 14;

    /**
     * The type of {@link org.objectweb.custom_asm.tree.LineNumberNode} "instructions".
     */
    public static final int LINE = 15;

    /**
     * The opcode of this instruction.
     */
    protected int opcode;

    /**
     * The runtime visible type annotations of this instruction. This field is
     * only used for real instructions (i.e. not for labels, frames, or line
     * number nodes). This list is a list of {@link org.objectweb.custom_asm.tree.TypeAnnotationNode} objects.
     * May be <tt>null</tt>.
     * 
     * @associates TypeAnnotationNode
     * @label visible
     */
    public List<org.objectweb.custom_asm.tree.TypeAnnotationNode> visibleTypeAnnotations;

    /**
     * The runtime invisible type annotations of this instruction. This field is
     * only used for real instructions (i.e. not for labels, frames, or line
     * number nodes). This list is a list of {@link org.objectweb.custom_asm.tree.TypeAnnotationNode} objects.
     * May be <tt>null</tt>.
     * 
     * @associates TypeAnnotationNode
     * @label invisible
     */
    public List<org.objectweb.custom_asm.tree.TypeAnnotationNode> invisibleTypeAnnotations;

    /**
     * Previous instruction in the list to which this instruction belongs.
     */
    AbstractInsnNode prev;

    /**
     * Next instruction in the list to which this instruction belongs.
     */
    AbstractInsnNode next;

    /**
     * Index of this instruction in the list to which it belongs. The value of
     * this field is correct only when {@link org.objectweb.custom_asm.tree.InsnList#cache} is not null. A
     * value of -1 indicates that this instruction does not belong to any
     * {@link org.objectweb.custom_asm.tree.InsnList}.
     */
    int index;

    /**
     * Constructs a new {@link AbstractInsnNode}.
     * 
     * @param opcode
     *            the opcode of the instruction to be constructed.
     */
    protected AbstractInsnNode(final int opcode) {
        this.opcode = opcode;
        this.index = -1;
    }

    public void setOpcode(int op) {
    	opcode = op;
    }
    
    /**
     * Returns the opcode of this instruction.
     * 
     * @return the opcode of this instruction.
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Returns the type of this instruction.
     * 
     * @return the type of this instruction, i.e. one the constants defined in
     *         this class.
     */
    public abstract int getType();

    /**
     * Returns the previous instruction in the list to which this instruction
     * belongs, if any.
     * 
     * @return the previous instruction in the list to which this instruction
     *         belongs, if any. May be <tt>null</tt>.
     */
    public AbstractInsnNode getPrevious() {
        return prev;
    }

    /**
     * Returns the next instruction in the list to which this instruction
     * belongs, if any.
     * 
     * @return the next instruction in the list to which this instruction
     *         belongs, if any. May be <tt>null</tt>.
     */
    public AbstractInsnNode getNext() {
        return next;
    }

    /**
     * Makes the given code visitor visit this instruction.
     * 
     * @param cv
     *            a code visitor.
     */
    public abstract void accept(final MethodVisitor cv);

    /**
     * Makes the given visitor visit the annotations of this instruction.
     * 
     * @param mv
     *            a method visitor.
     */
    protected final void acceptAnnotations(final MethodVisitor mv) {
        int n = visibleTypeAnnotations == null ? 0 : visibleTypeAnnotations
                .size();
        for (int i = 0; i < n; ++i) {
            org.objectweb.custom_asm.tree.TypeAnnotationNode an = visibleTypeAnnotations.get(i);
            an.accept(mv.visitInsnAnnotation(an.typeRef, an.typePath, an.desc,
                    true));
        }
        n = invisibleTypeAnnotations == null ? 0 : invisibleTypeAnnotations
                .size();
        for (int i = 0; i < n; ++i) {
            org.objectweb.custom_asm.tree.TypeAnnotationNode an = invisibleTypeAnnotations.get(i);
            an.accept(mv.visitInsnAnnotation(an.typeRef, an.typePath, an.desc,
                    false));
        }
    }

    /**
     * Returns a copy of this instruction.
     * 
     * @param labels
     *            a map from LabelNodes to cloned LabelNodes.
     * @return a copy of this instruction. The returned instruction does not
     *         belong to any {@link org.objectweb.custom_asm.tree.InsnList}.
     */
    public abstract AbstractInsnNode clone(
            final Map<LabelNode, LabelNode> labels);

    /**
     * Returns the clone of the given label.
     * 
     * @param label
     *            a label.
     * @param map
     *            a map from LabelNodes to cloned LabelNodes.
     * @return the clone of the given label.
     */
    static LabelNode clone(final LabelNode label,
            final Map<LabelNode, LabelNode> map) {
        return map.get(label);
    }

    /**
     * Returns the clones of the given labels.
     * 
     * @param labels
     *            a list of labels.
     * @param map
     *            a map from LabelNodes to cloned LabelNodes.
     * @return the clones of the given labels.
     */
    static LabelNode[] clone(final List<LabelNode> labels,
            final Map<LabelNode, LabelNode> map) {
        LabelNode[] clones = new LabelNode[labels.size()];
        for (int i = 0; i < clones.length; ++i) {
            clones[i] = map.get(labels.get(i));
        }
        return clones;
    }

    /**
     * Clones the annotations of the given instruction into this instruction.
     * 
     * @param insn
     *            the source instruction.
     * @return this instruction.
     */
    protected final AbstractInsnNode cloneAnnotations(
            final AbstractInsnNode insn) {
        if (insn.visibleTypeAnnotations != null) {
            this.visibleTypeAnnotations = new ArrayList<org.objectweb.custom_asm.tree.TypeAnnotationNode>();
            for (int i = 0; i < insn.visibleTypeAnnotations.size(); ++i) {
                org.objectweb.custom_asm.tree.TypeAnnotationNode src = insn.visibleTypeAnnotations.get(i);
                org.objectweb.custom_asm.tree.TypeAnnotationNode ann = new org.objectweb.custom_asm.tree.TypeAnnotationNode(src.typeRef,
                        src.typePath, src.desc);
                src.accept(ann);
                this.visibleTypeAnnotations.add(ann);
            }
        }
        if (insn.invisibleTypeAnnotations != null) {
            this.invisibleTypeAnnotations = new ArrayList<org.objectweb.custom_asm.tree.TypeAnnotationNode>();
            for (int i = 0; i < insn.invisibleTypeAnnotations.size(); ++i) {
                org.objectweb.custom_asm.tree.TypeAnnotationNode src = insn.invisibleTypeAnnotations.get(i);
                org.objectweb.custom_asm.tree.TypeAnnotationNode ann = new org.objectweb.custom_asm.tree.TypeAnnotationNode(src.typeRef,
                        src.typePath, src.desc);
                src.accept(ann);
                this.invisibleTypeAnnotations.add(ann);
            }
        }
        return this;
    }
}
