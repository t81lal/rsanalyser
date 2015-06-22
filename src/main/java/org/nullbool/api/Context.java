package org.nullbool.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A static registry-type structure to allow easy binding and unbinding
 * as well as current context retrieval. <br>
 * 
 * An internal Map populated by {@link Thread} objects linked with 
 * {@link AbstractAnalysisProvider}s is held and is pollable using the
 * {@link #current()} method, which MUST be called from the same Thread
 * that the provider was registered from. <br>
 * 
 * Data can be binded using the {@link #bind(AbstractAnalysisProvider)} method
 * and can then be unbinded with {@link #unbind()}. <br>
 * 
 * A utility method {@link #block()} is defined to allow a Thread to wait or 'block'
 * until either the latest provider has been binded.
 * 
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015 21:13:26 (actually before this)
 */
public class Context {

	private static final Map<Thread, AbstractAnalysisProvider> binded = new HashMap<Thread, AbstractAnalysisProvider>();

	/**
	 * Waits on the current Thread until either the provider that
	 * was submitted with the bind method has been binded or
	 * until the method times out (25,000 ms).
	 */
	public static void block() {
		synchronized (binded) {
			final long startTime = System.currentTimeMillis();
			final Thread thread = Thread.currentThread();
			while(true) {
				if(binded.containsKey(thread))
					break;
				
				long now = System.currentTimeMillis();
				long d   = now - startTime;
				if(d >= 25000) {
					throw new RuntimeException("Timed out.");
				}
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Maps the current Thread with the provider or throws a RuntimeException
	 * if the current thread is already binded.
	 * @param provider
	 */
	public static void bind(AbstractAnalysisProvider provider) {
		synchronized (binded) {
			Thread thread = Thread.currentThread();
			if(binded.containsKey(thread))
				throw new RuntimeException("A provider is already binded to this thread!");
			
			binded.put(thread, provider);	
		}
	}

	/**
	 * Removes the current Thread (and thus provider) from the map.
	 */
	public static void unbind() {
		synchronized (binded) {
			binded.remove(Thread.currentThread());
		}
	}

	/**
	 * Gets the provider that was binded on the current Thread.
	 * @return An AbstractAnalysisProvider or null.
	 */
	public static AbstractAnalysisProvider current() {
		synchronized (binded) {
			return binded.get(Thread.currentThread());
		}
	}
}