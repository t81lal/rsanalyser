package org.nullbool.impl.analysers.world;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "markedRenderable1&Renderable", "markedRenderable2&Renderable", "strictX&I", "strictY&I", "plane&I", "uid&I",
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
		public List<FieldHook> find(ClassNode cn) {

			String h, n = cn.name;
			List<FieldHook> l = new ArrayList<FieldHook>();
			ClassNode c = getClassNodeByRefactoredName("Region");
			String[] p = { "invokespecial " + n + ".<init> ()V", "astore 11" };
			MethodNode[] ms = getMethodNodes(c.methods.toArray());
			MethodNode m = identifyMethod(ms, false, p);
			AbstractInsnNode[] ins = followJump(m, 100);

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "markedRenderable1"));

			h = findField(ins, false, true, 7, 'f', "putfield");
			l.add(asFieldHook(h, "markedRenderable2"));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "strictX"));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "strictY"));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "plane"));

			h = findField(ins, false, true, 1, 'f', "putfield");
			l.add(asFieldHook(h, "uid"));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "flags"));

			h = findField(ins, false, true, 8, 'f', "putfield");
			l.add(asFieldHook(h, "orientation1"));

			h = findField(ins, false, true, 9, 'f', "putfield");
			l.add(asFieldHook(h, "orientation2"));

			return l;
		}
	}
}