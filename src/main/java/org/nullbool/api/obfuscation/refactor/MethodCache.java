package org.nullbool.api.obfuscation.refactor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodCache {
	private final Map<String, MethodNode> methodMap;
	
	public MethodCache() {
		methodMap = new HashMap<String, MethodNode>();
	}
	public MethodCache(Collection<ClassNode> classes) {
		this();
		put(classes);
	}
	
	//you have no idea how much I wished the compiler inlined these...
	
	public void put(Collection<ClassNode> classes) {
		for(ClassNode cn : classes) {
			put(cn);
		}
	}
	
	public void put(ClassNode cn) {
		for(MethodNode m : cn.methods) {
			put(m);
		}
	}
	
	public void put(MethodNode m) {
		methodMap.put(makeKey(m.owner.name, m.name, m.desc), m);
	}
	
	public void reset() {
		Collection<MethodNode> methods = methodMap.values();
		methodMap.clear();
		for(MethodNode m : methods) {
			put(m);
		}
	}
	
	public static String makeKey(String owner, String name, String desc) {
		return new StringBuilder(owner).append(".").append(name).append(desc).toString();
	}
	
	public MethodNode get(String owner, String name, String desc) {
		return get(makeKey(owner, name, desc));
	}
	
	public MethodNode get(String key) {
		if(methodMap.containsKey(key))
			return methodMap.get(key);
		return null;
	}
	
	public void clear() {
		methodMap.clear();
	}
	
	public int size() {
		return methodMap.size();
	}
}