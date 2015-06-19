package org.nullbool.impl.analysers.world;

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
@SupportedHooks(fields = { "getHash&I", "getPlane&I", "getStrictX&I", "getStrictY&I", "getLocalX&I", "getLocalY&I", "getWidth&I", "getHeight&I",
		"getOrientation&I", "getFlags&I", "getMarkedRenderable&Renderable", }, methods = {})
public class GameObjectAnalyser extends ClassAnalyser {

	public GameObjectAnalyser() throws AnalysisException {
		super("GameObject");
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new HashHook(), new ObjectInfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	@Override
	public boolean matches(ClassNode c) {
		String cnType = "[L" + c.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("Region");
		String type = "L" + findObfClassName("Renderable") + ";";

		boolean fieldIsInClassNode = containFieldOfType(c, type);
		boolean rightSuperClass = c.superName.contains("Object");
		boolean fieldIsInRegionClass = containFieldOfType(classNode, cnType);
		return fieldIsInClassNode && rightSuperClass && fieldIsInRegionClass;
	}

	public class ObjectInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String[] pattern = { "iload", "istore" };
			List<FieldHook> l = new ArrayList<FieldHook>();
			ClassNode c = getClassNodeByRefactoredName("Region");
			MethodNode[] ms = getMethodNodes(c.methods.toArray());

			MethodNode m = startWithBc(pattern, ms)[0];
			AbstractInsnNode[] i = followJump(m, 300);

			String h = findField(i, true, true, 1, 'f', "iload 1", "ldc .*");
			l.add(asFieldHook(h, "getPlane", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 6", "ldc .*");
			l.add(asFieldHook(h, "getStrictX", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 7", "ldc .*");
			l.add(asFieldHook(h, "getStrictY", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 2", "ldc .*");
			l.add(asFieldHook(h, "getLocalX", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 3", "ldc .*");
			l.add(asFieldHook(h, "getLocalY", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 4", "iadd", "iconst_1");
			l.add(asFieldHook(h, "getWidth", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 5", "iadd", "iconst_1");
			l.add(asFieldHook(h, "getHeight", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 10", "ldc .*");
			l.add(asFieldHook(h, "getOrientation", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "iload 13", "ldc .*");
			l.add(asFieldHook(h, "getFlags", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', "aload 14", "aload 9");
			l.add(asFieldHook(h, "getMarkedRenderable", findMultiplier(h, false)));

			return l;
		}
	}

	public class HashHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			MethodNode[] m = getMethodNodes(cn.methods.toArray());
			MethodNode method = searchMethod(m, "<init>");
			AbstractInsnNode[] i = followJump(method, 100);
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "getHash", findMultiplier(h, false)));
			return list;
		}
	}
}