package org.nullbool.api.obfuscation.flow;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;
import org.topdank.banalysis.filter.Filter;

public enum BlockType {
	EMPTY(new Filter<FlowBlock>() {
		@Override
		public boolean accept(FlowBlock block) {
			return block.size() == 1 && block.accept(new Filter<AbstractInsnNode>() {
				@Override
				public boolean accept(AbstractInsnNode ai) {
					return ai.opcode() == Opcodes.GOTO;
				}
			});
		}
	}),
	END(new Filter<FlowBlock>() {
		@Override
		public boolean accept(FlowBlock block) {
			AbstractInsnNode ain = block.last();
			return ain != null && Printer.OPCODES[ain.opcode()].endsWith("RETURN");
		}
	}),
	IMMEDIATE(new Filter<FlowBlock>() {
		@Override
		public boolean accept(FlowBlock block) {
			return !EMPTY.filter.accept(block) && !END.filter.accept(block);
		}
	});

	protected final Filter<FlowBlock> filter;

	BlockType(Filter<FlowBlock> filter) {
		this.filter = filter;
	}
}