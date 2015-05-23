package org.nullbool.impl.analysers.client;

import java.awt.Canvas;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
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
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return null;
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	@Override
	public boolean matches(ClassNode cn) {
		return cn.superName.equals(Canvas.class.getCanonicalName().replace(".", "/"));
	}
}