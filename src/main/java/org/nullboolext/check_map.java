package org.nullboolext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Bibl (don't ban me pls)
 * @created 1 Jul 2015 19:00:52
 */
public class check_map {

	public static void check(Map m) {
		for(Entry<Object, Object> e : (Set<Entry<Object, Object>>) m.entrySet()) {
			System.out.printf("map.key = %d, val = %s.%n", e.getKey(), deep(e.getValue()));
		}
	}
	
	private static String deep(Object o) {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		
		for(Field f : o.getClass().getDeclaredFields()) {
			if(!Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				try {
					sb.append(f.getName()).append(" ").append(f.getType().getCanonicalName()).append(" = ").append(f.get(o));
					sb.append('\n');
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return sb.toString();
	}
}