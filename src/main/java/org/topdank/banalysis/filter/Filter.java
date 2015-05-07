package org.topdank.banalysis.filter;

public abstract interface Filter<T> {
	
	public static final Filter<Object> ACCEPT_ALL = new Filter<Object>() {
		@Override
		public boolean accept(Object t) {
			return true;
		}
	};
	
	public abstract boolean accept(T t);
}