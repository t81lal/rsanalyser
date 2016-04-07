package org.nullbool.impl.analysers.client.definitions;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "name&Ljava/lang/String;", "width&I", "height&I", "animationId&I", "objMapScene&I", "modelWidth&I",
		"modelHeight&I", "modelBreadth&I", "translationX&I", "translationY&I", "translationZ&I", "walkable&Z",
		"actions&[Ljava/lang/String;", "icon&I", "rotated&Z", "hasCastedShadow&Z", }, methods = {})
public class ObjectDefinitionAnalyser extends ClassAnalyser {

	public ObjectDefinitionAnalyser() throws AnalysisException {
		super("ObjectDefinition");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String obj = "\\[Ljava/lang/String;";
		String npcDefinition = findObfClassName("NPCDefinition");
		String superClassName = findObfClassName("DualNode");
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean containFourInts = getFieldOfTypeCount(cn, "\\[S") >= 4;
		boolean containOneString = getFieldOfTypeCount(cn, obj) == 1;
		boolean gName = !npcDefinition.equals(cn.name);
		return containFourInts && rightSuperClass && containOneString && gName;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new InfoHooks(), new ColourHook());
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
			list.add(asFieldHook(h, "width"));

			h = findField(i, false, true, 3, 'f', "putfield");
			list.add(asFieldHook(h, "height"));

			h = findField(i, false, true, 10, 'f', "putfield");
			list.add(asFieldHook(h, "animationId"));

			h = findField(i, true, true, 3, 'f', regex);
			list.add(asFieldHook(h, "objMapScene"));

			h = findField(i, true, true, 6, 'f', regex);
			list.add(asFieldHook(h, "modelWidth"));

			h = findField(i, true, true, 7, 'f', regex);
			list.add(asFieldHook(h, "modelHeight"));

			h = findField(i, true, true, 8, 'f', regex);
			list.add(asFieldHook(h, "modelBreadth"));

			h = findField(i, true, true, 9, 'f', regex);
			list.add(asFieldHook(h, "translationX"));

			h = findField(i, true, true, 10, 'f', regex);
			list.add(asFieldHook(h, "translationY"));

			h = findField(i, true, true, 11, 'f', regex);
			list.add(asFieldHook(h, "translationZ"));

			h = findField(i, false, true, 5, 'f', "putfield");
			list.add(asFieldHook(h, "walkable"));

			h = findField(i, true, true, 1, 'f', regex);
			list.add(asFieldHook(h, "actions"));

			h = findField(i, true, true, 2, 'f', regex);
			list.add(asFieldHook(h, "icon"));

			h = findField(i, true, true, 4, 'f', regex);
			list.add(asFieldHook(h, "rotated"));

			h = findField(i, true, true, 5, 'f', regex);
			list.add(asFieldHook(h, "hasCastedShadow"));

			return list;
		}
	}

	public class ColourHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String regex = ";\\w*" + "L" +  getFoundClass().name+ ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 55");
			
			//this field nigga...

			
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