package org.topdank.byteengineer.commons.asm;

import java.io.IOException;

import org.objectweb.custom_asm.tree.ClassNode;

public abstract interface ASMFactory<C extends ClassNode> {

	public abstract C create(byte[] bytes, String name) throws IOException;

	public abstract byte[] write(C c);
}