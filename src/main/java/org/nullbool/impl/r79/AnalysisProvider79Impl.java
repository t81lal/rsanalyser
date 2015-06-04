package org.nullbool.impl.r79;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.AnalysisProviderImpl;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 16:19:04
 */
public class AnalysisProvider79Impl extends AnalysisProviderImpl {

	public AnalysisProvider79Impl(Revision revision) throws IOException {
		super(revision);
	}
	
	@Override
	protected Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers().replace(new Filter<ClassAnalyser>() {
			@Override
			public boolean accept(ClassAnalyser t) {
				return t.getClass().equals(ClientAnalyser.class);
			}
		}, new ClientAnalyser79()).sort();
	}
}