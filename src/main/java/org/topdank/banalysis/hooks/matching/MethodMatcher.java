package org.topdank.banalysis.hooks.matching;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author sc4re
 */
public abstract interface MethodMatcher extends ContainerMatcher<ClassNode, List<MethodNode>> {

	@Override
	public abstract List<MethodNode> match(ClassNode object);
}