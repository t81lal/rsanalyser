package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.util.map.NullPermeableMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class MultiplicativeModifierRemover extends NodeVisitor {

	private final MultiplicativeModifierCollector collector;
	private final Set<FieldNode> fields = new HashSet<FieldNode>();
	private final NullPermeableMap<MethodNode, List<AbstractInsnNode>> toRemove = new NullPermeableMap<MethodNode, List<AbstractInsnNode>>(new ValueCreator<List<AbstractInsnNode>>() {
		@Override
		public List<AbstractInsnNode> create() {
			return new ArrayList<AbstractInsnNode>();
		}
	});
	private int removed;

	public MultiplicativeModifierRemover(MultiplicativeModifierCollector collector) {
		this.collector = collector;

		for(Entry<FieldNode, AtomicInteger> e : collector.total.entrySet()) {
			FieldNode fn = e.getKey();
			AtomicInteger i = collector.mults.get(fn);
			if(i != null) {
				if(i.get() == e.getValue().get()) {
					fields.add(fn);
				}
			}
		}
	}

	@Override
	public void visitOperation(ArithmeticNode an) {
		if(an.children() < 1)
			return;

		FieldMemberNode f = an.firstField();
		FieldNode fn = null;
		if(f != null) {
			fn = collector.lookup(f.fin());
			if(fn == null)
				return;

			//			if(!fields.contains(fn))
			//				return;
		} else {
			return;
		}

		if(fn.key().equals("dh.aI")) {
			NumberNode nn = an.firstNumber();
			if(nn != null) {
				int num = nn.number();
				int decoder = 0;
				if(an.subtracting() || an.adding()) {
					decoder = collector.mh.getEncoder(f.key());
				} else {
					decoder = collector.mh.inverseDecoder(f.key());
				}
				int val = num * decoder;
				nn.setNumber(val);
				//System.out.printf("(%s) %s %s [%d * %d] = %d.%n", an.method(), Printer.OPCODES[an.opcode()], fn.key(), num, decoder, val);
			}
		}
		//		NumberNode nn = an.firstNumber();
		//		if(nn != null) {
		//			toRemove.getNotNull(an.method()).add(nn.insn());
		//			toRemove.getNotNull(an.method()).add(an.insn());
		//			removed++;
		//		}
	}

	public void output() {
		int k = 0;
		for(Entry<MethodNode, List<AbstractInsnNode>> e : toRemove.entrySet()) {
			for(AbstractInsnNode ain : e.getValue()) {
				e.getKey().instructions.remove(ain);
				k++;
			}
		}

		System.err.println("Undoing Multiplicative Number Obfuscation.");
		System.out.printf("   Changed %d fields.%n", fields.size());
		System.out.printf("   Removed %d (%d insns) field calls (with consts).%n", removed, k);

		fields.clear();
		toRemove.clear();
	}
}