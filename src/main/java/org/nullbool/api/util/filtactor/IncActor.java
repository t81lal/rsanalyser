package org.nullbool.api.util.filtactor;

import static org.nullbool.api.util.InstructionUtil.next;
import static org.nullbool.api.util.InstructionUtil.resolve;

import org.nullbool.api.util.IntMap;
import org.objectweb.custom_asm.tree.AbstractInsnNode;

public class IncActor implements Actor {
	private final IntMap countMap;

	public IncActor() {
		countMap = new IntMap();
	}

	public IncActor(IntMap countMap) {
		this.countMap = countMap;
	}

	@Override
	public int act(AbstractInsnNode ain) {
		countMap.inc(resolve(next(ain.getNext())));
		return 0;
	}

	public IntMap getCountMap() {
		return countMap;
	}
}
