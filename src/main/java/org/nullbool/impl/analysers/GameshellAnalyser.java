package org.nullbool.impl.analysers;

import java.util.List;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;

@SupportedHooks(fields = {}, methods = {})
public class GameshellAnalyser extends ClassAnalyser {
 
	private final ClassNode clientNode;
	
	public GameshellAnalyser() {
		super("Gameshell");
		clientNode = Context.current().getClassNodes().get("client");
	}
	
	@Override
	protected boolean matches(ClassNode cn) {
		return cn.name.equals(clientNode.superName);
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return null;
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
}
