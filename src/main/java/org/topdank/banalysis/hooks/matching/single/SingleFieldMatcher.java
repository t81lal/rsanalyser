package org.topdank.banalysis.hooks.matching.single;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.topdank.banalysis.hooks.matching.ContainerMatcher;

/**
 * @author sc4re
 */
public interface SingleFieldMatcher extends ContainerMatcher<ClassNode, FieldNode> {

	@Override
	public abstract FieldNode match(ClassNode object);
}