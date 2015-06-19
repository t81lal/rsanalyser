package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.nullbool.zbot.pi.core.hooks.api.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getVertexCount&I", "getIndicesCount&I", "getTriangleCount&I", "getVerticesX&[I", "getVerticesY&[I", "getVerticesZ&[I",
		"getIndicesX&[I", "getIndicesY&[I", "getIndicesZ&[I", }, methods = {})
public class ModelAnalyser extends ClassAnalyser {

	public ModelAnalyser() throws AnalysisException {
		super("Model");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String superClassName = findObfClassName("Renderable");
		boolean rightFields = getFieldOfTypeCount(cn, "\\[I", false) >= 10;
		boolean rightSuperClass = cn.superName.equals(superClassName);
		return rightSuperClass && rightFields;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new ModelInfo());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class ModelInfo implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String[] pattern = { "aload", "aload" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(pattern, ms)[0];
			AbstractInsnNode[] i = followJump(m, 15);

			String h = findField(i, false, true, 1, 'f', "getfield");
			list.add(asFieldHook(h, "getVertexCount"));

			h = findField(i, false, true, 2, 'f', "getfield");
			list.add(asFieldHook(h, "getIndicesCount"));

			h = findField(i, false, true, 3, 'f', "getfield");
			list.add(asFieldHook(h, "getTriangleCount"));

			h = findField(i, true, true, 1, 'f', "newarray 10");
			list.add(asFieldHook(h, "getVerticesX"));

			h = findField(i, true, true, 3, 'f', "newarray 10");
			list.add(asFieldHook(h, "getVerticesY"));

			h = findField(i, true, true, 5, 'f', "newarray 10");
			list.add(asFieldHook(h, "getVerticesZ"));

			h = findField(i, true, true, 4, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "getIndicesX"));

			h = findField(i, true, true, 6, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "getIndicesY"));

			h = findField(i, true, true, 8, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "getIndicesZ"));

			return list;
		}
	}
}