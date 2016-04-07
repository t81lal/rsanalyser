package org.topdank.byteengineer.commons.asm;

import java.io.IOException;

import org.objectweb.custom_asm.ClassReader;
import org.objectweb.custom_asm.ClassWriter;
import org.objectweb.custom_asm.tree.ClassNode;

public class DefaultASMFactory implements ASMFactory<ClassNode> {

	@Override
	public ClassNode create(byte[] bytes, String name) throws IOException {
		ClassReader cr = new ClassReader(bytes);
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_FRAMES);
		return cn;
	}

	@Override
	public byte[] write(ClassNode c) {
		ClassWriter cw = new ClassWriter(0);
		c.accept(cw);
		return cw.toByteArray();
	}
}