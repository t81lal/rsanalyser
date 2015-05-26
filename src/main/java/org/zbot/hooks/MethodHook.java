package org.zbot.hooks;

import java.io.Serializable;

import org.objectweb.asm.tree.InsnList;

public class MethodHook implements Serializable {

	private static final long serialVersionUID = 5848090575172209265L;

	private MethodType type;
	private ClassHook owner;
	private ObfuscatedData name;
	private DynamicDesc desc;
	private boolean isStatic;
	private int maxStack, maxLocals;
	private InsnList instructions;
	private boolean marked;

	public MethodHook() {

	}

	public MethodHook(MethodType type, ClassHook owner, ObfuscatedData name, DynamicDesc desc, boolean isStatic, InsnList instructions) {
		this.type = type;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isStatic = isStatic;
		this.instructions = instructions;
	}

	public MethodType getType() {
		return type;
	}

	public void setType(MethodType type) {
		this.type = type;
	}

	public ClassHook getOwner() {
		return owner;
	}

	public MethodHook setOwner(ClassHook owner) {
		if (this.owner != null)
			this.owner.getMethods().remove(this);

		this.owner = owner;
		owner.getMethods().add(this);
		return this;
	}

	public int getMaxStack() {
		return maxStack;
	}

	public void setMaxStack(int maxStack) {
		this.maxStack = maxStack;
	}

	public int getMaxLocals() {
		return maxLocals;
	}

	public void setMaxLocals(int maxLocals) {
		this.maxLocals = maxLocals;
	}

	public ObfuscatedData getName() {
		return name;
	}

	public MethodHook setName(ObfuscatedData name) {
		this.name = name;
		return this;
	}

	public DynamicDesc getDesc() {
		return desc;
	}

	public MethodHook setDesc(DynamicDesc desc) {
		this.desc = desc;
		return this;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public MethodHook setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return this;
	}

	public InsnList getInstructions() {
		return instructions;
	}

	public MethodHook setInstructions(InsnList instructions) {
		this.instructions = instructions;
		return this;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public String baseToString() {
		return "MethodHook [type=" + type + ", name=" + name + ", desc=" + desc + ", isStatic=" + isStatic + ", instructions="
				+ instructions + ", maxStack=" + maxStack + ", maxLocals=" + maxLocals + ", marked=" + marked + "]";
	}

	@Override
	public String toString() {
		return "MethodHook [type=" + type + ", owner=" + owner + ", name=" + name + ", desc=" + desc + ", isStatic=" + isStatic + ", instructions="
				+ instructions + ", maxStack=" + maxStack + ", maxLocals=" + maxLocals + ", marked=" + marked + "]";
	}

	public enum MethodType {
		PATCH(true), PATCH_START(true), PATCH_END(true), CALLBACK();

		private final boolean isPatch;

		private MethodType() {
			this(false);
		}

		private MethodType(boolean isPatch) {
			this.isPatch = isPatch;
		}

		public boolean isPatch() {
			return isPatch;
		}
	}
}