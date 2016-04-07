package org.nullbool.api.util.filtactor;

import org.objectweb.custom_asm.tree.AbstractInsnNode;

public abstract interface Actor {
	public abstract int act(AbstractInsnNode ain);
}