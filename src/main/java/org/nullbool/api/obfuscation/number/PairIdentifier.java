package org.nullbool.api.obfuscation.number;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.VariableNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 29 Jul 2015 01:24:59
 */
public class PairIdentifier extends NodeVisitor {

	private final Map<String, EuclideanNumberPair> found = new HashMap<String, EuclideanNumberPair>();
	
	@Override
	public void visitOperation(ArithmeticNode an) {
		if(an.multiplying()) {
			NumberNode cst = an.firstNumber();
			AbstractNode other = an.child(0);
			if(other == cst) {
				other = an.child(1);
			}
			
			if(cst != null) {
				AtomicBoolean safe = new AtomicBoolean(true);
				AtomicReference<Object> store = new AtomicReference<Object>(null);

				AbstractNode parent = an.parent();
				parent.accept(new NodeVisitor() {
					@Override
					public void visitField(FieldMemberNode fmn) {
						if(fmn.opcode() == PUTFIELD || fmn.opcode() == PUTSTATIC) {
							if(store.get() != null) {
								safe.set(false);
							}
							
							store.set(fmn);
						}
					}
					
					@Override
					public void visitVariable(VariableNode vn) {
						if(vn.storing()) {
							if(store.get() != null) {
								safe.set(false);
							}
							
							store.set(vn);
						}
					}
				});
				
				if(!safe.get()) {
					return;
				}
				
				FieldMemberNode f1 = null;
				if(store.get() != null) {
					if(store.get() instanceof FieldMemberNode) {
						f1 = (FieldMemberNode) store.get();
					}
				}
				
				FieldMemberNode f2 = null;
				if(other instanceof FieldMemberNode) {
					f2 = (FieldMemberNode) other;
				}
				
				if(store.get() != null && f2 != null && f1 != f2) {
					return;
				}
				
				FieldMemberNode f = f1 == null ? f2 : f1;
				if(f == null) {
					return;
				}
				
				EuclideanNumberPair prev = found.get(f);
				if (prev != null) {
					return;
				}
			}
			
		}
	}
}