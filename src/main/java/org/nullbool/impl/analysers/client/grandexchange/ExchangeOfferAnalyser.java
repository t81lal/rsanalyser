package org.nullbool.impl.analysers.client.grandexchange;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 30 Jul 2015 14:44:43
 */
public class ExchangeOfferAnalyser extends ClassAnalyser {

	public ExchangeOfferAnalyser() {
		super("ExhangeOffer");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		return false;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return null;
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return null;
	}
}