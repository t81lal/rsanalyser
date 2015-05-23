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
@SupportedHooks(fields = { "getMarkedRenderable&Renderable", "getStrictX&I", "getStrictY&I", "getPlane&I", "getUID&I", "getFlags&I", }, methods = {})
public class GroundObjectAnalyser extends ClassAnalyser {

	public GroundObjectAnalyser() throws AnalysisException {
		super("GroundObject");
	}

	@Override
	protected boolean matches(ClassNode c) {
		String cnType = "L" + c.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("Tile");
		String type = "L" + findObfClassName("Renderable") + ";";
		boolean inRegion = containFieldOfType(classNode, cnType);
		boolean rightCount = getFieldOfTypeCount(c, "I", false) == 5;
		boolean rightCount1 = getFieldOfTypeCount(c, type, false) == 1;
		return rightCount1 && inRegion && rightCount;
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
			l.add(asFieldHook(h, "getMarkedRenderable"));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "getStrictX", findMultiplier(h, false)));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "getStrictY", findMultiplier(h, false)));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "getPlane", findMultiplier(h, false)));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "getUID", findMultiplier(h, false)));

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "getFlags", findMultiplier(h, false)));

			return l;
		}
	}
}