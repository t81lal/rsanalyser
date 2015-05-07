package org.topdank.banalysis.hooks.matching.single;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.topdank.banalysis.hooks.matching.ContainerMatcher;

/**
 * @author sc4re
 */
public abstract interface SingleClassMatcher extends ContainerMatcher<List<ClassNode>, ClassNode> {

	@Override
	public abstract ClassNode match(List<ClassNode> object);
}