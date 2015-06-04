package org.nullbool.impl;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 16:19:04
 */
public class AnalysisProvider79 extends AnalysisProviderImpl implements Filter<ClassAnalyser> {

	public AnalysisProvider79(Revision revision) throws IOException {
		super(revision);
	}
	
	@Override
	protected Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers().addFilter(this).sort();
	}
	
	@Override
	public boolean accept(ClassAnalyser t) {
		return false;
	}
}