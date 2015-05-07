package org.nullbool.api;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private static final Map<Thread, AbstractAnalysisProvider> registered = new HashMap<Thread, AbstractAnalysisProvider>();

	public static void register(AbstractAnalysisProvider updater) {
		registered.put(Thread.currentThread(), updater);
	}

	public static void unregister() {
		registered.remove(Thread.currentThread());
	}

	public static AbstractAnalysisProvider current() {
		return registered.get(Thread.currentThread());
	}
}