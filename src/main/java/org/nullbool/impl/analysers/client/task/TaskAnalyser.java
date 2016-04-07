package org.nullbool.impl.analysers.client.task;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.BoundedInstructionIdentifier.DataPoint;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 23 Jul 2015 12:33:51
 */
@SupportedHooks(
		fields = { "status&I", "type&I", "intArg&I", "result&Ljava/lang/Object;", "objArg&Ljava/lang/Object;", "next&Task"}, 
		methods = { }
)
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
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
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
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
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
			
//            aload0 // reference to self
//            getfield df.t:ej
//            aload6
//            putfield ej.y:ej
			
			String[] nextpattern = new String[]{
					"aload 0", 
					"getfield.*" + method.owner.name + ".*L" + cn.name + ";", 
					"aload 6",
					"putfield.*" + cn.name + ".*L" + cn.name + ";.*" 
			};

			DataPoint dp = pointOf(method, nextpattern);
			if(dp != null) {
				FieldInsnNode fin = (FieldInsnNode) dp.instruction().getNext().getNext().getNext();
				list.add(asFieldHook(fin, "next"));
			}
			
			TreeBuilder tb = new TreeBuilder();
			
			for(MethodNode m : method.owner.methods) {
				if(m.name.equals("run") && m.desc.equals("()V")) {
					Set<FieldInsnNode> objfins = new HashSet<FieldInsnNode>();
					
					Set<Integer> ints = new HashSet<Integer>();
					Set<FieldInsnNode> intfins = new HashSet<FieldInsnNode>();

					
					NodeVisitor collector = new NodeVisitor() {
						@Override
						public void visitField(FieldMemberNode fmn) {
							if(fmn.owner().equals(cn.name) && fmn.opcode() == PUTFIELD) {
								if(fmn.desc().equals("I")) {
									NumberNode nn = fmn.firstNumber();
									if(nn != null) {
										ints.add(nn.number());
										intfins.add(fmn.fin());
									}
								} else if(fmn.desc().equals("Ljava/lang/Object;")) {
									objfins.add(fmn.fin());
								}
							}
						}
					};
					tb.build(m).accept(collector);

					if(intfins.size() > 0 && same(intfins) && contains(ints, new int[]{1, 2})) {
						list.add(asFieldHook(intfins.iterator().next(), "status"));
					}
					
					if(objfins.size() > 0 && same(objfins)) {
						list.add(asFieldHook(objfins.iterator().next(), "result"));
					}
				}
			}
			
			return list;
		}
	}
	
	private static boolean contains(Set<Integer> ints, int[] arr) {
		for(int i : arr) {
			if(!ints.contains(i))
				return false;
		}
		return true;
	}
	
	private static boolean same(Set<FieldInsnNode> fins) {
		String key = null;
		for(FieldInsnNode fin : fins) {
			if(key == null) {
				key = fin.key();
			} else {
				if(!fin.key().equals(key))
					return false;
			}
		}
		return true;
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