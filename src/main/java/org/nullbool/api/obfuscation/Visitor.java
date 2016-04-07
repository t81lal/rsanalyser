package org.nullbool.api.obfuscation;

import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.tree.ClassNode;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * A timed visitor base class for basic transformations.
 * 
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015 21:25:48 (actually before this)
 */
public abstract class Visitor implements Opcodes {

	private final Visitor cv;

	public Visitor(Visitor cv) {
		this.cv = cv;
	}

	public Visitor() {
		this(null);
	}

	/**
	 * Implementation dependent transformer.
	 * @param contents
	 */
	public abstract void visit(JarContents<? extends ClassNode> contents);

	public final void accept(JarContents<? extends ClassNode> contents) {
		if (cv != null) {
			cv.visit(contents);
		}
		visit(contents);
	}

	private long startTime;

	public final void visitStart() {
		this.startTime = System.nanoTime();
	}

	public final void visitEnd() {
		long endTime = System.nanoTime();
		System.out.printf("%s%s%.2f%s%n", "? " + getClass().getSimpleName(), " executed in ", (endTime - startTime) / 1e9, " seconds");
	}
}