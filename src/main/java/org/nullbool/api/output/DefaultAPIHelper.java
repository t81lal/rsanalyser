package org.nullbool.api.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAPIHelper {

	private final String base;
	private final Map<String, String> canons;
	private final Map<String, List<String>> supers;
	
	public DefaultAPIHelper(String base) {
		this.base = base;
		canons = new HashMap<String, String>();
		supers = new HashMap<String, List<String>>();
	}
	
	public String accessorBase() {
		return base;
	}
	
	public void mapSuperInterfaces(String klass, String[] superInterfaces, boolean overwrite) {
		List<String> col = null;
		if(supers.containsKey(klass)) {
			col = supers.get(klass);
			if(overwrite) {
				col.clear();
			}
		} else {
			col = new ArrayList<String>();
			supers.put(klass, col);
		}
		
		for(String si : superInterfaces) {
			col.add(si);
		}
	}
	
	public String[] superInterfaces(String klass) {
		if(!supers.containsKey(klass)) {
			return new String[0];
		} else {
			List<String> col = supers.get(klass);
			return col.toArray(new String[0]);
		}
	}
	
	public String canonicalName(String klass) {
		return canons.get(klass);
	}
	
	public void remapCanonicalname(String klass, String newName) {
		canons.put(klass, newName);
	}

	public Collection<String> simpleNames() {
		return canons.keySet();
	}
}