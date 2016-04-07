package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.nullbool.api.Context;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * Removes POP and constant loading instructions 
 * in the code that are directly popping a 
 * constant (number). <br>
 * 
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class EmptyPopRemover extends NodeVisitor {

	private final NullPermeableHashMap<MethodNode, List<AbstractInsnNode>> toRemove = new NullPermeableHashMap<MethodNode, List<AbstractInsnNode>>(new ValueCreator<List<AbstractInsnNode>>() {
		@Override
		public List<AbstractInsnNode> create() {
			return new ArrayList<AbstractInsnNode>();
		}
	});
	
	private int removed, unremovable, chosenUnremovable;
	
	@Override
	public void visit(AbstractNode n) {
		if(n.opcode() == POP) {
			if(n.children() > 0) {
				AbstractNode child = n.child(0);
				if(child instanceof NumberNode) {
					toRemove.getNonNull(n.method()).add(n.insn());
					toRemove.get(n.method()).add(child.insn());
					removed++;
				} else {
					chosenUnremovable++;
				}
				//if(child instanceof ConstantNode || child || )
			} else {
				unremovable++;
			}
		}
	}

	public void output() {
		int i = 0;
		for(Entry<MethodNode, List<AbstractInsnNode>> e : toRemove.entrySet()) {
			for(AbstractInsnNode ain : e.getValue()) {
				e.getKey().instructions.remove(ain);
				i++;
			}
		}

		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Running empty pop remover.");
			System.out.printf("   Collapsed %d pops (%d).%n", removed, i / 2);
			System.out.printf("   Was unable to remove %d pops.%n", unremovable);
			System.out.printf("   Chose not to remove %d pops.%n", chosenUnremovable);
		}
		
		toRemove.clear();
	}
}