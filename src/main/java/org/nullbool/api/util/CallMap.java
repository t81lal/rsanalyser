package org.nullbool.api.util;

import java.util.HashMap;

public class CallMap extends HashMap<Integer, Long> {

	private static final long serialVersionUID = 8408691314253812882L;

	public void call(int key) {
		long now = System.currentTimeMillis();
		put(key, now);
	}

	public boolean before(int key1, int key2) {
		long n1 = getOrDefault(key1, 0L);
		long n2 = getOrDefault(key2, 0L);
		if(n1 == 0 || n2 == 0)
			return false;
		return n1 > n2;
	}
}