package org.topdank.banalysis.hooks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClassHook extends ObfuscatedData implements Serializable {

	private static final long serialVersionUID = -6864421229081291283L;

	private List<InterfaceMapping> interfaces;
	private List<FieldHook> fields;
	private List<MethodHook> methods;

	public ClassHook() {
		initLists();
	}

	public ClassHook(String s, boolean refactored) {
		super(s, refactored);
		initLists();
	}

	public ClassHook(String obfuscated, String refactored) {
		super(obfuscated, refactored);
		initLists();
	}

	private void initLists() {
		interfaces = new ArrayList<InterfaceMapping>();
		fields = new ArrayList<FieldHook>();
		methods = new ArrayList<MethodHook>();
	}

	public List<InterfaceMapping> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<InterfaceMapping> interfaces) {
		this.interfaces = interfaces;
	}

	public List<FieldHook> getFields() {
		return fields;
	}

	public void setFields(List<FieldHook> fields) {
		this.fields = fields;
	}

	public List<MethodHook> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodHook> methods) {
		this.methods = methods;
	}

	@Override
	public String toString() {
		return "ClassHook [getObfuscated()=" + getObfuscated() + ", getRefactored()=" + getRefactored() + "]";
	}
}