package org.nullbool.impl.r77;

import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.impl.analysers.net.BufferAnalyser;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

/**
 * write40 disabled for revisions before 77.
 * 
 * @author Bibl (don't ban me pls)
 * @created 7 Jun 2015 10:30:38
 */
public class BufferAnalyser77 extends BufferAnalyser {

	@Override
	public String[] supportedMethods() {
		return new Builder<String>(super.supportedMethods()).add("write40&(J)V").asArray(new String[0]);
	}
	
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return super.registerMethodAnalysers().replace(new Filter<IMethodAnalyser>() {
			
			@Override
			public boolean accept(IMethodAnalyser t) {
				return t.getClass().equals(MethodAnalyser.class);
			}
		}, new MethodAnalyser77());
	}
	
	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:44:24
	 */
	public class MethodAnalyser77 extends MethodAnalyser {
		@Override
		protected void analyse(ArrayStoreVisitor asv, ControlFlowGraph graph, MethodNode m, List<MethodHook> list) {
			boolean b = false;
			if(m.desc.startsWith("(J") && m.desc.endsWith("V")) {
				List<Object> value = asv.found.get(ArrayStoreVisitor.VALUE);
				if(match(value, WRITE_LONG_SHIFTS, 2)) {
					// NOTE: ADDED IN R77
					list.add(asMethodHook(MethodType.CALLBACK, m, "write40"));
					b = true;
				}
			} 

			if(!b) {
				super.analyse(asv, graph, m, list);
			}
		}
	}
}