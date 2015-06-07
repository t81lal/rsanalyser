package org.nullbool.impl.r77;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.AnalysisProviderImpl;
import org.nullbool.impl.analysers.net.BufferAnalyser;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 7 Jun 2015 10:30:20
 */
public class AnalysisProvider77Impl extends AnalysisProviderImpl {

	public AnalysisProvider77Impl(Revision revision) throws IOException {
		super(revision);
	}
	
	@Override
	public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers().replace(new Filter<ClassAnalyser>(){
			@Override
			public boolean accept(ClassAnalyser t) {
				return t.getClass().equals(BufferAnalyser.class);
			}
			
		}, new BufferAnalyser77());
	}
}