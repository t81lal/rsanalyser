package org.nullbool.api.rs;

import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

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
		while(next != null) {
			step();
		}
	}
	
	public void step() {
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
		} else {
			throw new RuntimeException();
		}
	}
	
	protected abstract void exit(FlowBlock block);
	
	protected abstract FlowBlock unconditional(FlowBlock block);
	
	protected abstract FlowBlock lookupswitch(FlowBlock block);
	
	protected abstract FlowBlock tableswitch(FlowBlock block);
	
	protected abstract FlowBlock conditional(FlowBlock block);
	
	protected abstract FlowBlock basic(FlowBlock block);
}