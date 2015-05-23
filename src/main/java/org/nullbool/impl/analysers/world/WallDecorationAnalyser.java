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
@SupportedHooks(fields = { "getMarkerRenderable1&Renderable", "getMarkerRenderable2&Renderable", "getStrictX&I", "getStrictY&I", "getPlane&I", "getHash&I",
		"getUID&I", "getOrientation1&I", "getOrientation2&I", }, methods = {})
public class WallDecorationAnalyser extends ClassAnalyser {

	public WallDecorationAnalyser() throws AnalysisException {
		super("WallDecoration");
	}

	@Override
	public boolean matches(ClassNode c) {
		String cnType = "L" + c.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("Tile");
		String type = "L" + findObfClassName("Renderable") + ";";

		boolean fieldPresent = containFieldOfType(c, type);
		boolean rightSuperClass = c.superName.contains("Object");
		boolean fieldInRegion = containFieldOfType(classNode, cnType);
		boolean rightCount = getFieldOfTypeCount(c, "I", false) == 9;
		return fieldPresent && rightSuperClass && fieldInRegion && rightCount;
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
			String[] p = { "invokespecial " + n + ".<init> ()V", "astore 13" };
			MethodNode[] ms = getMethodNodes(c.methods.toArray());
			MethodNode m = identifyMethod(ms, false, p);
			AbstractInsnNode[] ins = followJump(m, 100);

			h = findField(ins, false, true, 6, 'f', "putfield");
			l.add(asFieldHook(h, "getMarkerRenderable1"));

			h = findField(ins, false, true, 7, 'f', "putfield");
			l.add(asFieldHook(h, "getMarkerRenderable2"));

			h = findField(ins, false, true, 3, 'f', "putfield");
			l.add(asFieldHook(h, "getStrictX", findMultiplier(h, false)));

			h = findField(ins, false, true, 4, 'f', "putfield");
			l.add(asFieldHook(h, "getStrictY", findMultiplier(h, false)));

			h = findField(ins, false, true, 5, 'f', "putfield");
			l.add(asFieldHook(h, "getPlane", findMultiplier(h, false)));

			h = findField(ins, false, true, 1, 'f', "putfield");
			l.add(asFieldHook(h, "getHash", findMultiplier(h, false)));

			h = findField(ins, false, true, 2, 'f', "putfield");
			l.add(asFieldHook(h, "getUID", findMultiplier(h, false)));

			h = findField(ins, false, true, 8, 'f', "putfield");
			l.add(asFieldHook(h, "getOrientation1", findMultiplier(h, false)));

			h = findField(ins, false, true, 9, 'f', "putfield");
			l.add(asFieldHook(h, "getOrientation2", findMultiplier(h, false)));
			return l;
		}
	}
}