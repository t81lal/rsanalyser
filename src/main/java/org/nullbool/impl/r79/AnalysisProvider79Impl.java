package org.nullbool.impl.r79;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.r77.AnalysisProvider77Impl;
import org.nullbool.impl.r77.BufferAnalyser77;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 16:19:04
 */
public class AnalysisProvider79Impl extends AnalysisProvider77Impl {

	public AnalysisProvider79Impl(Revision revision) throws IOException {
		super(revision);
	}
	
	@Override
	public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers().replace(new Filter<ClassAnalyser>() {
			@Override
			public boolean accept(ClassAnalyser t) {
				return t.getClass().equals(ClientAnalyser.class);
			}
		}, new ClientAnalyser79()).replace(new Filter<ClassAnalyser>(){
			@Override
			public boolean accept(ClassAnalyser t) {
				return t.getClass().equals(BufferAnalyser77.class);
			}
		}, new BufferAnalyser79()).sort();
	}
}