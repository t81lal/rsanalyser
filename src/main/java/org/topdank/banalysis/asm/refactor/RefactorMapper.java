package org.topdank.banalysis.asm.refactor;

import java.util.Map;

import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.topdank.banalysis.asm.refactor.InheritanceTree.MethodData;
import org.topdank.banalysis.hooks.ClassHook;
import org.topdank.banalysis.hooks.FieldHook;
import org.topdank.banalysis.hooks.HookMap;
import org.topdank.banalysis.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 6 Apr 2015 at 23:47:45 <br>
 */
public class RefactorMapper extends Remapper {

	private final HookMap hookMap;
	private final InheritanceTree inheritanceTree;

	public RefactorMapper(Map<String, ClassNode> classes, HookMap hookMap) {
		this.hookMap = hookMap;
		MethodHook hook = hookMap.getClasses().get(0).getMethods().get(0);
		inheritanceTree = new InheritanceTree(classes, hook);
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