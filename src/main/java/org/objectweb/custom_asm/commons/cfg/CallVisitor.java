package org.objectweb.custom_asm.commons.cfg;

import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.commons.cfg.graph.CallGraph;

/**
 * @author Tyler Sedlar
 */
public class CallVisitor extends org.objectweb.custom_asm.MethodVisitor {

    public CallVisitor() {
		super(Opcodes.ASM5);
	}

	public final CallGraph graph = new CallGraph();

    private org.objectweb.custom_asm.tree.MethodNode mn;

    public void visit(org.objectweb.custom_asm.tree.MethodNode mn) {
        this.mn = mn;
        mn.accept(this);
    }
    
    @Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        graph.addMethodCall(mn.handle, new org.objectweb.custom_asm.Handle(0, owner, name, desc));

    }
    
    @Override
	public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        graph.addMethodCall(mn.handle, new org.objectweb.custom_asm.Handle(0, owner, name, desc));

    }
}