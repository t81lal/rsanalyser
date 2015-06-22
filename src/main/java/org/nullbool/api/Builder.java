package org.nullbool.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.topdank.banalysis.filter.Filter;

/**
 * Abstract List appender utility that supports filtering of elements.
 * 
 * @see List
 * @see ArrayList
 * @see Iterable
 * @see Filter
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 15:20:03
 */
public class Builder<T> implements Iterable<T> {

	private final List<T> sequenced;
	private final Set<Filter<T>> filters;

	public Builder() {
		sequenced = new ArrayList<T>();
		filters = new HashSet<Filter<T>>();
	}
	
	public Builder(T[] ts) {
		this();
		for(T t : ts) {
			add(t);
		}
	}

	public int size() {
		return sequenced.size();
	}

	/**
	 * Checks to see if the given element is accepted by the internal filter set.
	 * 
	 * @param t Element to add.
	 * @param runall Whether to check all of the filters before returning.
	 * @return Whether the item passes.
	 */
	public boolean allow(T t, boolean runall) {
		if(runall) {
			boolean res = true;
			for(Filter<T> f : filters) {
				res &= f.accept(t);
			}
			return res;
		} else {
			for(Filter<T> f : filters) {
				if(!f.accept(t))
					return false;
			}
			return true;
		}
	}

	/**
	 * Adds the item to the underlying list.
	 * 
	 * @param t The item to add.
	 * @param check Whether to check the filters before adding.
	 * @return this.
	 */
	public Builder<T> add(T t, boolean check) {
		if(check) {
			if(allow(t, false)) {}
			//return false;
		}

		//return 
		sequenced.add(t);

		return this;
	}

	/**
	 * Adds the item to the underlying list.
	 * 
	 * @param t The item to add.
	 * @return this.
	 */
	public Builder<T> add(T t) {
		add(t, true);

		return this;
	}
	
	public Builder<T> addAll(@SuppressWarnings("unchecked") T... ts) {
		return addAll(ts, false);
	}

	/**
	 * Attempts to add all of the items into the underlying list.
	 * 
	 * @param ts Array of items to add.
	 * @param checkAll Whether to check the filters before adding.
	 * @return this.
	 */
	public Builder<T> addAll(T[] ts, boolean checkAll) {
		if(checkAll) {
			//boolean res = true;
			for(T t : ts) {
				//res &= add(t);
				add(t);
			}
			//return res;
		} else {
			for(T t : ts) {
				//if(!add(t))
				//return false;
				add(t);
			}
			//return true;
		}

		return this;
	}
	
	public Builder<T> replace(Filter<T> filter, T t) {
		ListIterator<T> it = sequenced.listIterator();
		while(it.hasNext()) {
			T next = it.next();
			if(filter.accept(next)) {
				it.set(t);
			}
		}
		
		return this;
	}
	
	public Builder<T> remove(Filter<T> filter) {
		ListIterator<T> it = sequenced.listIterator();
		while(it.hasNext()) {
			T next = it.next();
			if(filter.accept(next)) {
				it.remove();
			}
		}
		
		return this;
	}

	/**
	 * Removes entries in the internal list that don't pass through the filters.
	 * <br>
	 * See {@link #allow(Object, boolean)}
	 * @return this.
	 */
	public Builder<T> sort() {
		ListIterator<T> it = sequenced.listIterator();
		while(it.hasNext()) {
			T next = it.next();
			if(!allow(next, false))
				it.remove();
		}

		return this;
	}

	/**
	 * Adds a filter.
	 * 
	 * @param filter The filter to add.
	 * @return this.
	 */
	public Builder<T> addFilter(Filter<T> filter) {
		filters.add(filter);
		return this;
	}

	/**
	 * @return A copy of the underlying array.
	 */
	public List<T> asList() {
		return Collections.unmodifiableList(sequenced);
	}
	
	public T[] asArray(T[] t) {
		return sequenced.toArray(t);
	}

	@Override
	public Iterator<T> iterator() {
		return sequenced.listIterator();
	}
}