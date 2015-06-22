package org.nullbool.pi.core.hook.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ObfuscatedData implements Serializable {
	
	private static final long serialVersionUID = -1204102050670799479L;
	public static final String OBFUSCATED = "name.obfuscated";
	public static final String REFACTORED = "name.refactored";

	private final Map<String, String> variables = new HashMap<String, String>();

	public ObfuscatedData() {
	}

	public ObfuscatedData(String s, boolean refactored) {
		var(refactored ? REFACTORED : OBFUSCATED, s);
	}

	public ObfuscatedData(String obfuscated, String refactored) {
		obfuscated(obfuscated).refactored(refactored);
	}
	
	public Map<String, String> variables() {
		return variables;
	}
	
	public ObfuscatedData var(String key, String value) {
		variables.put(key, value);
		return this;
	}
	
	public String val(String key) {
		return variables.get(key);
	}
	
	public String val(String key, String dflt) {
		if(!variables.containsKey(key))
			return dflt;
		return variables.get(key);
	}

	public String obfuscated() {
		return variables.get(OBFUSCATED);
	}

	public ObfuscatedData obfuscated(String obfuscated) {
		var(OBFUSCATED, obfuscated);
		return this;
	}

	public String refactored() {
		return variables.get(REFACTORED);
	}

	public ObfuscatedData refactored(String refactored) {
		var(REFACTORED, refactored);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ObfuscatedData");
		sb.append("[").append("variables=").append(variables).append("]");
		return sb.toString();
	}
}