package org.nullbool.impl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.Revision;
import org.nullbool.api.output.APIGenerator;
import org.topdank.byteio.util.Debug;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public class Boot {

	private static int revision = 78;

	public static void main(String[] args) throws Exception {
//		runLatest(71);
//		run(70, revision, 4);
//		runLast10();
		runTest(revision);
	}
	
	private static void runLast10() {
		for(int i=70; i <= revision; i++) {			
			System.out.println("=====================START===================");
			try {
				runTest(i);
			} catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("=====================END=====================\n");
		}
	}

	private static void runTest(int j) throws Exception {
		Debug.debugging  = true;
		APIGenerator.log = false;
		
		AbstractAnalysisProvider provider = new AnalysisProviderImpl(new Revision(Integer.toString(j), new File(Boot.class.getResource("/jars/gamepack" + j + ".jar").toURI())));
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("debug", false);
		flags.put("reorderfields", true);
		flags.put("multis", false);
		flags.put("logresults", false);
		flags.put("verify", false);
		flags.put("superDebug", false);
		flags.put("basicout", false);
		flags.put("out", false);
		//flags.put("justdeob", true);

		Context.bind(provider);
		Context.block();
		provider.run();
	}
	
	private static void run(int min, int max, int threads) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		Debug.debugging  = true;
		APIGenerator.log = false;
		
		for(int i=min; i <= max; i++) {
			final int j = i;
			if(j == 76)
				continue;
			
			Thread thread = new Thread(){
				@Override
				public void run() {
					try {		
						AbstractAnalysisProvider provider = new AnalysisProviderImpl(new Revision(Integer.toString(j), new File(Boot.class.getResource(
								"/jars/gamepack" + j + ".jar").toURI())));
						Map<String, Boolean> flags = provider.getFlags();
						flags.put("debug", false);
						flags.put("reorderfields", true);
						flags.put("multis", false);
						flags.put("logresults", false);
						flags.put("verify", false);
						flags.put("superDebug", false);
						flags.put("basicout", false);
						flags.put("out", false);
						//flags.put("justdeob", true);

						Context.bind(provider);
						Context.block();
						provider.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			executor.submit(thread);
		}
		
		executor.shutdown();
	}
	
	private static void runLatest(int revision) throws Exception {
		AbstractAnalysisProvider provider = new AnalysisProviderImpl(new Revision(Integer.toString(revision), new File(Boot.class.getResource(
				"/jars/gamepack" + revision + ".jar").toURI())));
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("debug", false);
		flags.put("reorderfields", true);
		flags.put("multis", true);
		flags.put("logresults", true);
		flags.put("verify", false);
//		flags.put("justdeob", true);
 
		Context.bind(provider);
		provider.run();
//		Context.unbind();
	}
} 