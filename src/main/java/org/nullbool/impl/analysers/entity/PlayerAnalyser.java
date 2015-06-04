package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getName&Ljava/lang/String;", "getPlayerLevel&I" }, methods = {})
public class PlayerAnalyser extends ClassAnalyser {

	public PlayerAnalyser() throws AnalysisException {
		super("Player");

	}

	@Override
	protected boolean matches(ClassNode c) {
		String npcClassName = findObfClassName("NPC");
		String superClassName = findObfClassName("Actor");
		boolean rightSuperClass = c.superName.equals(superClassName);
		return !c.name.equals(npcClassName) && rightSuperClass;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new NameHook(), new LevelHook());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NameHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String pat, hook, actorObj = findObfClassName("Player");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;\\w";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 400");

			pat = "getfield " + actorObj + ".\\w* \\w*\\/\\w*\\/String;";
			hook = findField(method, true, false, 1, 'f', pat);
			list.add(asFieldHook(hook, "getName"));

			return list;
		}
	}

	public class LevelHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {

			String h, definitionObj = cn.name;
			String player = findObfClassName("Player");
			List<FieldHook> list = new ArrayList<FieldHook>();
			String regex = ";\\w*" + "L" + definitionObj + ";" + "\\w*;\\w";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 400");

			String[] pattern = { "getstatic \\w*.\\w* L" + player + ";", "getfield \\w*.\\w* I" };

			h = findField(method, true, true, 1, 'f', pattern);
			list.add(asFieldHook(h, "getPlayerLevel", findMultiplier(h, false)));

			return list;
		}
	}
}