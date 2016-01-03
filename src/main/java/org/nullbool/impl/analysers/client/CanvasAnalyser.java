package org.nullbool.impl.analysers.client;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = {"component&Ljava/awt/Component;"}, methods = {})
public class CanvasAnalyser extends ClassAnalyser {

	public CanvasAnalyser() throws AnalysisException {
		super("Canvas");
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldsAnalyser());
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
		return null;
	}

	private class FieldsAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			String h = getFieldOfType(cn, "Ljava/awt/Component;", false);
			list.add(asFieldHook(h, "component"));

			return list;
		}
	}
}