package org.nullbool.api.util;

import org.objectweb.asm.tree.FieldNode;
import org.topdank.banalysis.filter.Filter;

public class DescFilter implements Filter<FieldNode> {

	private final String desc;
	
	public DescFilter(String desc) {
		this.desc = desc;
	}

	@Override
	public boolean accept(FieldNode t) {
		return t.desc.equals(desc);
	}
}