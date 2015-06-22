/**
 * 
 */
package org.nullbool.pi.core.hook.api;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jun 2015 01:04:48
 */
public class Util {

	public static String mapToString(Map<?, ?> map, String prefix, String suffix) {
		StringBuilder sb = new StringBuilder();
		for(Entry<?, ?> e : map.entrySet()) {
			sb.append(prefix).append("\"").append(e.getKey()).append("\"=\"").append(e.getValue()).append("\"").append(suffix);
		}
		return sb.toString();
	}
}