package org.nullbool.api.obfuscation;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.util.map.NullPermeableMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class MultiplicativeModifierCollector extends NodeVisitor {

	public final MultiplierHandler mh = Context.current().getMultiplierHandler();
	public final CountMap total = new CountMap();
	public final CountMap mults = new CountMap();

	//	@Override
	//	public void visitField(FieldMemberNode f) {
	//		String desc = f.fin().desc;
	//		if(desc.equals("I") || desc.equals("J")) {
	//			total.getNotNull(lookup(f.fin())).incrementAndGet();
	//			
	//			if(f.opcode() == GETFIELD || f.opcode() == GETSTATIC) {
	//				if(f.firstNumber() != null) {
	//					System.out.println(f.fin().key() + " has " + f.firstNumber().number());
	//				}
	//			}
	//		}
	//	}

	@Override
	public void visitOperation(ArithmeticNode an) {
		FieldMemberNode f = an.firstField();
		FieldNode fn = null;
		if(f != null) {
			fn = lookup(f.fin());
			total.getNotNull(fn).incrementAndGet();
		}

		if((an.opcode() == IMUL || an.opcode() == LMUL) && an.children() > 0 && fn != null) {
			String desc = f.fin().desc;
			if(desc.equals("I") || desc.equals("J")) {
				NumberNode nn = an.firstNumber();
				if(nn != null) {
					if(f.opcode() == GETFIELD || an.opcode() == GETSTATIC) {
						int decoder = mh.inverseDecoder(f.key());
						if(decoder != 0) {
							int n = nn.number() * decoder;
							//System.out.printf("%s %d * %d = %d.%n", f.fin().key(), nn.number(), decoder, n);
							if(n == 1) {
								mults.getNotNull(fn).incrementAndGet();
							}
						}
					} else if(f.opcode() == PUTFIELD || f.opcode() == PUTSTATIC) {
						int encoder = mh.inverseDecoder(f.key());
						if(encoder != 0) {
							int n = nn.number() * encoder;
							//System.out.printf("%s %d * %d = %d.%n", f.fin().key(), nn.number(), encoder, n);
							if(n == 1) {
								mults.getNotNull(fn).incrementAndGet();
							}
						}
					}
				}
			}
		}
	}

	public FieldNode lookup(FieldInsnNode fin) {
		String halfKey = fin.halfKey();
		ClassTree tree = Context.current().getClassTree();
		ClassNode cn = tree.getClass(fin.owner);

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

	private FieldNode find(ClassNode cn, String halfKey) {
		for(FieldNode f : cn.fields) {
			if(f.halfKey().equals(halfKey))
				return f;
		}

		return null;
	}

	public void output() {
		System.err.println("Collecting revertable fields.");
		int k = 0;
		for(Entry<FieldNode, AtomicInteger> e : total.entrySet()) {
			FieldNode fn = e.getKey();
			AtomicInteger i = mults.get(fn);
			if(i != null) {
				if(i.get() == e.getValue().get()) {
					//System.err.println(fn.key() + " equals.");
					k++;
				}
			}
		}

		System.out.printf("   Map= %d:%d.%n", total.size(), mults.size());
		System.out.printf("   Found %d changeable fields.%n", k);
		System.out.printf("   %d fluctuating field values.%n", total.size() - k);
	}

	public static final class CountMap extends NullPermeableMap<FieldNode, AtomicInteger> {
		private static final long serialVersionUID = -176930165575731808L;

		public CountMap() {
			super(new ValueCreator<AtomicInteger>() {
				@Override
				public AtomicInteger create() {
					return new AtomicInteger();
				}
			});
		}
	}
}