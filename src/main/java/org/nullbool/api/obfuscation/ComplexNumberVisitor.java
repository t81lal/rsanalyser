/**
 * 
 */
package org.nullbool.api.obfuscation;

import java.util.List;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 20 Jun 2015 00:41:27
 */
public class ComplexNumberVisitor implements Opcodes {
	
	public static FieldNode lookup(FieldInsnNode fin) {
		return lookup(fin.owner, fin.name, fin.desc);
	}
	
	public static FieldNode lookup(String owner, String name, String desc) {
		String halfKey = name + " " + desc;
		ClassTree tree = Context.current().getClassTree();
		ClassNode cn = tree.getClass(owner);

		if(cn == null)
			return null;

		FieldNode f = find(cn, halfKey);
		if(f != null)
			return f;

		for(ClassNode sup : tree.getSupers(cn)) {
			f = find(sup, halfKey);
			if(f != null)
				return f;
		}

		return null;
	}
	
	private static FieldNode find(ClassNode cn, String halfKey) {
		for(FieldNode f : cn.fields) {
			if(f.halfKey().equals(halfKey))
				return f;
		}

		return null;
	}
	
    private static boolean validDesc(String desc) {
    	return desc.equals("I") || desc.equals("J");
    }

    private static class TestVisitor extends NodeVisitor {
    	@Override
		public void visitOperation(ArithmeticNode an) {
    		FieldMemberNode fmn = an.firstField();
    		NumberNode nn = an.firstNumber();
    		
    		if(nn != null && fmn != null && validDesc(fmn.desc())) {
    			long value = nn.longNumber();
    			FieldNode fn = null;
    			if((value % 2) == 1 && ((fn = lookup(fmn.owner(), fmn.name(), fmn.desc()))) != null) {
    				if(fn.owner.name.equals("client") && fn.name.equals("ko")) {
    					System.out.printf("%s.%s * %d [%s, %s, %s %s, %s].%n", fn.owner.name, fn.name, value, fmn.opname(), an.opname(), an.parent().opname(), an.parent().insn(), an.method());
    				}
    			}
    		}
    	}
    	
    	@Override
		public void visitField(FieldMemberNode fmn) {
    		FieldNode fn = null;
    		if((fn = lookup(fmn.owner(), fmn.name(), fmn.desc())) != null) {
				if(fn.owner.name.equals("client") && fn.name.equals("ko")) {
					System.out.printf("%s.%s [%s, %s, %s].%n", fn.owner.name, fn.name, fmn.opname(), fmn.parent().opname(), fmn.method());
				}
			}
    	}
    }
    
    /* First we collect all of the 'pure' multis, ie. those that
	 * are not folded. */
	/* Check to see if any of the values are resolved to one. This means
	 * that the number the real multi. This is a sanity check and if it 
	 * fails we most likely can't reverse the number obfuscation on that
	 * field so we error. */
    public void run(List<ClassNode> classes, TreeBuilder tb) {
    	
    	TestVisitor tv = new TestVisitor();
    	run(classes, tv, tb);
    	
    	if(true) {
    		System.exit(1);
    	}
    }
    
    private void run(List<ClassNode> classes, NodeVisitor nv, TreeBuilder tb) {
		for(ClassNode cn : classes) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					tb.build(m).accept(nv);
				}
			}
		}
    }
}