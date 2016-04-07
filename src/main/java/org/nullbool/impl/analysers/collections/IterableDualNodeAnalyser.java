package org.nullbool.impl.analysers.collections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.BoundedInstructionIdentifier.DataPoint;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jul 2015 22:54:04
 */
@SupportedHooks(fields = { "node&Node" }, methods = { "unlinkAll&()V", "insertBefore&(Node)V" })
public class IterableDualNodeAnalyser extends ClassAnalyser {

	public IterableDualNodeAnalyser() {
		super("IterableDualNode");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(!cn.superName.equals("java/lang/Object"))
			return false;
		if(!cn.interfaces.contains("java/lang/Iterable"))
			return false;


		ClassNode nodde = getClassNodeByRefactoredName("DualNode");
		if(nodde == null)
			return false;

		String desc = "L" + nodde.name + ";";
		int dc = getFieldCount(cn, new StaticDescFilter(desc));
		if(dc != 1 || cn.countNonStatic() != 1)
			return false;

		return true;
	}

	public class DualNodeFieldAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			ClassNode dual = getClassNodeByRefactoredName("DualNode");
			String desc = "L" + dual.name + ";";

			for(FieldNode f : cn.fields) {
				if(!Modifier.isStatic(f.access) && f.desc.equals(desc)) {
					list.add(asFieldHook(f, "node"));
				}
			}

			return list;
		}
	}

	public class DualNodeMethodsAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			ClassNode node = getClassNodeByRefactoredName("DualNode");

			// aload0 // reference to self
			// getfield gq.p:gk
			// getfield gk.cl:gk
			// invokevirtual gk ew(()V);
			String[] unlinkAllPattern = new String[]{"aload 0", "getfield .*", "getfield .*", "invokevirtual .*"};

			String[] insertPattern = new String[]{"aload 1", "invokevirtual " + node.name + ".*()V"};
			//aload1
			//invokevirtual gl iq(()V);

			String desc = "L" + node.name + ";";

			for(MethodNode m : cn.methods) {
				if(m.desc.equals("()V")) {
					if(!m.name.equals("<init>") && !m.name.equals("<clinit>")) {
						DataPoint p = pointOf(m, unlinkAllPattern);
						if(p != null) {
							list.add(asMethodHook(m, "unlinkAll"));
						}
					}
				} else if(m.desc.equals("(" + desc + ")V")) {
					DataPoint p = pointOf(m, insertPattern);
					if(p != null) {
						list.add(asMethodHook(m, "insertBefore"));
					}
				}
			} 

			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new DualNodeFieldAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new DualNodeMethodsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}