package org.nullbool.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassWriter;

public class ClassRepository {

	public static Map<String, ClassStructure> fromJar(File file) {
		Map<String, ClassStructure> classes = new HashMap<String, ClassStructure>();
		try (JarFile jf = new JarFile(file)) {
			@SuppressWarnings("resource")
			JarInputStream in = new JarInputStream(new FileInputStream(file));
			for (JarEntry entry = in.getNextJarEntry(); entry != null; entry = in.getNextJarEntry()) {
				String entryName = entry.getName();
				if (entryName.endsWith(".class")) {
					ClassStructure cs = ClassStructure.create(jf.getInputStream(entry));
					classes.put(entryName.replace(".class", ""), cs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}

	public static void save(Map<String, ClassStructure> classes, String name) {
		try (JarOutputStream output = new JarOutputStream(new FileOutputStream(new File(name)))) {
			for (Map.Entry<String, ClassStructure> entry : classes.entrySet()) {
				output.putNextEntry(new JarEntry(entry.getKey().replaceAll("\\.", "/") + ".class"));
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				entry.getValue().accept(writer);
				output.write(writer.toByteArray());
				output.closeEntry();
			}
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}