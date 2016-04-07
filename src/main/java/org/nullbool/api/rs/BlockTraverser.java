package org.nullbool.api.rs;

import java.util.HashSet;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.tree.MethodInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 14:23:43
 */
public abstract class BlockTraverser implements Opcodes {

	private FlowBlock next;
	
	public BlockTraverser(FlowBlock entry) {
		next = entry;
	}
	
	public void traverseFully() {
		Set<FlowBlock> visited = new HashSet<FlowBlock>();
		while(next != null) {
			if(!visited.contains(next)) {
				step(visited);
				visited.add(next);
			} else {
				break;
			}
		}
	}
	
	public void step(Set<FlowBlock> visited) {
		int op = next.lastOpcode();
		if(InstructionUtil.isExit(op)) {
			exit(next);
		} else if(InstructionUtil.isUnconditional(op)) {
			next = unconditional(next);
		} else if(op == LOOKUPSWITCH) {
			next = lookupswitch(next);
		} else if(op == TABLESWITCH) {
			next = tableswitch(next);
		} else if(InstructionUtil.isConditional(op)) {
			next = conditional(next);
		} else if(next.last() instanceof MethodInsnNode) {
			next = basic(next);
		} else if(next.size() == 0){
//			if(!(next instanceof DummyExitBlock)) {
//				step();
//			}
		} else {
			throw new RuntimeException(String.format("Block %s (%d), insn:%s", next.id(), next.size(), next.last() != null ? next.last().getClass().getSimpleName() : "null"));
		}
	}
	
	protected abstract void exit(FlowBlock block);
	
	protected abstract FlowBlock unconditional(FlowBlock block);
	
	protected abstract FlowBlock lookupswitch(FlowBlock block);
	
	protected abstract FlowBlock tableswitch(FlowBlock block);
	
	protected abstract FlowBlock conditional(FlowBlock block);
	
	protected abstract FlowBlock basic(FlowBlock block);
}