package org.nullbool.impl.analysers.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
@SupportedHooks(fields = { "getBottomRenderable&Renderable", "getMiddleRenderable&Renderable", "getTopRenderable&Renderable", "getRegionX&I", "getRegionY&I",
		"getPlane&I", "getUID&I", }, methods = {})
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
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new WallObjectInfoHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
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
			l.add(asFieldHook(h, "getBottomRenderable"));
			// l.add(new Hook("getBottomRenderable()", h));

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "getMiddleRenderable"));
			// l.add(new Hook("getMiddleRenderable()", h));

			h = findField(ins, false, true, 7, 'f', "putfield");
			l.add(asFieldHook(h, "getTopRenderable"));
			// l.add(new Hook("getTopRenderable()", h));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "getRegionX", findMultiplier(h, false)));
			// l.add(new Hook("getRegionX()", h, findMultiplier(h, false)));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "getRegionY", findMultiplier(h, false)));
			// l.add(new Hook("getRegionY()", h, findMultiplier(h, false)));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "getPlane", findMultiplier(h, false)));
			// l.add(new Hook("getPlane()", h, findMultiplier(h, false)));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "getUID", findMultiplier(h, false)));
			// l.add(new Hook("getUID()", h, findMultiplier(h, false)));
			// return l.toArray(new Hook[l.size()]);
			return l;
		}
	}
}