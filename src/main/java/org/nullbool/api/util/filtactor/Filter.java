package org.nullbool.api.util.filtactor;

public abstract interface Filter<T> {
	public abstract T accept(T t);
}