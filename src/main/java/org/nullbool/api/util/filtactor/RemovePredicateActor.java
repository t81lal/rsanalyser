package org.nullbool.api.util.filtactor;

import static org.nullbool.api.util.InstructionUtil.next;

import org.nullbool.api.util.IntMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class RemovePredicateActor implements Actor {

	private final IntMap counter;
	private MethodNode method;

	public RemovePredicateActor(IntMap counter) {
		this.counter = counter;
	}

	@Override
	public int act(AbstractInsnNode _ain) {
		counter.inc(next(_ain.getNext().getNext()).getOpcode());
		AbstractInsnNode ain = _ain;
		AbstractInsnNode[] ains = new AbstractInsnNode[7];
		for (int i = 0; i < ains.length; i++) {
			ains[i] = (ain == null ? ains[i - 1] : ain);
			ain = ain.getNext();
		}
		// have to traverse backwards to remove the instructions as the nodes are linked
		for (int i = ains.length - 1; i >= 0; i--) {
			method.instructions.remove(ains[i]);
		}
		return ains.length - 1;
	}

	public void setMethod(MethodNode m) {
		method = m;
	}
}