package org.nullbool.api.util.filtactor;

import org.objectweb.custom_asm.tree.AbstractInsnNode;

public class BooleanActor implements Actor {

	private boolean state;

	public BooleanActor() {
		this(false);
	}

	public BooleanActor(boolean state) {
		this.state = state;
	}

	@Override
	public int act(AbstractInsnNode ain) {
		state = true;
		return 0;
	}

	public void reset() {
		state = false;
	}

	public boolean state() {
		return state;
	}
}