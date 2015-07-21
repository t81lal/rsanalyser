package org.nullbool.impl.r82;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.r79.AnalysisProvider79Impl;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 17 Jul 2015 20:38:48
 */
public class AnalysisProvider82Impl extends AnalysisProvider79Impl {

	/**
	 * @param revision
	 * @throws IOException
	 */
	public AnalysisProvider82Impl(Revision revision) throws IOException {
		super(revision);
	}

	@Override
	public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers().replaceAfter(new Filter<ClassAnalyser>() {
			@Override
			public boolean accept(ClassAnalyser t) {
				return ClientAnalyser.class.isAssignableFrom(t.getClass());
			}
		}, new ClientAnalyser82());
	}
}