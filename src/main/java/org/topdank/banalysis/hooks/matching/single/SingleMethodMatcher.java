package org.topdank.banalysis.hooks.matching.single;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.hooks.matching.ContainerMatcher;

/**
 * @author sc4re
 */
public abstract interface SingleMethodMatcher extends ContainerMatcher<ClassNode, MethodNode> {

	@Override
	public abstract MethodNode match(ClassNode object);
}
