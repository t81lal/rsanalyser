package org.nullbool.impl.analysers.collections;

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
 * @author Bibl
 * @author MalikDz
 */
@SupportedHooks(fields = { "nextDualNode&DualNode", "previousDualNode&DualNode", }, methods = {})
public class DualNodeAnalyser extends ClassAnalyser {

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
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new SubNodeInfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class SubNodeInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(new String[] { "aload", "getfield" }, ms)[0];

			AbstractInsnNode[] i = followJump(m, 12);

			String hook = findField(i, true, true, 1, 'f', "ifnonnull");
			list.add(asFieldHook(hook, "nextDualNode"));

			hook = findField(i, true, true, 2, 'f', "ifnonnull");
			list.add(asFieldHook(hook, "previousDualNode"));

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