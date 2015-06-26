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
@SupportedHooks(fields = { "getName&Ljava/lang/String;", "getModelZoom&I", "getRotation1&I", "getRotation2&I", "getDiagonalRotation&I", "getModelOffset&I",
		"getModelSine&I", "getValue&I", "getGroundActions&[Ljava/lang/String;", "getWidgetActions&[Ljava/lang/String;", "getMaleEquipPrimaryModel&I",
		"getMaleEquipSecondaryModel&I", "getFemaleEquipPrimaryModel&I", "getFemaleEquipSecondaryModel&I", "getMaleEquipOffset&I", "getFemaleEquipOffset&I",
		"getMaleEmblem&I", "getFemaleEmblem&I", "getMaleDialog&I", "getMaleDialogHat&I", "getFemaleDialog&I", "getFemaleDialogHat&I", "getNoteIndex&I",
		"getNoteTemplateIndex&I", "getModelWidth&I", "getModelHeight&I", "getModelBreadth&I", "getStackedModelLightModifier&I",
		"getStackedModelShadowModifier&I", "getTeamIndex&I", }, methods = {})
public class ItemDefinitionAnalyser extends ClassAnalyser {

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

			h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "getName"));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "getModelZoom"));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "getRotation1"));

			h = findField(i, false, true, 4, 'f', "putfield");
			list.add(asFieldHook(h, "getRotation2"));

			h = findField(i, false, true, 5, 'f', "putfield");
			list.add(asFieldHook(h, "getDiagonalRotation"));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "getModelOffset"));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "getModelSine"));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "getValue"));

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "getGroundActions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "getWidgetActions"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipPrimaryModel"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipSecondaryModel"));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipPrimaryModel"));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipSecondaryModel"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "getMaleEquipOffset"));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEquipOffset"));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "getMaleEmblem"));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "getFemaleEmblem"));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "getMaleDialog"));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "getMaleDialogHat"));

			h = findField(i, true, true, 13, 'f', regex);
			list.add(asFieldHook(h, "getFemaleDialog"));

			h = findField(i, true, true, 14, 'f', regex);
			list.add(asFieldHook(h, "getFemaleDialogHat"));

			h = findField(i, true, true, 15, 'f', regex);
			list.add(asFieldHook(h, "getNoteIndex"));

			h = findField(i, true, true, 16, 'f', regex);
			list.add(asFieldHook(h, "getNoteTemplateIndex"));

			h = findField(i, true, true, 17, 'f', regex);
			list.add(asFieldHook(h, "getModelWidth"));

			h = findField(i, true, true, 18, 'f', regex);
			list.add(asFieldHook(h, "getModelHeight"));

			h = findField(i, true, true, 19, 'f', regex);
			list.add(asFieldHook(h, "getModelBreadth"));

			h = findField(i, true, true, 20, 'f', regex);
			list.add(asFieldHook(h, "getStackedModelLightModifier"));

			h = findField(i, true, true, 21, 'f', regex);
			list.add(asFieldHook(h, "getStackedModelShadowModifier"));

			h = findField(i, true, true, 22, 'f', regex);
			list.add(asFieldHook(h, "getTeamIndex"));

			return list;
		}
	}
}