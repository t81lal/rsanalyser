package org.nullbool.impl.analysers.client.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.AbstractClassAnalyser;
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
@SupportedHooks(fields = { "getActions&[Ljava/lang/String;", "isOnMap&Z", "isVisible&Z", "isClickable&Z", "getName&Ljava/lang/String;", "getCombatLevel&I",
		"getWidth&I", "getHeight&I", "getBrightness&I", "getContrast&I", "getHeadIcon&I", "getNpcDegToTurn&I", "getVarpId&I", "getSettingId&I",
		"getNpcBoundDim&I", "getIdleAnimationId&I", "getWalkAnimationId&I", "getNpcTurnAround&I", "getNpcTurnRight&I", "getNpcTurnLeft&I", }, methods = {})
public class NPCDefinitionAnalyser extends AbstractClassAnalyser {

	public NPCDefinitionAnalyser() throws AnalysisException {
		super("NPCDefinition");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String type = "L" + cn.name + ";";
		ClassNode classNode = getClassNodeByRefactoredName("NPC");
		String[] pattern = { "iconst_5", "anewarray", "putfield" };
		String superClassName = findObfClassName("DualNode");
		boolean fieldIsInNpcClass = containFieldOfType(classNode, type);
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean goodPattern = findMethod(cn, "<init>", pattern);
		return rightSuperClass && goodPattern && fieldIsInNpcClass;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new InfoHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class InfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = searchMethod(ms, "<init>");
			AbstractInsnNode[] i = followJump(m, 70);

			String h, regex = "putfield .* \\[Ljava/lang/String;";

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "getActions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "isOnMap"));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "isVisible"));

			h = findField(i, true, true, 13, 'f', regex);
			list.add(asFieldHook(h, "isClickable"));

			h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "getName"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "getCombatLevel", findMultiplier(h, false)));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "getWidth", findMultiplier(h, false)));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "getHeight", findMultiplier(h, false)));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "getBrightness", findMultiplier(h, false)));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "getContrast", findMultiplier(h, false)));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "getHeadIcon", findMultiplier(h, false)));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "getNpcDegToTurn", findMultiplier(h, false)));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "getVarpId", findMultiplier(h, false)));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "getSettingId", findMultiplier(h, false)));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcBoundDim", findMultiplier(h, false)));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "getIdleAnimationId", findMultiplier(h, false)));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "getWalkAnimationId", findMultiplier(h, false)));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnAround", findMultiplier(h, false)));

			h = findField(i, false, true, 8, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnRight", findMultiplier(h, false)));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnLeft", findMultiplier(h, false)));

			return list;
		}
	}
}