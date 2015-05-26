package org.topdank.byteio.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.JarResource;
import org.topdank.byteio.util.Debug;

/**
 * Dumps ClassNodes and JarResources back into a file on the local system.
 *
 * @author Bibl
 */
public class CompleteJarDumper implements JarDumper {

	protected JarContents<?> contents;

	/**
	 * Creates a new JarDumper.
	 *
	 * @param contents Contents of jar.
	 */
	public CompleteJarDumper(JarContents<?> contents) {
		this.contents = contents;
	}

	/**
	 * Dumps the jars contents.
	 *
	 * @param file File to dump it to.
	 */
	@Override
	public void dump(File file) throws IOException {
		if (file.exists())
			file.delete();
		file.createNewFile();
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(file));
		int classesDumped = 0;
		int resourcesDumped = 0;
		for (ClassNode cn : contents.getClassContents()) {
			classesDumped += dumpClass(jos, cn.name, cn);
		}
		for (JarResource res : contents.getResourceContents()) {
			resourcesDumped += dumpResource(jos, res.getName(), res.getData());
		}
		if(!Debug.debugging)
			System.out.println("Dumped " + classesDumped + " classes and " + resourcesDumped + " resources to " + file.getAbsolutePath());
		
		jos.close();
	}

	/**
	 * Writes the {@link ClassNode} to the Jar.
	 *
	 * @param out The {@link JarOutputStream}.
	 * @param cn The ClassNode.
	 * @param name The entry name.
	 * @throws IOException If there is a write error.
	 * @return The amount of things dumped, 1 or if you're not dumping it 0.
	 */
	@Override
	public int dumpClass(JarOutputStream out, String name, ClassNode cn) throws IOException {
		JarEntry entry = new JarEntry(cn.name + ".class");
		out.putNextEntry(entry);
		ClassWriter writer = new ClassWriter(0);
		cn.accept(writer);
		out.write(writer.toByteArray());
		return 1;
	}

	/**
	 * Writes a resource to the Jar.
	 *
	 * @param out The {@link JarOutputStream}.
	 * @param name The name of the file.
	 * @param file File as a byte[].
	 * @throws IOException If there is a write error.
	 * @return The amount of things dumped, 1 or if you're not dumping it 0.
	 */
	@Override
	public int dumpResource(JarOutputStream out, String name, byte[] file) throws IOException {
		JarEntry entry = new JarEntry(name);
		out.putNextEntry(entry);
		out.write(file);
		return 1;
	}
}