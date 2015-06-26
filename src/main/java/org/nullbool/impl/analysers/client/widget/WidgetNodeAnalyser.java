package org.nullbool.impl.analysers.client.widget;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = { "getWidgetId&I", "getType&I" }, methods = {})
public class WidgetNodeAnalyser extends ClassAnalyser {

	public WidgetNodeAnalyser() throws AnalysisException {
		super("WidgetNode");
	}

	@Override
	public boolean matches(ClassNode cn) {
		String node = findObfClassName("Node");
		boolean rightSuperClass = cn.superName.equals(node);
		boolean containInts = getFieldOfTypeCount(cn, "I", false) == 2;
		boolean containBoolean = getFieldOfTypeCount(cn, "Z", false) == 1;
		return containInts && containBoolean && rightSuperClass;
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
			String pattern = ";L" + cn.name + ";.*;V";
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = findMethods(Context.current().getClassNodes(), pattern, true);
			String h = findField(ms[0], false, true, 1, 'f', "aload");
			list.add(asFieldHook(h, "getWidgetId"));

			for (FieldNode f : cn.fields) {
				String s = cn.name + "." + f.name;
				if (!s.equals(h) && f.desc.equals("I") && !Modifier.isStatic(f.access)) {
					list.add(asFieldHook(s, "getType"));
				}
			}

			// System.out.println("found:   ");
			// for (MethodNode m : ms) {
			// System.out.println("         " + m.owner.name + "." + m.name + "." + m.desc);
			// }
			return list;
		}
	}
}