package org.nullbool.api.obfuscation;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015
 */
public class StringBuilderCharReplacer extends NodeVisitor {

	private final Set<Replace> replaces = new HashSet<Replace>();
	
	@Override
	public void visitMethod(MethodMemberNode m) {
		if(m.opcode() == INVOKEVIRTUAL) {
			MethodInsnNode min = m.min();
			if(min.owner.equals("java/lang/StringBuilder") && min.name.equals("append") && min.desc.equals("(C)Ljava/lang/StringBuilder;")) {
				NumberNode nn = m.firstNumber();
				if(nn != null) {
					Replace r = new Replace(m.method(), nn.insn(), min, nn.number());
					replaces.add(r);
				}
			}
		}
	}

	public void output() {
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		
		for(Replace r : replaces) {
			r.method.instructions.remove(r.cstInsn);
			char c = (char) r.cst;
			String s = String.valueOf(c);
			r.method.instructions.insertBefore(r.min, new LdcInsnNode(s));
			r.min.desc = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
			
			if(c <= lowest)
				lowest = c;
			
			if(c >= highest)
				highest = c;
		}
		
		System.err.printf("Replaced %d StringBuilder.append(C) calls (lower=%d, upper=%d).%n", replaces.size(), lowest, highest);
		replaces.clear();
	}
	
	private static class Replace {
		private final MethodNode method;
		private final AbstractInsnNode cstInsn;
		private final MethodInsnNode min;
		private final int cst;
		
		public Replace(MethodNode method, AbstractInsnNode cstInsn, MethodInsnNode min, int cst) {
			this.method = method;
			this.cstInsn = cstInsn;
			this.min = min;
			this.cst = cst;
		}
	}
}