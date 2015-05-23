package org.nullbool.impl.analysers.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getId&I", "getStackSize&I", }, methods = {})
public class GroundItemAnalyser extends ClassAnalyser {

	public GroundItemAnalyser() throws AnalysisException {
		super("GroundItem");
	}

	@Override
	protected boolean matches(ClassNode c) {
		String[] pat = { "aload", "invokespecial", "return", "new", "dup" };
		String renderable = findObfClassName("Renderable");
		boolean rightSuperClass = c.superName.equals(renderable);
		boolean findMethod = findMethod(c, "<init>", pat);
		return rightSuperClass && findMethod;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new InfoHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class InfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String[] pat = { "invokevirtual", "areturn", "new", "dup" };
			MethodNode[] m = getMethodNodes(cn.methods.toArray());
			MethodNode method = identifyMethod(m, true, pat);
			AbstractInsnNode[] ins = followJump(method, 5);

			String h = findField(ins, false, true, 1, 'f', "getfield");
			list.add(asFieldHook(h, "getId", findMultiplier(h, false)));

			h = findField(ins, false, true, 2, 'f', "getfield");
			list.add(asFieldHook(h, "getStackSize", findMultiplier(h, false)));

			return list;
		}
	}
}