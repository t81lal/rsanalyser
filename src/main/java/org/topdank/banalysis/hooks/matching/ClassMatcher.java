package org.topdank.banalysis.hooks.matching;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

/**
 * @author sc4re
 */
public abstract interface ClassMatcher extends ContainerMatcher<List<ClassNode>, List<ClassNode>> {

	@Override
	public abstract List<ClassNode> match(List<ClassNode> object);
}