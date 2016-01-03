package org.nullbool.impl.analysers.world;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MalikDz
 */
@SupportedHooks(
		fields = { "markerRenderable1&Renderable", "markerRenderable2&Renderable", "localX&I", "localY&I", "plane&I", "hash&I",
		"flags&I", "orientation1&I", "orientation2&I", }, methods = {})
public class WallObjectAnalyser extends ClassAnalyser {

	public WallObjectAnalyser() throws AnalysisException {
		super("WallObject");
	}

	@Override
	public boolean matches(ClassNode c) {
		String cnType = "L" + c.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("Tile");
		String type = "L" + findObfClassName("Renderable") + ";";

		boolean fieldPresent = containFieldOfType(c, type);
		boolean rightSuperClass = c.superName.contains("Object");
		boolean fieldInRegion = containFieldOfType(classNode, cnType);
		boolean rightCount = getFieldOfTypeCount(c, "I", false) == 7;
		return fieldPresent && rightSuperClass && fieldInRegion && rightCount;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new WallObjectInfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class WallObjectInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {

			String h, n = cn.name;
			List<FieldHook> l = new ArrayList<FieldHook>();
			ClassNode c = getClassNodeByRefactoredName("Region");
			String[] p = { "invokespecial " + n + ".<init> ()V", "astore 11" };
			MethodNode[] ms = getMethodNodes(c.methods.toArray());
			MethodNode m = identifyMethod(ms, false, p);
			AbstractInsnNode[] ins = followJump(m, 100);

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "markerRenderable1"));

			h = findField(ins, false, true, 7, 'f', "putfield");
			l.add(asFieldHook(h, "markerRenderable2"));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "localX"));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "localY"));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "plane"));

			h = findField(ins, false, true, 1, 'f', "putfield");
			l.add(asFieldHook(h, "hash"));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "flags"));

			h = findField(ins, false, true, 8, 'f', "putfield");
			l.add(asFieldHook(h, "orientation1"));

			h = findField(ins, false, true, 9, 'f', "putfield");
			l.add(asFieldHook(h, "orientation2"));

			return l;
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