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
@SupportedHooks(fields = { "getName&Ljava/lang/String;", "getWidth&I", "getHeight&I", "getAnimationId&I", "getObjMapScene&I", "getModelWidth&I",
		"getModelHeight&I", "getModelBreadth&I", "getTranslationX&I", "getTranslationY&I", "getTranslationZ&I", "isWalkable&Z",
		"getActions&[Ljava/lang/String;", "getIcon&I", "isRotated&Z", "hasCastedShadow&Z", }, methods = {})
public class ObjectDefinitionAnalyser extends AbstractClassAnalyser {

	public ObjectDefinitionAnalyser() throws AnalysisException {
		super("ObjectDefinition");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String obj = "\\[Ljava/lang/String;";
		String npcDefinition = findObfClassName("NPCDefinition");
		String superClassName = findObfClassName("DualNode");
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean containFourInts = getFieldOfTypeCount(cn, "\\[I") >= 4;
		boolean containOneString = getFieldOfTypeCount(cn, obj) == 1;
		boolean gName = !npcDefinition.equals(cn.name);
		return containFourInts && rightSuperClass && containOneString && gName;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new InfoHooks(), new ColourHook());
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
			list.add(asFieldHook(h, "getWidth", findMultiplier(h, false)));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "getHeight", findMultiplier(h, false)));

			h = findField(i, false, true, 10, 'f', "putfield");
			list.add(asFieldHook(h, "getAnimationId", findMultiplier(h, false)));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "getObjMapScene", findMultiplier(h, false)));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "getModelWidth", findMultiplier(h, false)));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "getModelHeight", findMultiplier(h, false)));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "getModelBreadth", findMultiplier(h, false)));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "getTranslationX", findMultiplier(h, false)));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "getTranslationY", findMultiplier(h, false)));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "getTranslationZ", findMultiplier(h, false)));

			h = findField(i, false, true, 5, 'f', "putfield");
			list.add(asFieldHook(h, "isWalkable"));

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "getActions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "getIcon"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "isRotated"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "hasCastedShadow"));

			return list;
		}
	}

	public class ColourHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			return list;
		}
	}
}