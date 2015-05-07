package org.nullbool.impl.analysers.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * @author Bibl
 * @author MalikDz
 */
@SupportedHooks(fields = { "getNextDualNode&DualNode", "getPreviousDualNode&DualNode", }, methods = {})
public class DualNodeAnalyser extends AbstractClassAnalyser {

	public DualNodeAnalyser() throws AnalysisException {
		super("DualNode");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String superClassName = findObfClassName("Node");
		boolean rightStru = (cn.methods.size() == 2) && (cn.fields.size() == 2);
		boolean rightSuperClass = cn.superName.equals(superClassName);
		return rightStru && rightSuperClass;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new SubNodeInfoHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class SubNodeInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(new String[] { "aload", "getfield" }, ms)[0];

			AbstractInsnNode[] i = followJump(m, 12);

			String hook = findField(i, true, true, 1, 'f', "ifnonnull");
			list.add(asFieldHook(hook, "getNextDualNode"));

			hook = findField(i, true, true, 2, 'f', "ifnonnull");
			list.add(asFieldHook(hook, "getPreviousDualNode"));

			return list;
		}
	}
}