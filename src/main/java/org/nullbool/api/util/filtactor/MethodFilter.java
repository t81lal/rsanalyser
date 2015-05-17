package org.nullbool.api.util.filtactor;

import org.objectweb.asm.tree.MethodNode;

public class MethodFilter implements Filter<MethodNode> {

	private final MethodNode orig;

	public MethodFilter(MethodNode orig) {
		this.orig = orig;
	}

	@Override
	public MethodNode accept(MethodNode m) {
		boolean pass = orig.name.equals(m.name) && orig.desc.equals(m.desc);
		if (pass)
			return m;
		return null;
	}
}