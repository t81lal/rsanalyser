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
@SupportedHooks(fields = { "getName&Ljava/lang/String;", "getModelZoom&I", "getRotation1&I", "getRotation2&I", "getDiagonalRotation&I", "getModelOffset&I",
		"getModelSine&I", "getValue&I", "getGroundActions&[Ljava/lang/String;", "getWidgetActions&[Ljava/lang/String;", "getMaleEquipPrimaryModel&I",
		"getMaleEquipSecondaryModel&I", "getFemaleEquipPrimaryModel&I", "getFemaleEquipSecondaryModel&I", "getMaleEquipOffset&I", "getFemaleEquipOffset&I",
		"getMaleEmblem&I", "getFemaleEmblem&I", "getMaleDialog&I", "getMaleDialogHat&I", "getFemaleDialog&I", "getFemaleDialogHat&I", "getNoteIndex&I",
		"getNoteTemplateIndex&I", "getModelWidth&I", "getModelHeight&I", "getModelBreadth&I", "getStackedModelLightModifier&I",
		"getStackedModelShadowModifier&I", "getTeamIndex&I", }, methods = {})
public class ItemDefinitionAnalyser extends AbstractClassAnalyser {

	public ItemDefinitionAnalyser() throws AnalysisException {
		super("ItemDefinition");

	}

	@Override
	public boolean matches(ClassNode cn) {
		String obj = "\\[Ljava/lang/String;";
		String superClassName = findObfClassName("DualNode");
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean containFourInts = getFieldOfTypeCount(cn, "\\[S") >= 4;
		boolean containOneString = getFieldOfTypeCount(cn, obj) == 2;
		return containFourInts && rightSuperClass && containOneString;
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

			h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "getName"));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "getModelZoom", findMultiplier(h, false)));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "getRotation1", findMultiplier(h, false)));

			h = findField(i, false, true, 4, 'f', "putfield");
			list.add(asFieldHook(h, "getRotation2", findMultiplier(h, false)));

			h = findField(i, false, true, 5, 'f', "putfield");
			list.add(asFieldHook(h, "getDiagonalRotation", findMultiplier(h, false)));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "getModelOffset", findMultiplier(h, false)));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "getModelSine", findMultiplier(h, false)));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "getValue", findMultiplier(h, false)));

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "getGroundActions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "getWidgetActions"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipPrimaryModel", findMultiplier(h, false)));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipSecondaryModel", findMultiplier(h, false)));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipPrimaryModel", findMultiplier(h, false)));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipSecondaryModel", findMultiplier(h, false)));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipOffset", findMultiplier(h, false)));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipOffset", findMultiplier(h, false)));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "getMaleEmblem", findMultiplier(h, false)));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEmblem", findMultiplier(h, false)));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "getMaleDialog", findMultiplier(h, false)));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "getMaleDialogHat", findMultiplier(h, false)));

			h = findField(i, true, true, 13, 'f', regex);
			list.add(asFieldHook(h, "getFemaleDialog", findMultiplier(h, false)));

			h = findField(i, true, true, 14, 'f', regex);
			list.add(asFieldHook(h, "getFemaleDialogHat", findMultiplier(h, false)));

			h = findField(i, true, true, 15, 'f', regex);
			list.add(asFieldHook(h, "getNoteIndex", findMultiplier(h, false)));

			h = findField(i, true, true, 16, 'f', regex);
			list.add(asFieldHook(h, "getNoteTemplateIndex", findMultiplier(h, false)));

			h = findField(i, true, true, 17, 'f', regex);
			list.add(asFieldHook(h, "getModelWidth", findMultiplier(h, false)));

			h = findField(i, true, true, 18, 'f', regex);
			list.add(asFieldHook(h, "getModelHeight", findMultiplier(h, false)));

			h = findField(i, true, true, 19, 'f', regex);
			list.add(asFieldHook(h, "getModelBreadth", findMultiplier(h, false)));

			h = findField(i, true, true, 20, 'f', regex);
			list.add(asFieldHook(h, "getStackedModelLightModifier", findMultiplier(h, false)));

			h = findField(i, true, true, 21, 'f', regex);
			list.add(asFieldHook(h, "getStackedModelShadowModifier", findMultiplier(h, false)));

			h = findField(i, true, true, 22, 'f', regex);
			list.add(asFieldHook(h, "getTeamIndex", findMultiplier(h, false)));

			return list;
		}
	}
}