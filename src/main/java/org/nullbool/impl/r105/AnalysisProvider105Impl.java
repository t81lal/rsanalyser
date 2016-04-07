package org.nullbool.impl.r105;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.analysers.client.grandexchange.ExchangeOfferAnalyser;
import org.nullbool.impl.r104.AnalysisProvider104Impl;

import java.io.IOException;

/**
 * Created by Xerion on 1-2-2016.
 */
public class AnalysisProvider105Impl extends AnalysisProvider104Impl {

    public AnalysisProvider105Impl(Revision revision) throws IOException {
        super(revision);
    }

    @Override
    public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
        return super.registerAnalysers()
                .replace(t -> ClientAnalyser.class.isAssignableFrom(t.getClass()), new ClientAnalyser105())
                        .replace(t -> ExchangeOfferAnalyser.class.isAssignableFrom(t.getClass()), new ExchangeOfferAnalyser105());
    }
}
