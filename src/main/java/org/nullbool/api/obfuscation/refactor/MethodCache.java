package org.nullbool.api.obfuscation.refactor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public class MethodCache {
	private final Filter<MethodNode> filter;
	private final Map<String, MethodNode> methodMap;
	
	public MethodCache(Filter<MethodNode> filter) {
		this.filter = filter;
		methodMap = new HashMap<String, MethodNode>();
	}
	
	public MethodCache(Filter<MethodNode> filter,Collection<ClassNode> classes) {
		this(filter);
		put(classes);
	}
	
	public MethodCache(Collection<ClassNode> classes) {
		this(Filter.acceptAll());
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
		if(filter.accept(m)){
			methodMap.put(makeKey(m.owner.name, m.name, m.desc), m);
			m.cacheKey();
		}
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