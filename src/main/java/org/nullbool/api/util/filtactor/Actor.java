package org.nullbool.api.util.filtactor;

import org.objectweb.asm.tree.AbstractInsnNode;

public abstract interface Actor {
	public abstract int act(AbstractInsnNode ain);
}