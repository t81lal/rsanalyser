package org.nullbool.api.obfuscation.refactor;

import java.util.HashSet;
import java.util.Set;

import org.nullbool.api.util.map.ValueCreator;

public class SetCreator<T> implements ValueCreator<Set<T>> {

	@Override 
	public Set<T> create() {
		return new HashSet<T>();
	}
}