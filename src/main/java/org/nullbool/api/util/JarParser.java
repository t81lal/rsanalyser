package org.nullbool.api.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.tree.ClassNode;

public class JarParser {

	private final Map<String, ClassNode> classes;

	public JarParser(JarFile j) throws IOException {
		classes = new NodeTable<ClassNode>();
		try {
			Enumeration<JarEntry> entries = j.entries();
			while (entries.hasMoreElements()) {
				JarEntry en = entries.nextElement();
				if (en.getName().endsWith(".class")) {
					// final ClassNode cn = new ClassNode();
					// final int skip = ClassReader.SKIP_FRAMES;
					// new ClassReader(j.getInputStream(en)).accept(cn, skip);
					ClassNode cn = ClassStructure.create(j.getInputStream(en));
					classes.put(cn.name, cn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return a table of parsed classes
	 */
	public Map<String, ClassNode> getParsedClasses() {
		return classes;
	}
}