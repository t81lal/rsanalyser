package org.topdank.banalysis.hooks;

import java.io.Serializable;

public class ObfuscatedData implements Serializable {

	private static final long serialVersionUID = -1204102050670799479L;

	private String obfuscated;
	private String refactored;

	public ObfuscatedData() {

	}

	public ObfuscatedData(String s, boolean refactored) {
		if (refactored) {
			this.refactored = s;
		} else {
			obfuscated = s;
		}
	}

	public ObfuscatedData(String obfuscated, String refactored) {
		this.obfuscated = obfuscated;
		this.refactored = refactored;
	}

	public String getObfuscated() {
		return obfuscated;
	}

	public ObfuscatedData setObfuscated(String obfuscated) {
		this.obfuscated = obfuscated;
		return this;
	}

	public String getRefactored() {
		return refactored;
	}

	public ObfuscatedData setRefactored(String refactored) {
		this.refactored = refactored;
		return this;
	}

	@Override
	public String toString() {
		return "ObfuscatedData [obfuscated=" + obfuscated + ", refactored=" + refactored + "]";
	}
}