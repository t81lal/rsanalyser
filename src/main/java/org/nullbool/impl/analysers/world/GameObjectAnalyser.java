package org.nullbool.impl.analysers.world;

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
@SupportedHooks(fields = { "hash&I", "plane&I", "strictX&I", "strictY&I", "localX&I", "localY&I", "width&I", "height&I",
		"orientation&I", "flags&I", "markedRenderable&Renderable", }, methods = {})
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
		public List<FieldHook> findFields(ClassNode cn) {
			String[] pattern = { "iload", "istore" };
			List<FieldHook> l = new ArrayList<FieldHook>();
			ClassNode c = getClassNodeByRefactoredName("Region");
			MethodNode[] ms = getMethodNodes(c.methods.toArray());

			MethodNode m = startWithBc(pattern, ms)[0];
			AbstractInsnNode[] i = followJump(m, 300);

			String h = findField(i, true, true, 1, 'f', "iload 1", "ldc .*");
			l.add(asFieldHook(h, "plane"));

			h = findField(i, true, true, 1, 'f', "iload 6", "ldc .*");
			l.add(asFieldHook(h, "strictX"));

			h = findField(i, true, true, 1, 'f', "iload 7", "ldc .*");
			l.add(asFieldHook(h, "strictY"));

			h = findField(i, true, true, 1, 'f', "iload 2", "ldc .*");
			l.add(asFieldHook(h, "localX"));

			h = findField(i, true, true, 1, 'f', "iload 3", "ldc .*");
			l.add(asFieldHook(h, "localY"));

			h = findField(i, true, true, 1, 'f', "iload 4", "iadd", "iconst_1");
			l.add(asFieldHook(h, "width"));

			h = findField(i, true, true, 1, 'f', "iload 5", "iadd", "iconst_1");
			l.add(asFieldHook(h, "height"));

			h = findField(i, true, true, 1, 'f', "iload 10", "ldc .*");
			l.add(asFieldHook(h, "orientation"));

			h = findField(i, true, true, 1, 'f', "iload 13", "ldc .*");
			l.add(asFieldHook(h, "flags"));

			h = findField(i, true, true, 1, 'f', "aload 14", "aload 9");
			l.add(asFieldHook(h, "markedRenderable"));

			return l;
		}
	}

	public class HashHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			MethodNode[] m = getMethodNodes(cn.methods.toArray());
			MethodNode method = searchMethod(m, "<init>");
			AbstractInsnNode[] i = followJump(method, 100);
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "hash"));
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