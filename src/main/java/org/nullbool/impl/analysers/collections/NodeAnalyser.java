package org.nullbool.impl.analysers.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.nullbool.api.analysis.AbstractClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.ClassStructure;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getKey&J", "getPrevious&Node", "getNext&Node", }, methods = {})
public class NodeAnalyser extends AbstractClassAnalyser {

	public NodeAnalyser() throws AnalysisException {
		super("Node");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		// boolean rightField = containFieldOfType(cn, "J");
		// boolean rightStructure = (cn.methods.size() == 3) && (cn.fields.size() == 3);
		// if (cn.name.equals("gm")) {
		// System.out.println(cn.methods.size());
		// System.out.println(rightField);
		// System.out.println(rightStructure);
		// }
		// return rightStructure && rightField;
		return cn.superName.equals("java/lang/Object") && ((ClassStructure) cn).delegates.size() > 25;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new NodeInfoHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NodeInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.stream().filter(element -> element.desc.endsWith("Z")).collect(Collectors.toList()).toArray());

			MethodNode m = startWithBc(new String[] { "aload", "getfield", "ifnonnull" }, ms)[0];

			FieldInsnNode prevFin = (FieldInsnNode) Arrays.asList(m.instructions.toArray()).stream().filter(ain -> ain.opcode() == GETFIELD).findFirst()
					.get();
			list.add(asFieldHook(cn.name + "." + prevFin.name, "getPrevious"));

			FieldNode nextFn = cn.fields.stream().filter(f -> f.desc.equals(prevFin.desc) && !f.name.equals(prevFin.name)).findFirst().get();
			list.add(asFieldHook(nextFn.owner.name + "." + nextFn.name, "getNext"));

			String hook = getFieldOfType(cn, "J", false);
			list.add(asFieldHook(hook, "getKey"));

			return list;
		}
	}
}