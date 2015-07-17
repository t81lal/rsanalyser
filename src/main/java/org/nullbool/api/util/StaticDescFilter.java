package org.nullbool.api.util;

import java.lang.reflect.Modifier;

import org.objectweb.asm.tree.FieldNode;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 17 Jul 2015 16:25:22
 */
public class StaticDescFilter implements Filter<FieldNode> {

	private final String desc;
	private final boolean isStatic;
	
	public StaticDescFilter(String desc) {
		this.desc = desc;
		isStatic = false;
	}
	
	public StaticDescFilter(String desc, boolean isStatic) {
		this.desc = desc;
		this.isStatic = isStatic;
	}

	@Override
	public boolean accept(FieldNode t) {
		return Modifier.isStatic(t.access) == isStatic && t.desc.equals(desc);
	}
}