package org.zbot.hooks;

import java.io.Serializable;

public class FieldHook implements Serializable {

	private static final long serialVersionUID = 5858100799382432079L;

	private ClassHook owner;
	private ObfuscatedData name;
	private DynamicDesc desc;
	private boolean isStatic;
	private long multiplier;
	private boolean marked;

	public FieldHook() {

	}

	public FieldHook(ClassHook owner, ObfuscatedData name, DynamicDesc desc, boolean isStatic, long multiplier) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isStatic = isStatic;
		this.multiplier = multiplier;
	}

	public ClassHook getOwner() {
		return owner;
	}

	public FieldHook setOwner(ClassHook owner) {
		if (this.owner != null)
			this.owner.getFields().remove(this);

		this.owner = owner;
		owner.getFields().add(this);
		return this;
	}

	public ObfuscatedData getName() {
		return name;
	}

	public FieldHook setName(ObfuscatedData name) {
		this.name = name;
		return this;
	}

	public DynamicDesc getDesc() {
		return desc;
	}

	public FieldHook setDesc(DynamicDesc desc) {
		this.desc = desc;
		return this;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public FieldHook setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return this;
	}

	public long getMultiplier() {
		return multiplier;
	}

	public FieldHook setMultiplier(long multiplier) {
		this.multiplier = multiplier;
		return this;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public String baseToString() {
		return "FieldHook  [name=" + name + ", desc=" + desc + ", isStatic=" + isStatic + ", multiplier=" + multiplier + ", marked=" + marked + "]";
	}

	@Override
	public String toString() {
		return "FieldHook [owner=" + owner.baseToString() + ", name=" + name + ", desc=" + desc + ", isStatic=" + isStatic + ", multiplier=" + multiplier + ", marked=" + marked + "]";
	}
}