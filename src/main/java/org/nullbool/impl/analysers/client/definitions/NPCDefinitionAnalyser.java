package org.nullbool.impl.analysers.client.definitions;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.Type;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "id&I","actions&[Ljava/lang/String;", "onMap&Z", "visible&Z", "clickable&Z", "name&Ljava/lang/String;", "combatLevel&I",
		"width&I", "height&I", "brightness&I", "contrast&I", "headIcon&I", "npcDegToTurn&I", "varpId&I", "settingId&I",
		"npcBoundDim&I", "idleAnimationId&I", "walkAnimationId&I", "npcTurnAround&I", "npcTurnRight&I", "npcTurnLeft&I", }, methods = {})
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
		return new Builder<IFieldAnalyser>().addAll(new InfoHooks(),new IdHook());
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

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "actions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "onMap"));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "visible"));

			h = findField(i, true, true, 13, 'f', regex);
			list.add(asFieldHook(h, "clickable"));

			h = findField(i, false, true, 1, 'f', "putfield");
			list.add(asFieldHook(h, "name"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "combatLevel"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "width"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "height"));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "brightness"));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "contrast"));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "headIcon"));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "npcDegToTurn"));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "varpId"));

			h = findField(i, true, true, 12, 'f', regex);
			list.add(asFieldHook(h, "settingId"));

			h = findField(i, false, true, 2, 'f', "putfield");
			list.add(asFieldHook(h, "npcBoundDim"));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "idleAnimationId"));

			h = findField(i, false, true, 6, 'f', "putfield");
			list.add(asFieldHook(h, "walkAnimationId"));

			h = findField(i, false, true, 7, 'f', "putfield");
			list.add(asFieldHook(h, "npcTurnAround"));

			h = findField(i, false, true, 8, 'f', "putfield");
			list.add(asFieldHook(h, "npcTurnRight"));

			h = findField(i, false, true, 9, 'f', "putfield");
			list.add(asFieldHook(h, "npcTurnLeft"));

			return list;
		}
	}

	public class IdHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
            String h, model = findObfClassName("Model");
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			for(MethodNode m : ms){
				if(Type.getReturnType(m.desc).getClassName().equals(model)){
					h = findField(m,true, false, 1, 'f',"checkcast " + model);
                    list.add(asFieldHook(h,"id"));
				}
			}

			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return null;
	}
}