/***
 * ASM XML Adapter
 * Copyright (c) 2004-2011, Eugene Kuleshov
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
package org.objectweb.custom_asm.xml;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.Type;
import org.objectweb.custom_asm.TypePath;
import org.objectweb.custom_asm.util.Printer;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A {@link org.objectweb.custom_asm.MethodVisitor} that generates SAX 2.0 events from the visited
 * method.
 * 
 * @see SAXClassAdapter
 * @see Processor
 * 
 * @author Eugene Kuleshov
 */
public final class SAXCodeAdapter extends org.objectweb.custom_asm.MethodVisitor {

    static final String[] TYPES = { "top", "int", "float", "double", "long",
            "null", "uninitializedThis" };

    SAXAdapter sa;

    int access;

    private final Map<org.objectweb.custom_asm.Label, String> labelNames;

    /**
     * Constructs a new {@link SAXCodeAdapter SAXCodeAdapter} object.
     * 
     * @param sa
     *            content handler that will be used to send SAX 2.0 events.
     */
    public SAXCodeAdapter(final SAXAdapter sa, final int access) {
        super(Opcodes.ASM5);
        this.sa = sa;
        this.access = access;
        this.labelNames = new HashMap<org.objectweb.custom_asm.Label, String>();
    }

    @Override
    public void visitParameter(String name, int access) {
        AttributesImpl attrs = new AttributesImpl();
        if (name != null) {
            attrs.addAttribute("", "name", "name", "", name);
        }
        StringBuffer sb = new StringBuffer();
        SAXClassAdapter.appendAccess(access, sb);
        attrs.addAttribute("", "access", "access", "", sb.toString());
        sa.addElement("parameter", attrs);
    }

    @Override
    public final void visitCode() {
        if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_NATIVE)) == 0) {
            sa.addStart("code", new AttributesImpl());
        }
    }

    @Override
    public void visitFrame(final int type, final int nLocal,
            final Object[] local, final int nStack, final Object[] stack) {
        AttributesImpl attrs = new AttributesImpl();
        switch (type) {
        case Opcodes.F_NEW:
        case Opcodes.F_FULL:
            if (type == Opcodes.F_NEW) {
                attrs.addAttribute("", "type", "type", "", "NEW");
            } else {
                attrs.addAttribute("", "type", "type", "", "FULL");
            }
            sa.addStart("frame", attrs);
            appendFrameTypes(true, nLocal, local);
            appendFrameTypes(false, nStack, stack);
            break;
        case Opcodes.F_APPEND:
            attrs.addAttribute("", "type", "type", "", "APPEND");
            sa.addStart("frame", attrs);
            appendFrameTypes(true, nLocal, local);
            break;
        case Opcodes.F_CHOP:
            attrs.addAttribute("", "type", "type", "", "CHOP");
            attrs.addAttribute("", "count", "count", "",
                    Integer.toString(nLocal));
            sa.addStart("frame", attrs);
            break;
        case Opcodes.F_SAME:
            attrs.addAttribute("", "type", "type", "", "SAME");
            sa.addStart("frame", attrs);
            break;
        case Opcodes.F_SAME1:
            attrs.addAttribute("", "type", "type", "", "SAME1");
            sa.addStart("frame", attrs);
            appendFrameTypes(false, 1, stack);
            break;
        }
        sa.addEnd("frame");
    }

    private void appendFrameTypes(final boolean local, final int n,
            final Object[] types) {
        for (int i = 0; i < n; ++i) {
            Object type = types[i];
            AttributesImpl attrs = new AttributesImpl();
            if (type instanceof String) {
                attrs.addAttribute("", "type", "type", "", (String) type);
            } else if (type instanceof Integer) {
                attrs.addAttribute("", "type", "type", "",
                        TYPES[((Integer) type).intValue()]);
            } else {
                attrs.addAttribute("", "type", "type", "", "uninitialized");
                attrs.addAttribute("", "label", "label", "",
                        getLabel((org.objectweb.custom_asm.Label) type));
            }
            sa.addElement(local ? "local" : "stack", attrs);
        }
    }

    @Override
    public final void visitInsn(final int opcode) {
        sa.addElement(Printer.OPCODES[opcode], new AttributesImpl());
    }

    @Override
    public final void visitIntInsn(final int opcode, final int operand) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "value", "value", "", Integer.toString(operand));
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public final void visitVarInsn(final int opcode, final int var) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "var", "var", "", Integer.toString(var));
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public final void visitTypeInsn(final int opcode, final String type) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", type);
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public final void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "owner", "owner", "", owner);
        attrs.addAttribute("", "name", "name", "", name);
        attrs.addAttribute("", "desc", "desc", "", desc);
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public final void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc, final boolean itf) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "owner", "owner", "", owner);
        attrs.addAttribute("", "name", "name", "", name);
        attrs.addAttribute("", "desc", "desc", "", desc);
        attrs.addAttribute("", "itf", "itf", "", itf ? "true" : "false");
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, org.objectweb.custom_asm.Handle bsm,
            Object... bsmArgs) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "name", "name", "", name);
        attrs.addAttribute("", "desc", "desc", "", desc);
        attrs.addAttribute("", "bsm", "bsm", "",
                SAXClassAdapter.encode(bsm.toString()));
        sa.addStart("INVOKEDYNAMIC", attrs);
        for (int i = 0; i < bsmArgs.length; i++) {
            sa.addElement("bsmArg", getConstantAttribute(bsmArgs[i]));
        }
        sa.addEnd("INVOKEDYNAMIC");
    }

    @Override
    public final void visitJumpInsn(final int opcode, final org.objectweb.custom_asm.Label label) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "label", "label", "", getLabel(label));
        sa.addElement(Printer.OPCODES[opcode], attrs);
    }

    @Override
    public final void visitLabel(final org.objectweb.custom_asm.Label label) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "name", "name", "", getLabel(label));
        sa.addElement("Label", attrs);
    }

    @Override
    public final void visitLdcInsn(final Object cst) {
        sa.addElement(Printer.OPCODES[Opcodes.LDC], getConstantAttribute(cst));
    }

    private static AttributesImpl getConstantAttribute(final Object cst) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "cst", "cst", "",
                SAXClassAdapter.encode(cst.toString()));
        attrs.addAttribute("", "desc", "desc", "",
                Type.getDescriptor(cst.getClass()));
        return attrs;
    }

    @Override
    public final void visitIincInsn(final int var, final int increment) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "var", "var", "", Integer.toString(var));
        attrs.addAttribute("", "inc", "inc", "", Integer.toString(increment));
        sa.addElement(Printer.OPCODES[Opcodes.IINC], attrs);
    }

    @Override
    public final void visitTableSwitchInsn(final int min, final int max,
                                           final org.objectweb.custom_asm.Label dflt, final org.objectweb.custom_asm.Label... labels) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "min", "min", "", Integer.toString(min));
        attrs.addAttribute("", "max", "max", "", Integer.toString(max));
        attrs.addAttribute("", "dflt", "dflt", "", getLabel(dflt));
        String o = Printer.OPCODES[Opcodes.TABLESWITCH];
        sa.addStart(o, attrs);
        for (int i = 0; i < labels.length; i++) {
            AttributesImpl att2 = new AttributesImpl();
            att2.addAttribute("", "name", "name", "", getLabel(labels[i]));
            sa.addElement("label", att2);
        }
        sa.addEnd(o);
    }

    @Override
    public final void visitLookupSwitchInsn(final org.objectweb.custom_asm.Label dflt, final int[] keys,
                                            final org.objectweb.custom_asm.Label[] labels) {
        AttributesImpl att = new AttributesImpl();
        att.addAttribute("", "dflt", "dflt", "", getLabel(dflt));
        String o = Printer.OPCODES[Opcodes.LOOKUPSWITCH];
        sa.addStart(o, att);
        for (int i = 0; i < labels.length; i++) {
            AttributesImpl att2 = new AttributesImpl();
            att2.addAttribute("", "name", "name", "", getLabel(labels[i]));
            att2.addAttribute("", "key", "key", "", Integer.toString(keys[i]));
            sa.addElement("label", att2);
        }
        sa.addEnd(o);
    }

    @Override
    public final void visitMultiANewArrayInsn(final String desc, final int dims) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", desc);
        attrs.addAttribute("", "dims", "dims", "", Integer.toString(dims));
        sa.addElement(Printer.OPCODES[Opcodes.MULTIANEWARRAY], attrs);
    }

    @Override
    public final void visitTryCatchBlock(final org.objectweb.custom_asm.Label start, final org.objectweb.custom_asm.Label end,
                                         final org.objectweb.custom_asm.Label handler, final String type) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "start", "start", "", getLabel(start));
        attrs.addAttribute("", "end", "end", "", getLabel(end));
        attrs.addAttribute("", "handler", "handler", "", getLabel(handler));
        if (type != null) {
            attrs.addAttribute("", "type", "type", "", type);
        }
        sa.addElement("TryCatch", attrs);
    }

    @Override
    public final void visitMaxs(final int maxStack, final int maxLocals) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "maxStack", "maxStack", "",
                Integer.toString(maxStack));
        attrs.addAttribute("", "maxLocals", "maxLocals", "",
                Integer.toString(maxLocals));
        sa.addElement("Max", attrs);

        sa.addEnd("code");
    }

    @Override
    public void visitLocalVariable(final String name, final String desc,
                                   final String signature, final org.objectweb.custom_asm.Label start, final org.objectweb.custom_asm.Label end,
                                   final int index) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "name", "name", "", name);
        attrs.addAttribute("", "desc", "desc", "", desc);
        if (signature != null) {
            attrs.addAttribute("", "signature", "signature", "",
                    SAXClassAdapter.encode(signature));
        }
        attrs.addAttribute("", "start", "start", "", getLabel(start));
        attrs.addAttribute("", "end", "end", "", getLabel(end));
        attrs.addAttribute("", "var", "var", "", Integer.toString(index));
        sa.addElement("LocalVar", attrs);
    }

    @Override
    public final void visitLineNumber(final int line, final org.objectweb.custom_asm.Label start) {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "line", "line", "", Integer.toString(line));
        attrs.addAttribute("", "start", "start", "", getLabel(start));
        sa.addElement("LineNumber", attrs);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitAnnotationDefault() {
        return new SAXAnnotationAdapter(sa, "annotationDefault", 0, null, null);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitAnnotation(final String desc,
                                                                      final boolean visible) {
        return new SAXAnnotationAdapter(sa, "annotation", visible ? 1 : -1,
                null, desc);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                                          TypePath typePath, String desc, boolean visible) {
        return new SAXAnnotationAdapter(sa, "typeAnnotation", visible ? 1 : -1,
                null, desc, typeRef, typePath);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitParameterAnnotation(final int parameter,
                                                                               final String desc, final boolean visible) {
        return new SAXAnnotationAdapter(sa, "parameterAnnotation", visible ? 1
                : -1, parameter, desc);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitInsnAnnotation(int typeRef,
                                                                          TypePath typePath, String desc, boolean visible) {
        return new SAXAnnotationAdapter(sa, "insnAnnotation", visible ? 1 : -1,
                null, desc, typeRef, typePath);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                                                                              TypePath typePath, String desc, boolean visible) {
        return new SAXAnnotationAdapter(sa, "tryCatchAnnotation", visible ? 1
                : -1, null, desc, typeRef, typePath);
    }

    @Override
    public org.objectweb.custom_asm.AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                                                   TypePath typePath, org.objectweb.custom_asm.Label[] start, org.objectweb.custom_asm.Label[] end, int[] index,
                                                                                   String desc, boolean visible) {
        String[] s = new String[start.length];
        String[] e = new String[end.length];
        for (int i = 0; i < s.length; ++i) {
            s[i] = getLabel(start[i]);
        }
        for (int i = 0; i < e.length; ++i) {
            e[i] = getLabel(end[i]);
        }
        return new SAXAnnotationAdapter(sa, "localVariableAnnotation",
                visible ? 1 : -1, null, desc, typeRef, typePath, s, e, index);
    }

    @Override
    public void visitEnd() {
        sa.addEnd("method");
    }

    private final String getLabel(final org.objectweb.custom_asm.Label label) {
        String name = labelNames.get(label);
        if (name == null) {
            name = Integer.toString(labelNames.size());
            labelNames.put(label, name);
        }
        return name;
    }

}
