package org.nullbool.impl.analysers.entity;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "name&Ljava/lang/String;", "playerLevel&I" , "playerSkull&I", "playerOverhead&I"}, methods = {})
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
		return new Builder<IFieldAnalyser>().addAll(new NameHook(), new LevelHook(), new EmblemHook());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NameHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String pat, hook, actorObj = findObfClassName("Player");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;\\w";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 400");
			pat = "getfield " + actorObj + ".\\w* \\w*\\/\\w*\\/String;";
			hook = findField(method, true, false, 1, 'f', pat);
			list.add(asFieldHook(hook, "name"));

			return list;
		}
	}

	public class LevelHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {

			String h, definitionObj = cn.name;
			String player = findObfClassName("Player");
			List<FieldHook> list = new ArrayList<FieldHook>();
			String regex = ";\\w*" + "L" + definitionObj + ";" + "\\w*;\\w";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 400");
			String[] pattern = { "getstatic \\w*.\\w* L" + player + ";", "getfield \\w*.\\w* I" };

			h = findField(method, true, true, 1, 'f', pattern);
			list.add(asFieldHook(h, "playerLevel"));

			return list;
		}
	}

	public class EmblemHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			String h, buffer = findObfClassName("Buffer"), player = findObfClassName("Player");

			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.stream().filter(methodNode -> methodNode.desc.replaceAll("[()]", ";").matches(";L" + buffer +";.{0,2};V") &&
                    !Modifier.isStatic(methodNode.access)).toArray());

			MethodNode method = startWithBc(new String[] { "aload", "iconst_0", "putfield" }, ms)[0];
			AbstractInsnNode[] i = followJump(method,120);

			h = findField(i, true, true, 1, 'f', "invokevirtual " + buffer + ".*","ldc .*","imul","putfield " + player + ".* I");
            list.add(asFieldHook(h, "playerSkull"));

            h = findField(i, true, true, 2, 'f', "invokevirtual " + buffer + ".*","ldc .*","imul","putfield " + player + ".* I");
            list.add(asFieldHook(h, "playerOverhead"));

			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}