package org.nullbool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.Revision;
import org.nullbool.api.output.APIGenerator;
import org.nullbool.api.util.RSVersionHelper;
import org.nullbool.impl.AnalysisProviderImpl;
import org.nullbool.impl.AnalysisProviderRegistry;
import org.nullbool.impl.AnalysisProviderRegistry.ProviderCreator;
import org.nullbool.impl.AnalysisProviderRegistry.RegistryEntry;
import org.nullbool.impl.r77.AnalysisProvider77Impl;
import org.nullbool.impl.r79.AnalysisProvider79Impl;
import org.nullbool.impl.r82.AnalysisProvider82Impl;
import org.nullbool.impl.r90.AnalysisProvider90Impl;
import org.topdank.banalysis.filter.Filter;
import org.topdank.byteio.util.Debug;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public class Boot {

	private static int revision = 1001;

	public static void main(String[] args) throws Exception {
		System.out.printf("Remote rev: %d.%n", RSVersionHelper.getVersion(RSVersionHelper.getServerAddress(58), 77, 100));
		
		bootstrap();

		// Use runLatest for full logs
//		int count = 1;
//		for(int i=0; i < count; i++) {
//			Revision revision = rev(Boot.revision - i);
//			System.out.println("Running " + revision.getName());
//			try {
////				deob(AnalysisProviderRegistry.get(revision).create(revision));
////				runQuiet(AnalysisProviderRegistry.get(revision).create(revision));
////				fast_runQuiet(AnalysisProviderRegistry.get(revision).create(revision));
////				fast_runLatest(AnalysisProviderRegistry.get(revision).create(revision));
////				runLatest(AnalysisProviderRegistry.get(revision).create(revision));
//			} catch(Throwable t) {
//				t.printStackTrace();
//			}
//		}		

		Revision revision = rev(Boot.revision);
		runLatest(AnalysisProviderRegistry.get(revision).create(revision));

		//runLatest(71);
		//runLast10();
		//runTest(revision);
		//run(70, revision, 2);
		//deob(revision);
		
		System.exit(1);
	}

	private static void deob(AbstractAnalysisProvider provider) throws Exception {
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("nodump", false);
		flags.put("debug", false);
		flags.put("reorderfields", true);
		flags.put("multis", true);
		flags.put("logresults", true);
		flags.put("verify", false);
		flags.put("justdeob", true);
		flags.put("paramdeob", true);
		flags.put("aggressiveparams", false);

		Context.bind(provider);
		provider.run();
		//Context.unbind();
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
		flags.put("nodump", true);
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
						//flags.put("nodump", true);
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
						try {
							Context.block();
							provider.run();
						} catch(RuntimeException e2) {
							System.err.println("Timeout for r" + j);
							e2.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			executor.submit(thread);
		}

		executor.shutdown();
	}

	private static void runFlags(AbstractAnalysisProvider provider, Map<String, Boolean> flags) throws Exception {
		try {
			Context.bind(provider);
			provider.run();
		} finally {
			Context.unbind();
		}
	}

	private static void fast_runQuiet(AbstractAnalysisProvider provider) throws Exception {		
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("nodump", true);
		flags.put("debug", false);
		flags.put("reorderfields", true);
		flags.put("multis", false);
		flags.put("logresults", false);
		flags.put("verify", false);
		flags.put("superDebug", false);
		flags.put("basicout", false);
		flags.put("out", false);
		flags.put("paramdeob", true);
		// flags.put("nodump", true);
		runFlags(provider, flags);
	}
	
	
	private static void runQuiet(AbstractAnalysisProvider provider) throws Exception {		
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("debug", false);
		flags.put("reorderfields", true);
		flags.put("multis", false);
		flags.put("logresults", false);
		flags.put("verify", false);
		flags.put("superDebug", false);
		flags.put("basicout", false);
		flags.put("out", false);
		flags.put("paramdeob", true);
		// flags.put("nodump", true);
		runFlags(provider, flags);
	}
	
	private static void fast_runLatest(AbstractAnalysisProvider provider) throws Exception {
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("nodump", true);
		flags.put("debug", true);
		flags.put("reorderfields", true);
		flags.put("multis", true);
		flags.put("logresults", true);
		flags.put("verify", false);
		flags.put("paramdeob", true);
		// flags.put("generateheaders", true);
		runFlags(provider, flags);
	}

	private static void runLatest(AbstractAnalysisProvider provider) throws Exception {
		Map<String, Boolean> flags = provider.getFlags();
		flags.put("nodump", false);
		flags.put("debug", true);
		flags.put("reorderfields", true);
		flags.put("multis", true);
		flags.put("logresults", true);
		flags.put("verify", false);
		flags.put("paramdeob", true);
		// flags.put("generateheaders", true);
		runFlags(provider, flags);
	}

	private static void bootstrap() throws Exception {
		AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
			@Override
			public AbstractAnalysisProvider create(Revision rev) throws Exception {
				return new AnalysisProviderImpl(rev);
			}
		}).addFilter(new Filter<Revision>() {
			@Override
			public boolean accept(Revision t) {
				return true;
			}
		}));

		/* Adds it before the default implementation. */
		AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
			@Override
			public AbstractAnalysisProvider create(Revision rev) throws Exception {
				return new AnalysisProvider77Impl(rev);
			}
		}).addFilter(new Filter<Revision>() {
			@Override
			public boolean accept(Revision t) {
				if(t == null)
					return false;

				try {
					int val = Integer.parseInt(t.getName());
					return val >= 77;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}
		}));

		AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
			@Override
			public AbstractAnalysisProvider create(Revision rev) throws Exception {
				return new AnalysisProvider79Impl(rev);
			}
		}).addFilter(new Filter<Revision>() {
			@Override
			public boolean accept(Revision t) {
				if(t == null)
					return false;

				try {
					int val = Integer.parseInt(t.getName());
					return val >= 79;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}
		}));		
		
		AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
			@Override
			public AbstractAnalysisProvider create(Revision rev) throws Exception {
				return new AnalysisProvider82Impl(rev);
			}
		}).addFilter(new Filter<Revision>() {
			@Override
			public boolean accept(Revision t) {
				if(t == null)
					return false;

				try {
					int val = Integer.parseInt(t.getName());
					return val >= 82;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}
		}));
		
		AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
			@Override
			public AbstractAnalysisProvider create(Revision rev) throws Exception {
				return new AnalysisProvider90Impl(rev);
			}
		}).addFilter(new Filter<Revision>(){
			@Override
			public boolean accept(Revision t) {
				if(t == null)
					return false;

				try {
					int val = Integer.parseInt(t.getName());
					return val >= 90;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}
		}));
	}

	public static Revision rev(int revision) throws Exception {
		return new Revision(Integer.toString(revision), new File(Boot.class.getResource(
				"/jars/gamepack" + revision + ".jar").toURI()));
	}

	public static void main1(String[] args) throws IOException {
		for(File f : files(new File("C:/Users/Bibl/Desktop/Arios RSPS"))) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while((line = br.readLine()) != null) {
				if(line.contains("mgi.")) {
					System.out.println("in " + f.getAbsolutePath());
				}
			}
			br.close();
		}
	}

	private static List<File> files(File dir) {
		List<File> files = new ArrayList<File>();
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				files.addAll(files(f));
			} else {
				files.add(f);
			}
		}
		return files;
	}
} 