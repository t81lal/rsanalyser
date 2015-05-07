package org.topdank.banalysis.hooks.matching;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author sc4re
 */
public abstract interface FieldMatcher extends ContainerMatcher<ClassNode, List<FieldNode>> {

	@Override
	public abstract List<FieldNode> match(ClassNode object);
}