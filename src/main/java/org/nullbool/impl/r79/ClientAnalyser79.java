package org.nullbool.impl.r79;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 17:51:10
 */
public class ClientAnalyser79 extends ClientAnalyser {
	
	public ClientAnalyser79() throws AnalysisException {
		super();
	}
	
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return super.registerFieldAnalysers();
	}
}