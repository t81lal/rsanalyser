package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.nullbool.api.util.map.NullPermeableMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class EmptyPopRemover extends NodeVisitor {

	private int removed, unremovable, chosenUnremovable;
	private NullPermeableMap<MethodNode, List<AbstractInsnNode>> toRemove = new NullPermeableMap<MethodNode, List<AbstractInsnNode>>(new ValueCreator<List<AbstractInsnNode>>() {
		@Override
		public List<AbstractInsnNode> create() {
			return new ArrayList<AbstractInsnNode>();
		}
	});
	
	@Override
	public void visit(AbstractNode n) {
		if(n.opcode() == POP) {
			if(n.children() > 0) {
				AbstractNode child = n.child(0);
				if(child instanceof NumberNode) {
					toRemove.getNotNull(n.method()).add(n.insn());
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
		
		System.err.println("Removing empty pop remover.");
		System.out.printf("   Collapsed %d pops (%d).%n", removed, i / 2);
		System.out.printf("   Was unable to remove %d pops.%n", unremovable);
		System.out.printf("   Chose not to remove %d pops.%n", chosenUnremovable);
	}
}