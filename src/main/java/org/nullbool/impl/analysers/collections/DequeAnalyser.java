package org.nullbool.impl.analysers.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getHead&Node", "getTail&Node", }, methods = {})
public class DequeAnalyser extends ClassAnalyser {

	public DequeAnalyser() throws AnalysisException {
		super("Deque");

	}

	@Override
	protected boolean matches(ClassNode cn) {
		String node = findObfClassName("Node");
		boolean cond = getFieldOfTypeCount(cn, "L" + node + ";") == 2;
		return cond && (cn.fields.size() == 2) && cn.superName.contains("Object");
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new NodeInfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NodeInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = searchMethod(ms, "<init>");
			AbstractInsnNode[] i = followJump(m, 12);

			String hook = findField(i, false, true, 1, 'f', "getfield");
			list.add(asFieldHook(hook, "getHead"));

			hook = findFieldNotEqual(cn, hook);
			list.add(asFieldHook(hook, "getTail"));

			return list;
		}

		private String findFieldNotEqual(ClassNode cn, String name) {
			String h = name.split("\\.")[1];
			Stream<FieldNode> s = cn.fields.stream();
			Object o = s.filter(f -> !((FieldNode) f).name.equals(h)).toArray()[0];
			return cn.name + "." + ((FieldNode) (o)).name;
		}
	}
}