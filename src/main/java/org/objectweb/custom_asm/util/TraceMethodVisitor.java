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
package org.objectweb.custom_asm.util;

import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.TypePath;

/**
 * A {@link org.objectweb.custom_asm.MethodVisitor} that prints the methods it visits with a
 * {@link Printer}.
 * 
 * @author Eric Bruneton
 */
public final class TraceMethodVisitor extends org.objectweb.custom_asm.MethodVisitor {

    public final Printer p;

    public TraceMethodVisitor(final Printer p) {
        this(null, p);
    }

    public TraceMethodVisitor(final org.objectweb.custom_asm.MethodVisitor mv, final Printer p) {
        super(Opcodes.ASM5, mv);
        this.p = p;
    }

    @Override
    public void visitParameter(String name, int access) {
        p.visitParameter(name, access);
        super.visitParameter(name, access);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitAnnotation(final String desc,
                                                                      final boolean visible) {
        Printer p = this.p.visitMethodAnnotation(desc, visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitAnnotation(desc,
                visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                                          TypePath typePath, String desc, boolean visible) {
        Printer p = this.p.visitMethodTypeAnnotation(typeRef, typePath, desc,
                visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitTypeAnnotation(
                typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitAttribute(final org.objectweb.custom_asm.Attribute attr) {
        p.visitMethodAttribute(attr);
        super.visitAttribute(attr);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitAnnotationDefault() {
        Printer p = this.p.visitAnnotationDefault();
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitAnnotationDefault();
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitParameterAnnotation(final int parameter,
                                                                               final String desc, final boolean visible) {
        Printer p = this.p.visitParameterAnnotation(parameter, desc, visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitParameterAnnotation(
                parameter, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitCode() {
        p.visitCode();
        super.visitCode();
    }

    @Override
    public void visitFrame(final int type, final int nLocal,
            final Object[] local, final int nStack, final Object[] stack) {
        p.visitFrame(type, nLocal, local, nStack, stack);
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitInsn(final int opcode) {
        p.visitInsn(opcode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        p.visitIntInsn(opcode, operand);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        p.visitVarInsn(opcode, var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        p.visitTypeInsn(opcode, type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        p.visitFieldInsn(opcode, owner, name, desc);
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Deprecated
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        if (api >= Opcodes.ASM5) {
            super.visitMethodInsn(opcode, owner, name, desc);
            return;
        }
        p.visitMethodInsn(opcode, owner, name, desc);
        if (mv != null) {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        if (api < Opcodes.ASM5) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        p.visitMethodInsn(opcode, owner, name, desc, itf);
        if (mv != null) {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, org.objectweb.custom_asm.Handle bsm,
            Object... bsmArgs) {
        p.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(final int opcode, final org.objectweb.custom_asm.Label label) {
        p.visitJumpInsn(opcode, label);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(final org.objectweb.custom_asm.Label label) {
        p.visitLabel(label);
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        p.visitLdcInsn(cst);
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        p.visitIincInsn(var, increment);
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
                                     final org.objectweb.custom_asm.Label dflt, final org.objectweb.custom_asm.Label... labels) {
        p.visitTableSwitchInsn(min, max, dflt, labels);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final org.objectweb.custom_asm.Label dflt, final int[] keys,
                                      final org.objectweb.custom_asm.Label[] labels) {
        p.visitLookupSwitchInsn(dflt, keys, labels);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        p.visitMultiANewArrayInsn(desc, dims);
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitInsnAnnotation(int typeRef,
                                                                          TypePath typePath, String desc, boolean visible) {
        Printer p = this.p
                .visitInsnAnnotation(typeRef, typePath, desc, visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitInsnAnnotation(
                typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitTryCatchBlock(final org.objectweb.custom_asm.Label start, final org.objectweb.custom_asm.Label end,
                                   final org.objectweb.custom_asm.Label handler, final String type) {
        p.visitTryCatchBlock(start, end, handler, type);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                                                                              TypePath typePath, String desc, boolean visible) {
        Printer p = this.p.visitTryCatchAnnotation(typeRef, typePath, desc,
                visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv.visitTryCatchAnnotation(
                typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc,
                                   final String signature, final org.objectweb.custom_asm.Label start, final org.objectweb.custom_asm.Label end,
                                   final int index) {
        p.visitLocalVariable(name, desc, signature, start, end, index);
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                                                   TypePath typePath, org.objectweb.custom_asm.Label[] start, org.objectweb.custom_asm.Label[] end, int[] index,
                                                                                   String desc, boolean visible) {
        Printer p = this.p.visitLocalVariableAnnotation(typeRef, typePath,
                start, end, index, desc, visible);
        org.objectweb.custom_asm.AnnotationVisitor av = mv == null ? null : mv
                .visitLocalVariableAnnotation(typeRef, typePath, start, end,
                        index, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitLineNumber(final int line, final org.objectweb.custom_asm.Label start) {
        p.visitLineNumber(line, start);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        p.visitMaxs(maxStack, maxLocals);
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        p.visitMethodEnd();
        super.visitEnd();
    }
}
