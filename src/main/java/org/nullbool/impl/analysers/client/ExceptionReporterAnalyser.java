package org.nullbool.impl.analysers.client;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

@Deprecated
@SupportedHooks(fields = {}, methods = {})
public class ExceptionReporterAnalyser extends ClassAnalyser {

	public ExceptionReporterAnalyser() {
		super("ExceptionReporter");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		for (MethodNode m : cn.methods) {
			if (m.desc.startsWith("(Ljava/lang/Throwable;Ljava/lang/String;") && m.desc.contains(")L")) {
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
		return null;// report method in client
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}