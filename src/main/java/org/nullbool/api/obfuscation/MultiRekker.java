package org.nullbool.api.obfuscation;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;

import org.nullbool.api.obfuscation.refactor.SetCreator;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 12 Sep 2015 01:20:05
 */
public class MultiRekker {
	

	public static void rek(Collection<ClassNode> nodes) {
		NullPermeableHashMap<String, Set<Long>> encoders = new NullPermeableHashMap<String, Set<Long>>(new SetCreator<Long>());
		NullPermeableHashMap<String, Set<Long>> decoders = new NullPermeableHashMap<String, Set<Long>>(new SetCreator<Long>());
		// lv = f  * d
		// f  = lv * e
		// f  = f  * (e)(d)
		TreeBuilder tb = new TreeBuilder();
		
		NodeVisitor nv = new NodeVisitor() {
			@Override
			public void visitOperation(ArithmeticNode an) {
				if (an.multiplying()) {
					NumberNode nn = an.firstNumber();
					AbstractInsnNode _ldc = nn.insn();
					LdcInsnNode ldc =  null;
					if(_ldc instanceof LdcInsnNode) {
						ldc = (LdcInsnNode) _ldc;
					} else {
						return;
					}
					
					FieldMemberNode fmn = an.firstField();
					if(nn != null) {
						if(fmn != null && fmn.desc().equals("I")) {
							if(fmn.getting()) {
								decoders.getNonNull(fmn.key()).add(Long.valueOf((long)ldc.cst));
							} else {
								throw new IllegalStateException();
							}
						} else if(an.parent() instanceof FieldMemberNode && (fmn = (FieldMemberNode) an.parent()).desc().equals("I")) {
							if(fmn.putting()) {
								encoders.getNonNull(fmn.key()).add(Long.valueOf((long)ldc.cst));
							} else {
								throw new IllegalStateException();
							}
						}
					}
				}
			}
		};
		
		for(ClassNode cn : nodes) {
			for(MethodNode m : cn.methods) {
				tb.build(m).accept(nv);
			}
		}
	}

	public static BigInteger gcd(BigInteger... given) {
		BigInteger g = given[0].gcd(given[1]);
		for (int i = 2; i < given.length; i++) {
			g = g.gcd(given[i]);
		}
		return g;
	}
}