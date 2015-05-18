package org.nullbool.api.obfuscation.refactor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

public class ClassHelper {

	public static Map<String, ClassNode> convertToMap(Collection<ClassNode> classes) {
		Map<String, ClassNode> map = new HashMap<String, ClassNode>();
		for (ClassNode cn : classes) {
			map.put(cn.name, cn);
		}
		return map;
	}
}