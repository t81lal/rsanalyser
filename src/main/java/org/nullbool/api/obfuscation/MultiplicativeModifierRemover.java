package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.Context;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class MultiplicativeModifierRemover extends NodeVisitor {

	private final MultiplicativeModifierCollector collector;
	private final Set<FieldNode> fields = new HashSet<FieldNode>();
	private final NullPermeableHashMap<MethodNode, List<AbstractInsnNode>> toRemove = new NullPermeableHashMap<MethodNode, List<AbstractInsnNode>>(new ValueCreator<List<AbstractInsnNode>>() {
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

		System.out.println("le multi " + collector.mh.getEncoder("dj.e") + " " + collector.mh.getDecoder("dj.e") + " " + collector.mh.inverseDecoder("dj.e"));
		//		System.out.println("multi for it is " + collector.mh.getEncoder("dj.f") + " " + collector.mh.getDecoder("dj.f") + " " + collector.mh.inverseDecoder("dj.f"));
		System.out.println(-2078860851 * 1472428805);
		System.out.println(-2078860851 * 1782921573);
		System.out.println(1472428805 * 1782921573);

		System.out.println((1763674376 * 1782921573) * -2078860851);

		System.out.println("asd " + 436273943 * 1512989863);

		System.out.println("ass " + (-1304982511 * 1512989863));
	}

	@Override
	public void visitOperation(ArithmeticNode an) {
		//		if(an.method().key().startsWith("dj.")) {
		FieldMemberNode f = an.firstField();
		if(f == null)
			return;

		FieldNode fn = collector.lookup(f.fin());

		if(fn == null) {
			return;
		}

		if(!fn.key().equals("dj.eI")) {
			return;
		}


		NumberNode nn = an.firstNumber();
		if(f != null && nn != null) {
			String key = f.owner() + "." + f.name();
			int dec = 1;

			if(f.opcode() == PUTSTATIC || f.opcode() == PUTFIELD || an.opcode() == IMUL) {
				dec = collector.mh.inverseDecoder(key);
			} else {
				dec = collector.mh.getEncoder(key);
			}

			int newVal = nn.number() * dec;
			System.out.printf("%s %s (%s) %d * %d -> %d.%n", Printer.OPCODES[an.opcode()], Printer.OPCODES[f.opcode()], an.method(), nn.number(), dec, newVal);
			nn.setNumber(newVal);
		}
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

		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Undoing Multiplicative Number Obfuscation.");
			System.out.printf("   Changed %d fields.%n", fields.size());
			System.out.printf("   Removed %d (%d insns) field calls (with consts).%n", removed, k);
		}

		fields.clear();
		toRemove.clear();
	}
}