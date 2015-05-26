package org.nullbool.api.obfuscation.cfg;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

public class MarkerInsnNode extends AbstractInsnNode {

	public final FlowBlock block;
	
	public MarkerInsnNode(FlowBlock block) {
		super(-1);
		this.block = block;
	}

	@Override
	public int type() {
		return 16;
	}

	@Override
	public void accept(MethodVisitor cv) {
		
	}

	@Override
	public AbstractInsnNode clone(Map<LabelNode, LabelNode> labels) {
		return null;
	}
	
	@Override
	public String toString() {
		return block.id();
	}
}