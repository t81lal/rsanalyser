package org.nullbool.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarFile;

import org.nullbool.api.util.JarParser;
import org.objectweb.asm.tree.ClassNode;

public class Revision {

	private final String name;
	private final File dataFile;

	public Revision(String name, File dataFile) {
		this.name = name;
		this.dataFile = dataFile;
	}

	public String getName() {
		return name;
	}

	public File getDataFile() {
		return dataFile;
	}

	public Map<String, ClassNode> parse() throws IOException {
		return new JarParser(new JarFile(dataFile)).getParsedClasses();
		// return (Map) ClassRepository.fromJar(dataFile);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataFile == null) ? 0 : dataFile.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Revision other = (Revision) obj;
		if (dataFile == null) {
			if (other.dataFile != null)
				return false;
		} else if (!dataFile.equals(other.dataFile))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Revision [name=" + name + ", dataFile=" + dataFile + "]";
	}
}