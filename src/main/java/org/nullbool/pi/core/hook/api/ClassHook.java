package org.nullbool.pi.core.hook.api;

import java.util.ArrayList;
import java.util.List;

public class ClassHook extends ObfuscatedData {

	private static final long serialVersionUID = -6864421229081291283L;

	private final List<InterfaceMapping> interfaces = new ArrayList<InterfaceMapping>();
	private final List<FieldHook> fields            = new ArrayList<FieldHook>();
	private final List<MethodHook> methods          = new ArrayList<MethodHook>();

	public ClassHook() {
	}

	public ClassHook(String name, boolean refactored) {
		super(name, refactored);
	}

	public ClassHook(String obfuscated, String refactored) {
		super(obfuscated, refactored);
	}

	@Override
	public ClassHook var(String name, String value) {
		super.var(name, value);
		return this;
	}
	
	@Override
	public ClassHook obfuscated(String obfuscated) {
		super.obfuscated(obfuscated);
		return this;
	}
	
	@Override
	public ClassHook refactored(String refactored) {
		super.refactored(refactored);
		return this;
	}
	
	public List<InterfaceMapping> interfaces() {
		return interfaces;
	}

	public List<FieldHook> fields() {
		return fields;
	}

	public List<MethodHook> methods() {
		return methods;
	}
	
	public String baseToString() {
		return "ClassHook [getObfuscated()=" + obfuscated() + ", getRefactored()=" + refactored() + "]";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(baseToString()).append("\n");
		for(FieldHook f : fields) {
			sb.append("   ").append(f.baseToString()).append("\n");
		}
		
		for(MethodHook m : methods) {
			sb.append("   ").append(m.baseToString()).append("\n");
		}
		return sb.toString();
	}
}