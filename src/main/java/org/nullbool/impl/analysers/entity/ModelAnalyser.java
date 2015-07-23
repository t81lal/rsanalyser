package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "vertexCount&I", "triangleCount&I", "verticesX&[I", "verticesY&[I", "verticesZ&[I",
		"indicesX&[I", "indicesY&[I", "indicesZ&[I", }, methods = {})
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
		public List<FieldHook> findFields(ClassNode cn) {
			String[] pattern = { "aload", "aload" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(pattern, ms)[0];
			AbstractInsnNode[] i = followJump(m, 15);

			String h = findField(i, false, true, 1, 'f', "getfield");
			list.add(asFieldHook(h, "vertexCount"));

			h = findField(i, false, true, 2, 'f', "getfield");
//			list.add(asFieldHook(h, "indicesCount"));

			h = findField(i, false, true, 3, 'f', "getfield");
			list.add(asFieldHook(h, "triangleCount"));

			h = findField(i, true, true, 1, 'f', "newarray 10");
			list.add(asFieldHook(h, "verticesX"));

			h = findField(i, true, true, 3, 'f', "newarray 10");
			list.add(asFieldHook(h, "verticesY"));

			h = findField(i, true, true, 5, 'f', "newarray 10");
			list.add(asFieldHook(h, "verticesZ"));

			h = findField(i, true, true, 4, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "indicesX"));

			h = findField(i, true, true, 6, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "indicesY"));

			h = findField(i, true, true, 8, 'f', "aload 2", "aload 3");
			list.add(asFieldHook(h, "indicesZ"));

			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		// TODO Auto-generated method stub
		return null;
	}
}