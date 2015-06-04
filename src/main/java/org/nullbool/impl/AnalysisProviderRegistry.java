package org.nullbool.impl;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Revision;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 16:56:44
 */
public class AnalysisProviderRegistry {
	
	private static final List<RegistryEntry> entries = new ArrayList<RegistryEntry>();
	
	public static void register(RegistryEntry e) {
		/* Add to the start. */
		entries.add(0, e);
	}
	
	public static ProviderCreator get(Revision rev) {
		for(RegistryEntry e : entries) {
			if(e.accept(rev)) {
				return e.creator();
			}
		}
		
		throw new UnsupportedOperationException("Unsupported revision " + rev);
	}
	
	public static class RegistryEntry {

		private final ProviderCreator creator;
		private final List<Filter<Revision>> filters;
		
		public RegistryEntry(ProviderCreator creator) {
			this.creator = creator;
			filters = new ArrayList<Filter<Revision>>();
		}
		
		public RegistryEntry addFilter(Filter<Revision> f) {
			filters.add(f);
			return this;
		}
		
		public boolean accept(Revision rev) {
			for(Filter<Revision> f : filters) {
				if(f.accept(rev))
					return true;
			}
			return false;
		}
		
		public ProviderCreator creator() {
			return creator;
		}
	}
	
	public static abstract class ProviderCreator {
		public abstract AbstractAnalysisProvider create(Revision rev) throws Exception;
	}
}