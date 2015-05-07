package org.topdank.banalysis.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HookMap implements Serializable {

	private static final long serialVersionUID = -2199521127019909810L;

	private List<ClassHook> classes;

	public HookMap() {
		classes = new ArrayList<ClassHook>();
	}

	public HookMap(List<ClassHook> classes) {
		this.classes = classes;
	}

	public List<ClassHook> getClasses() {
		return classes;
	}

	public void setClasses(List<ClassHook> classes) {
		this.classes = classes;
	}

	public ClassHook byObfName(String n) {
		for (ClassHook c : classes) {
			if (c.getObfuscated().equals(n))
				return c;
		}
		return null;
	}

	public ClassHook byRefacName(String n) {
		for (ClassHook c : classes) {
			if (c.getRefactored().equals(n))
				return c;
		}
		return null;
	}

	public void write(OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
		oos.close();
	}

	public static HookMap read(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		return (HookMap) ois.readObject();
	}
}