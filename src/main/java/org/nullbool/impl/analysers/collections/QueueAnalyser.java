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
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.objectweb.custom_asm.tree.InsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;
import org.objectweb.custom_asm.tree.TypeInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jul 2015 02:55:01
 */
@SupportedHooks(fields = { "head&DualNode" }, 
				methods = { "get&()DualNode", "remove&()DualNode", 
							"insertHead&(DualNode)V", "insertTail&(DualNode)V" })
public class QueueAnalyser extends ClassAnalyser {

	private ClassNode dual;

	public QueueAnalyser() {
		super("Queue");
	}

	private ClassNode dual() {
		if(dual == null) {
			dual = getClassNodeByRefactoredName("DualNode");
		}
		return dual;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(!cn.superName.equals("java/lang/Object"))
			return false;
		if(cn.interfaces.size() != 0)
			return false;

		String desc = "L" + dual().name + ";";
		int dns = getFieldCount(cn, new StaticDescFilter(desc));
		if(dns != 1)
			return false;
		int ns = 0;
		for(FieldNode f : cn.fields) {
			if(!Modifier.isStatic(f.access)) {
				ns++;
			}
		}
		if(ns != 1)
			return false;

		for(MethodNode m : cn.methods) {
			if(m.name.equals("<init>")) {
				//  aload0 // reference to self
				//  new gk
				//  dup
				//  invokespecial gk <init>(()V);
				// putfield gp.p:gk
				InstructionPattern pattern = new InstructionPattern(new AbstractInsnNode[]{
						new VarInsnNode(ALOAD, 0),
						new TypeInsnNode(NEW, dual().name),
						new InsnNode(DUP),
						new MethodInsnNode(INVOKESPECIAL, dual().name, "<init>", "()V", false),
						new FieldInsnNode(PUTFIELD, cn.name, null, desc)
				});
				InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
				if(searcher.search()) {
					return true;
				}
			}
		}

		return false;
	}

	public class HeadFieldAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {

			List<FieldHook> list = new ArrayList<FieldHook>();

			String desc = "L" + dual().name + ";";

			for(FieldNode f : cn.fields) {
				if(!Modifier.isStatic(f.access)) {
					if(f.desc.equals(desc)) {
						list.add(asFieldHook(f, "head"));
						// let it run on, shouldn't dup hook unless there is some
						// huge gaping wide bug in asm/something else.
					}
				}
			}

			return list;
		}
	}

	public class GetRemoveMethodAnalysers implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			String desc = "()L" + dual() + ";";

			for(MethodNode m : cn.methods) {
				if(m.desc.equals(desc)) {
					if(findMethod(cn, m, new String[]{"aconst_null", "areturn"}) && findMethod(cn, m, new String[]{"aload", "areturn"})) {
						if(findOpcodePattern(m, new int[]{INVOKEVIRTUAL}) != null) {
							list.add(asMethodHook(m, "remove"));
						} else {
							list.add(asMethodHook(m, "get"));
						}
					}
				}
			}

			return list;
		}
	}

	public class InsertMethodsAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			String fdesc = "L" + dual().name + ";";
			
			FieldHook prev = getAnalyser("DualNode").getFoundHook().fbyRefactoredName("previousDualNode");
			FieldHook next = getAnalyser("DualNode").getFoundHook().fbyRefactoredName("nextDualNode");
			//System.out.println(fdesc);
			// cant get this getfield to work
			
			// aload1
			// aload0 // reference to self
			// getfield gp.p:gk
			// getfield gk.ca:gk
			// putfield gk.ca:gk
			
            // aload1
            // aload0 // reference to self
            // getfield gp.p:gk
            // putfield gk.cl:gk

			
			// the difference between the two methods is that two lines
			// look like they are switched
			// so we gotta match them in the right order
			// fuck that, different fields used.
			
			String[] pat1 = new String[]{"aload 1", "aload 0", "getfield " + cn.name + ".*" + fdesc, "getfield .*" + fdesc, "putfield .*" + fdesc};
			String[] pat2 = new String[]{"aload 1", "aload 0", "getfield " + cn.name + ".*" + fdesc, "putfield .*" + fdesc};
			String desc = "(L" + dual().name + ";)V";
			for(MethodNode m : cn.methods) {
				if(m.desc.equals(desc)) {
					
					AbstractInsnNode ain1 = pointOf(m, pat1).instruction();
					AbstractInsnNode ain2 = pointOf(m, pat2).instruction();
					
					if(ain1 != null && ain2 != null) {
						FieldInsnNode fin = (FieldInsnNode) ain1.getNext().getNext().getNext();
						
						if(fin.name.equals(next.obfuscated())) {
							list.add(asMethodHook(m, "insertHead"));
						} else if(fin.name.equals(prev.obfuscated())) {
							list.add(asMethodHook(m, "insertTail"));
						} else {
							throw new RuntimeException();
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
		System.out.println("queue is: " + getFoundClass().name);
		return new Builder<IFieldAnalyser>().addAll(new HeadFieldAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new GetRemoveMethodAnalysers(), new InsertMethodsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}