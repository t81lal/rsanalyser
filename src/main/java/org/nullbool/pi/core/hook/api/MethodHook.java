package org.nullbool.pi.core.hook.api;

import org.objectweb.asm.tree.InsnList;

public class MethodHook extends ObfuscatedData {

	private static final long serialVersionUID = 5848090575172209265L;
	public static final String DESC = "desc.obfuscated";
	public static final String TYPE = "attr.type";
	public static final String STATIC = "attr.static";
	public static final String MAX_STACK = "attr.maxs";
	public static final String MAX_LOCALS = "attr.maxl";
	public static final String CALLBACK = "callback";
	public static final String PATCH = "patch";
	public static final String PATCH_POSITION = "attr.type.patch.pos";
	public static final String START = "attr.type.patch.pos.start";
	public static final String END = "attr.type.patch.pos.end";
	public static final String SAFE_OPAQUE = "attr.safeopaque";

	private ClassHook owner;
	private InsnList instructions;

	public MethodHook() {
	}

	public MethodHook(ClassHook owner) {
		this.owner = owner;
	}
	
	public MethodHook(InsnList instructions) {
		this.instructions = instructions;
	}

	@Override
	public MethodHook var(String name, String value) {
		super.var(name, value);
		return this;
	}
	
	@Override
	public MethodHook obfuscated(String obfuscated) {
		super.obfuscated(obfuscated);
		return this;
	}
	
	@Override
	public MethodHook refactored(String refactored) {
		super.refactored(refactored);
		return this;
	}
	
	public ClassHook owner() {
		return owner;
	}

	public MethodHook owner(ClassHook owner) {
		if (this.owner != null)
			this.owner.methods().remove(this);

		this.owner = owner;
		owner.methods().add(this);
		return this;
	}

	public InsnList insns() {
		return instructions;
	}

	public MethodHook insns(InsnList instructions) {
		this.instructions = instructions;
		return this;
	}
	
	public String baseToString() {
		StringBuilder sb = new StringBuilder("MethodHook");
		sb.append(" [\n");
		sb.append(Util.mapToString(variables(), "\t", "\n"));
		sb.append("\tinsns.size=").append(instructions == null ? "null" : instructions.size()).append("\n");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MethodHook");
		sb.append(" [\n");
		sb.append("\towner=").append(owner.baseToString()).append("\n");
		sb.append(Util.mapToString(variables(), "\t", "\n"));
		sb.append("\tinsns.size=").append(instructions == null ? "null" : instructions.size()).append("\n");
		sb.append("]");
		return sb.toString();
	}
}