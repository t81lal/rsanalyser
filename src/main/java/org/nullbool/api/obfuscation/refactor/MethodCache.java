package org.nullbool.api.obfuscation.refactor;

import java.util.Collection;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 8 Jul 2015 03:23:35
 */
public class MethodCache extends DataCache<MethodNode> {

	/**
	 * @param classes
	 */
	public MethodCache(Collection<ClassNode> classes) {
		super(classes);
	}

	/**
	 * @param filter
	 */
	public MethodCache(Filter<MethodNode> filter) {
		super(filter);
	}

	/**
	 * @param filter
	 * @param classes
	 */
	public MethodCache(Filter<MethodNode> filter, Collection<ClassNode> classes) {
		super(filter, classes);
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.obfuscation.refactor.DataCache#put(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	public void put(ClassNode cn) {
		for(MethodNode m : cn.methods) {
			put(m);
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.obfuscation.refactor.DataCache#makeKey(java.lang.Object)
	 */
	@Override
	public String makeKey(MethodNode t) {
		return makeKey(t.owner.name, t.name, t.desc);
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.obfuscation.refactor.DataCache#put(java.lang.Object)
	 */
	@Override
	public void put(MethodNode m) {
		if(canCache(m)) {
			put(makeKey(m), m);
			m.cacheKey();
		}
	}
}