package org.nullbool.impl.analysers.client.definitions;

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
import org.nullbool.pi.core.hook.api.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getActions&[Ljava/lang/String;", "isOnMap&Z", "isVisible&Z", "isClickable&Z", "getName&Ljava/lang/String;", "getCombatLevel&I",
		"getWidth&I", "getHeight&I", "getBrightness&I", "getContrast&I", "getHeadIcon&I", "getNpcDegToTurn&I", "getVarpId&I", "getSettingId&I",
		"getNpcBoundDim&I", "getIdleAnimationId&I", "getWalkAnimationId&I", "getNpcTurnAround&I", "getNpcTurnRight&I", "getNpcTurnLeft&I", }, methods = {})
public class NPCDefinitionAnalyser extends ClassAnalyser {

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
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new InfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
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
			list.add(asFieldHook(h, "getCombatLevel"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "getWidth"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "getHeight"));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "getBrightness"));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "getContrast"));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "getHeadIcon"));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "getNpcDegToTurn"));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "getVarpId"));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "getSettingId"));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcBoundDim"));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "getIdleAnimationId"));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "getWalkAnimationId"));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnAround"));

			h = findField(i, false, true, 8, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnRight"));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "getNpcTurnLeft"));

			return list;
		}
	}
}