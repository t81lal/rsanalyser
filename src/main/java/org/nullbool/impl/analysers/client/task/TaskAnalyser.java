package org.nullbool.impl.analysers.client.task;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 23 Jul 2015 12:33:51
 */
@SupportedHooks(fields = { "type&I", "intArg&I", "objArg&Ljava/lang/Object;" }, methods = { })
public class TaskAnalyser extends ClassAnalyser {

	private String cname;
	private MethodNode method;
	
	public TaskAnalyser() {
		super("Task");
	}
	
	void find() {
		TaskHandlerAnalyser tha = (TaskHandlerAnalyser) getAnalyser("TaskHandler");
		if(tha != null) {
			for(MethodNode m : tha.getFoundClass().methods) {
				if(!Modifier.isStatic(m.access)) {
					if(m.desc.contains(")L")) {
						if(m.desc.startsWith("(IIILjava/lang/Object;)")) {
							String s = m.desc.substring(m.desc.indexOf(')') + 2);
							s = s.substring(0, s.length() - 1);
							cname = s;
							method = m;
						}
					}
				}
			}
		}
		
		if(cname == null) {
			cname = "";
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(cname == null) {
			find();
		}
		
		return cn.name.equals(cname);
	}
	
	public class FieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

//            aload6
//            iload1 // reference to arg0
//            ldc 400964707 (java.lang.Integer)
//            imul
//            putfield rs/ej.e:int

            String[] typepattern = new String[]{"aload 6", "iload 1", "ldc.*", "imul"};
            
//            aload6
//            iload2 // reference to arg1
//            putfield rs/ej.q:int

            String[] intargpattern = new String[]{"aload 6", "iload 2"};

//          aload6
//          aload4
//          putfield rs/ej.z:java.lang.Object
            
            String[] objargpattern = new String[]{"aload 6", "aload 4"};
            
            String s = findField2(method, "putfield.*", typepattern);
			list.add(asFieldHook(s, "type"));
			
			s = findField2(method, "putfield.*", intargpattern);
			list.add(asFieldHook(s, "intArg"));
			
			s = findField2(method, "putfield.*", objargpattern);
			list.add(asFieldHook(s, "objArg"));
			

			
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
		// TODO Auto-generated method stub
		return null;
	}
}