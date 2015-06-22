package org.nullbool.pi.core.hook.api;

import java.io.Serializable;

public class InterfaceMapping implements Serializable {

	private static final long serialVersionUID = 5780052502265543139L;

	private ClassHook owner;
	private String canonicalName;

	public InterfaceMapping(ClassHook owner, String canonicalName) {
		this.owner = owner;
		this.canonicalName = canonicalName;
	}

	public ClassHook getOwner() {
		return owner;
	}

	public InterfaceMapping setOwner(ClassHook owner) {
		if (this.owner != null)
			this.owner.interfaces().remove(this);

		this.owner = owner;
		owner.interfaces().add(this);
		return this;
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	public InterfaceMapping setCanonicalName(String canonicalName) {
		this.canonicalName = canonicalName;
		return this;
	}
}