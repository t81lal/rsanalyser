package org.nullbool.api;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private static final Map<Thread, AbstractAnalysisProvider> binded = new HashMap<Thread, AbstractAnalysisProvider>();

	public static void block() {
		final long startTime = System.currentTimeMillis();
		final Thread thread = Thread.currentThread();
		while(true) {
			if(binded.containsKey(thread))
				break;
			
			long now = System.currentTimeMillis();
			long d   = now - startTime;
			if(d >= 2500) {
				throw new RuntimeException("Timed out.");
			}
		}
	}
	
	public static void bind(AbstractAnalysisProvider provider) {
		Thread thread = Thread.currentThread();
		if(binded.containsKey(thread))
			throw new RuntimeException("A provider is already binded to this thread!");
		
		binded.put(thread, provider);
	}

	public static void unbind() {
		binded.remove(Thread.currentThread());
	}

	public static AbstractAnalysisProvider current() {
		return binded.get(Thread.currentThread());
	}
}