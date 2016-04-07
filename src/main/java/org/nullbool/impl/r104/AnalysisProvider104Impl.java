package org.nullbool.impl.r104;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.entity.ActorAnalyser;
import org.nullbool.impl.r90.AnalysisProvider90Impl;

import java.io.IOException;

/**
 * Created by Xerion on 10-1-2016.
 */
public class AnalysisProvider104Impl extends AnalysisProvider90Impl{

    public AnalysisProvider104Impl(Revision revision) throws IOException {
        super(revision);
    }

    @Override
    public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
        return super.registerAnalysers()
                .replace(t -> ActorAnalyser.class.isAssignableFrom(t.getClass()), new ActorAnalyser104());
    }
}
