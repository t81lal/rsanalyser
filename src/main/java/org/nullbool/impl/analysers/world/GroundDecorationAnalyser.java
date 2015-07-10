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
@SupportedHooks(fields = { "bottomRenderable&Renderable", "middleRenderable&Renderable", "topRenderable&Renderable", "regionX&I", "regionY&I",
		"plane&I", "uid&I", }, methods = {})
public class GroundDecorationAnalyser extends ClassAnalyser {

	public GroundDecorationAnalyser() throws AnalysisException {
		super("GroundDecoration");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String cnType = "L" + cn.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("Tile");
		String type = "L" + findObfClassName("Renderable") + ";";

		boolean inRegion = containFieldOfType(classNode, cnType);
		boolean rightIntCount = getFieldOfTypeCount(cn, "I", false) == 5;
		boolean rightObjectCount = getFieldOfTypeCount(cn, type, false) == 3;
		return rightObjectCount && inRegion && rightIntCount;
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
			String[] p = { "invokespecial " + n + ".<init> ()V" };
			MethodNode[] ms = getMethodNodes(c.methods.toArray());
			MethodNode m = identifyMethod(ms, false, p);
			AbstractInsnNode[] ins = followJump(m, 100);

			h = findField(ins, false, true, 1, 'f', "putfield");
			l.add(asFieldHook(h, "bottomRenderable"));
			// l.add(new Hook("bottomRenderable()", h));

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "middleRenderable"));
			// l.add(new Hook("middleRenderable()", h));

			h = findField(ins, false, true, 7, 'f', "putfield");
			l.add(asFieldHook(h, "topRenderable"));
			// l.add(new Hook("topRenderable()", h));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "regionX"));
			// l.add(new Hook("regionX()", h));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "regionY"));
			// l.add(new Hook("regionY()", h));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "plane"));
			// l.add(new Hook("plane()", h));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "uid"));
			// l.add(new Hook("uid()", h));
			// return l.toArray(new Hook[l.size()]);
			return l;
		}
	}
}