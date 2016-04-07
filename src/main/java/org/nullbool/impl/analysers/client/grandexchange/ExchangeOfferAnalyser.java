package org.nullbool.impl.analysers.client.grandexchange;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.api.util.StaticDescFilter;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 30 Jul 2015 14:44:43
 */
@SupportedHooks(
		fields = { },
		methods = { })
public class ExchangeOfferAnalyser extends ClassAnalyser {

	public ExchangeOfferAnalyser() {
		super("ExchangeOffer");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		int ints = getFieldCount(cn, new StaticDescFilter("I"));
		int bytes = getFieldCount(cn, new StaticDescFilter("B"));

		if(ints < 5 || bytes < 1)
			return false;
		for(MethodNode mn : cn.methods){
			if(mn.name.equals("<init>") && mn.desc.contains("L"+ findObfClassName("Buffer") + ";")){
				return true;
			}
		}
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