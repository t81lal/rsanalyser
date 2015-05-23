package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getNpcDefinition&NPCDefinition", }, methods = {})
public class NPCAnalyser extends ClassAnalyser {

	public NPCAnalyser() throws AnalysisException {
		super("NPC");
	}

	@Override
	protected boolean matches(ClassNode c) {
		String[] pattern = Context.current().getPattern("NPC");
		String superClassName = findObfClassName("Actor");
		boolean rightSuperClass = c.superName.equals(superClassName);
		boolean goodPattern = findMethod(c, "init", pattern);

		return goodPattern && rightSuperClass;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new NpcDefHook());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NpcDefHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String obfName = findObfClassName("NPCDefinition");
			String hook = identify(cn, "L" + obfName + ";", 'f');
			list.add(asFieldHook(hook, "getNpcDefinition"));
			return list;
		}
	}
}