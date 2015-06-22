/**
 * 
 */
package org.nullbool.api.output;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.zbot.pi.core.hooks.api.ClassHook;
import org.nullbool.zbot.pi.core.hooks.api.HookMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jun 2015 01:34:49
 */
public class NewOutputLogger {

	public static HookMap output() {
		// TODO: do
		AbstractAnalysisProvider provider = Context.current();
		List<ClassAnalyser> analysers = provider.getAnalysers();
		List<ClassHook> classes = new ArrayList<ClassHook>();
		analysers.forEach(a -> classes.add(a.getFoundHook()));

		StringBuilder sb = new StringBuilder();
		
		for(ClassAnalyser analyser : analysers) {
			ClassHook ch = analyser.getFoundHook();
			if(ch == null) {
				
			} else {
				
			}
		}
		
		return null;
	}
}