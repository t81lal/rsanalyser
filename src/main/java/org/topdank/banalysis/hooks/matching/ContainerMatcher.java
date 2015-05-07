package org.topdank.banalysis.hooks.matching;

/**
 * @author sc4re
 */
public abstract interface ContainerMatcher<T, K> {

	public abstract K match(T object);
}