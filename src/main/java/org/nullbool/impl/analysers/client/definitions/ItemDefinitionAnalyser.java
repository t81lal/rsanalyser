package org.nullbool.impl.analysers.client.definitions;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "name&Ljava/lang/String;", "modelZoom&I", "rotation1&I", "rotation2&I", "diagonalRotation&I", "modelOffset&I",
		"modelSine&I", "value&I", "groundActions&[Ljava/lang/String;", "widgetActions&[Ljava/lang/String;", "maleEquipPrimaryModel&I",
		"maleEquipSecondaryModel&I", "femaleEquipPrimaryModel&I", "femaleEquipSecondaryModel&I", "maleEquipOffset&I", "femaleEquipOffset&I",
		"maleEmblem&I", "femaleEmblem&I", "maleDialog&I", "maleDialogHat&I", "femaleDialog&I", "femaleDialogHat&I", "noteIndex&I",
		"noteTemplateIndex&I", "modelWidth&I", "modelHeight&I", "modelBreadth&I", "stackedModelLightModifier&I",
		"stackedModelShadowModifier&I", "teamIndex&I", }, methods = {})
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
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = searchMethod(ms, "<init>");
			AbstractInsnNode[] i = followJump(m, 70);

			String h, regex = "putfield .* \\[Ljava/lang/String;";

			h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "name"));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "modelZoom"));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "rotation1"));

			h = findField(i, false, true, 4, 'f', "putfield");
			list.add(asFieldHook(h, "rotation2"));

			h = findField(i, false, true, 5, 'f', "putfield");
			list.add(asFieldHook(h, "diagonalRotation"));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "modelOffset"));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "modelSine"));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "value"));

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "groundActions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "widgetActions"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "maleEquipPrimaryModel"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "maleEquipSecondaryModel"));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "femaleEquipPrimaryModel"));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "femaleEquipSecondaryModel"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "maleEquipOffset"));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "femaleEquipOffset"));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "maleEmblem"));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "femaleEmblem"));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "maleDialog"));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "maleDialogHat"));

			h = findField(i, true, true, 13, 'f', regex);
			list.add(asFieldHook(h, "femaleDialog"));

			h = findField(i, true, true, 14, 'f', regex);
			list.add(asFieldHook(h, "femaleDialogHat"));

			h = findField(i, true, true, 15, 'f', regex);
			list.add(asFieldHook(h, "noteIndex"));

			h = findField(i, true, true, 16, 'f', regex);
			list.add(asFieldHook(h, "noteTemplateIndex"));

			h = findField(i, true, true, 17, 'f', regex);
			list.add(asFieldHook(h, "modelWidth"));

			h = findField(i, true, true, 18, 'f', regex);
			list.add(asFieldHook(h, "modelHeight"));

			h = findField(i, true, true, 19, 'f', regex);
			list.add(asFieldHook(h, "modelBreadth"));

			h = findField(i, true, true, 20, 'f', regex);
			list.add(asFieldHook(h, "stackedModelLightModifier"));

			h = findField(i, true, true, 21, 'f', regex);
			list.add(asFieldHook(h, "stackedModelShadowModifier"));

			h = findField(i, true, true, 22, 'f', regex);
			list.add(asFieldHook(h, "teamIndex"));

			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		// TODO Auto-generated method stub
		return null;
	}
}