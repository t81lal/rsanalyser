package org.nullbool.impl.analysers.client;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bibl (don't ban me pls)
 * @created 23 Jul 2015 17:59:07
 */

@SupportedHooks(fields = {"methodByteArray&[[[B", "fields&[Ljava/lang/reflect/Field;", "methods&[Ljava/lang/reflect/Method;"},
		methods = {})
public class VerificationDataAnalyser extends ClassAnalyser {

	public VerificationDataAnalyser() {
		super("VerificationData");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		int barrs = getFieldCount(cn, new StaticDescFilter("[[[B"));
		if(barrs < 1)
			return false;
		
		int iarrs = getFieldCount(cn, new StaticDescFilter("[I"));
		if(iarrs < 3)
			return false;
		
		int is = getFieldCount(cn, new StaticDescFilter("I"));
		if(is < 2)
			return false;
		
		int fs = getFieldCount(cn, new StaticDescFilter("[Ljava/lang/reflect/Field;"));
		if(fs < 1)
			return false;
		
		int ms = getFieldCount(cn, new StaticDescFilter("[Ljava/lang/reflect/Method;"));
		if(ms < 1)
			return false;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return null;
	}

	private class FieldsAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h;

			h = getFieldOfType(cn, "[[[B", false);
			list.add(asFieldHook(h, "methodByteArray"));

			h = getFieldOfType(cn, "[Ljava/lang/reflect/Field;", false);
			list.add(asFieldHook(h, "fields"));

			h = getFieldOfType(cn, "[Ljava/lang/reflect/Method;", false);
			list.add(asFieldHook(h, "methods"));

			return list;
		}
	}
}