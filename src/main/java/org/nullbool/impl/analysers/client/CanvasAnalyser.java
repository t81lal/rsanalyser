package org.nullbool.impl.analysers.client;

import java.awt.Canvas;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = {}, methods = {})
public class CanvasAnalyser extends ClassAnalyser {

	public CanvasAnalyser() throws AnalysisException {
		super("Canvas");
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
	public boolean matches(ClassNode cn) {
		return cn.superName.equals(Canvas.class.getCanonicalName().replace(".", "/"));
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		// TODO Auto-generated method stub
		return null;
	}
}