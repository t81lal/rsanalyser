package org.nullbool.impl.analysers.client;

import java.util.List;

import org.nullbool.api.analysis.AbstractClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@SupportedHooks(fields = {}, methods = {})
public class ExceptionReporterAnalyser extends AbstractClassAnalyser {

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
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return null;
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;// report method in client
	}
}