package org.nullbool.api.obfuscation.refactor;

import java.util.HashSet;
import java.util.Set;

import org.nullbool.api.util.map.ValueCreator;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public class SetCreator<T> implements ValueCreator<Set<T>> {

	@Override 
	public Set<T> create() {
		return new HashSet<T>();
	}
}