package org.nullbool.impl.analysers.collections;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jul 2015 11:15:46
 */
@SupportedHooks(fields = { "node&Node", "next&Node", "current&Node" }, methods = { })
public class NodeIteratorAnalyser extends ClassAnalyser {

	public NodeIteratorAnalyser() {
		super("NodeIterator");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(!cn.interfaces.contains("java/util/Iterator"))
			return false;
		if(!cn.superName.equals("java/lang/Object"))
			return false;

		ClassNode node = getClassNodeByRefactoredName("Node");
		ClassNode itn = getClassNodeByRefactoredName("IterableNode");
		if(node == null || itn == null)
			return false;
		String desc = "L" + itn.name + ";";
		String desc2 = "L" + node.name + ";";
		int inc = getFieldCount(cn, new StaticDescFilter(desc));
		if(inc != 1)
			return false;
		int nn = getFieldCount(cn, new StaticDescFilter(desc2));
		if(nn != 2)
			return false;
		
		if(cn.countNonStatic() != 3)
			return false;

		return true;
	}
	
	public class FieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			// aload0 // reference to self
            // aconst_null
            // putfield gn.e:gl
			
			for(MethodNode m : cn.methods) {
				if(m.name.equals("remove")) {
					String s = findField2(m, "putfield .*", "aload 0", "aconst_null");
					list.add(asFieldHook(s, "current"));
				} else if(m.name.equals("next")) {
					String s = findField2(m, "putfield .*", "aload 0", "aconst_null");
					list.add(asFieldHook(s, "next"));
				} else if(m.name.equals("<init>")) {
					String s = findField2(m, "putfield .*", "aload 0", "aload 1");
					list.add(asFieldHook(s, "node"));
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
		return new Builder<IFieldAnalyser>().add(new FieldsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}