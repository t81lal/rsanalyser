package org.nullbool.api.util.map;

public class NullCreator<V> implements ValueCreator<V> {

	@Override
	public V create() {
		return null;
	}
}