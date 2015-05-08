package org.nullbool.api.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class IntMap extends HashMap<Integer, Integer> {
	private static final long serialVersionUID = 1313743266412543203L;

	// public Set<Integer> getUseKeys() {
	// Set<Integer> set = new HashSet<Integer>();
	// int highestCount = Integer.MIN_VALUE;
	// for (Entry<Integer, Integer> e : entrySet()) {
	// if(e.getKey() != )
	// if (e.getValue() == highestCount) {
	// throw new IllegalArgumentException("multi: " + e.getKey());
	// } else if (e.getValue() > highestCount) {
	// highestCount = e.getValue();
	// }
	// }
	// return set;
	// }

	public int getLargestKey() {
		int highestVal = 0;
		int highestCount = Integer.MIN_VALUE;
		for (Entry<Integer, Integer> e : entrySet()) {
			if (e.getValue() == highestCount) {
				throw new IllegalArgumentException("multi: " + e.getKey());
			} else if (e.getValue() > highestCount) {
				highestCount = e.getValue();
				highestVal = e.getKey();
			}
		}
		return highestVal;
	}

	public Set<Integer> findEmptyVars() {
		Set<Integer> empty = new HashSet<Integer>();
		for (Entry<Integer, Integer> e : entrySet()) {
			if (e.getValue() == 0) {
				empty.add(e.getKey());
			}
		}
		return empty;
	}

	public void put0(int var) {
		put(var, 0);
	}

	public void inc(int var) {
		int count = get(var);
		count++;
		put(var, count);
	}

	@Override
	public Integer get(Object o) {
		Integer i = super.get(o);
		if (i == null)
			return 0;
		return i;
	}
}