package org.nullbool.api.obfuscation.rename;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.nullbool.api.obfuscation.rename.InheritanceTree.MethodData;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 6 Apr 2015 at 23:47:45 <br>
 */
public class RefactorMapper extends Remapper {

	private final HookMap hookMap;
	private final InheritanceTree inheritanceTree;

	public RefactorMapper(Map<String, ClassNode> classes, HookMap hookMap) {
		this.hookMap = hookMap;
		Collection<MethodHook> mhs = new HashSet<MethodHook>();
		for (ClassHook h : hookMap.getClasses()) {
			for (MethodHook mh : h.getMethods()) {
				mhs.add(mh);
			}
		}
		inheritanceTree = new InheritanceTree(classes, mhs.toArray(new MethodHook[0]));
		inheritanceTree.printData();
	}

	private ClassHook getByName(String obf) {
		for (ClassHook c : hookMap.getClasses()) {
			if (c.getObfuscated().equals(obf))
				return c;
		}
		return null;
	}

	@Override
	public String map(String type) {
		ClassHook c = getByName(type);
		if (c != null)
			return c.getRefactored();
		return type;
	}

	@Override
	public String mapFieldName(String owner, String name, String desc) {
		ClassHook c = getByName(owner);
		if (c == null)
			return name;
		for (FieldHook f : c.getFields()) {
			if (f.getName().getObfuscated().equals(name) && f.getDesc().getObfuscated().equals(desc))
				return f.getName().getRefactored();
		}
		return name;
	}

	@Override
	public String mapMethodName(String owner, String name, String desc) {
		// String newName = methodMap.get(owner + ":" + name + ":" + desc);
		// if (newName != null)
		// return newName;
		MethodData data = inheritanceTree.getMethodData(owner, name, desc);
		if (data != null)
			return data.getName();
		return name;
	}
}