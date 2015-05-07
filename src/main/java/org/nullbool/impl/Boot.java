package org.nullbool.impl;

import java.io.File;
import java.util.Map;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.Revision;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public class Boot {

	private static int revision = 78;

	public static void main(String[] args) throws Exception {
		AbstractAnalysisProvider provider = new AnalysisProviderImpl(new Revision(Integer.toString(revision), new File(Boot.class.getResource(
				"/jars/gamepack" + revision + ".jar").toURI())));
		Map<String, Boolean> flags = provider.getFlagsMap();
		flags.put("debug", false);
		flags.put("multis", true);
		flags.put("logresults", true);
		flags.put("verify", false);

		Context.register(provider);
		provider.run();
		Context.unregister();
	}
}