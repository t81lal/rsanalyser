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
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jul 2015 11:16:32
 */
@SupportedHooks(fields = { "node&Node" }, methods = { "next&(Node)Node", "first&()Node", "insertBefore&(Node)V" })
public class IterableNodeAnalyser extends ClassAnalyser {

	public IterableNodeAnalyser() {
		super("IterableNode");
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
		
		
		ClassNode nodde = getClassNodeByRefactoredName("Node");
		if(nodde == null)
			return false;
		
		String desc = "L" + nodde.name + ";";
		int dc = getFieldCount(cn, new StaticDescFilter(desc));
		if(dc != 1 || cn.countNonStatic() != 1)
			return false;
		
		return true;
	}

	public class NodeFieldAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			ClassNode dual = getClassNodeByRefactoredName("Node");
			String desc = "L" + dual.name + ";";
			
			for(FieldNode f : cn.fields) {
				if(!Modifier.isStatic(f.access) && f.desc.equals(desc)) {
					list.add(asFieldHook(f, "node"));
				}
			}
			
			return list;
		}
	}
	
	public class NodeMethodsAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			ClassNode node = getClassNodeByRefactoredName("Node");
			
			String[] firstPattern = new String[]{"aload 0", "aconst_null", "invokevirtual .*", "areturn"};
            //aload0 // reference to self
            //aconst_null
            //invokevirtual gj e((Lgl;)Lgl;);
            //areturn
			
			String[] insertPattern = new String[]{"aload 1", "invokevirtual " + node.name + ".*()V"};
            //aload1
            //invokevirtual gl iq(()V);

			String desc = "L" + node.name + ";";
			String nextDesc = "(" + desc + ")" + desc;
			
			MethodNode next = null;
			
			for(MethodNode m : cn.methods) {
				if(m.desc.equals(nextDesc)) {
					list.add(asMethodHook(m, "next"));
					next = m;
				}
			}
			
			if(next != null) {
				for(MethodNode m : cn.methods) {
					if(m.desc.equals("()" + desc)) {
						DataPoint p = pointOf(m, firstPattern);
						if(p != null && ((MethodInsnNode) p.instruction().getNext().getNext()).key().equals(next.key())) {
							list.add(asMethodHook(m, "first"));
						}
					} else if(m.desc.equals("(" + desc + ")V")) {
						DataPoint p = pointOf(m, insertPattern);
						if(p != null) {
							list.add(asMethodHook(m, "insertBefore"));
						}
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
		return new Builder<IFieldAnalyser>().add(new NodeFieldAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new NodeMethodsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}