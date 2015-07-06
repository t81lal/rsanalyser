package org.nullbool.pi.core.hook.api;

import org.objectweb.asm.tree.InsnList;

public class MethodHook extends ObfuscatedData {

	private static final long serialVersionUID = 5848090575172209265L;
	
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