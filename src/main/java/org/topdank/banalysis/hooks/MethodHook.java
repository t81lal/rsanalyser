package org.topdank.banalysis.hooks;

import java.io.Serializable;

import org.objectweb.asm.tree.InsnList;

public class MethodHook implements Serializable {

	private static final long serialVersionUID = 5848090575172209265L;

	private ClassHook owner;
	private ObfuscatedData name;
	private DynamicDesc desc;
	private boolean isStatic;
	private InsnList instructions;
	private boolean marked;

	public MethodHook() {

	}

	public MethodHook(ClassHook owner, ObfuscatedData name, DynamicDesc desc, boolean isStatic, InsnList instructions) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isStatic = isStatic;
		this.instructions = instructions;
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

	@Override
	public String toString() {
		return "MethodHook [owner=" + owner + ", name=" + name + ", desc=" + desc + ", isStatic=" + isStatic + ", instructions=" + instructions + ", marked=" + marked + "]";
	}
}