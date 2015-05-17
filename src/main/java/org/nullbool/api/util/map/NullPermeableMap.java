package org.nullbool.api.util.map;

import java.util.HashMap;

public class NullPermeableMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private final ValueCreator<V> creator;

	public NullPermeableMap(ValueCreator<V> creator) {
		this.creator = creator;
	}

	public NullPermeableMap() {
		this(new NullCreator<V>());
	}

	public V getNotNull(K k) {
		V val = get(k);
		if (val == null) {
			val = creator.create();
			put(k, val);
		} 
		return val;
	}

	private static class NullCreator<V> implements ValueCreator<V> {

		@Override
		public V create() {
			return null;
		}
	}
}