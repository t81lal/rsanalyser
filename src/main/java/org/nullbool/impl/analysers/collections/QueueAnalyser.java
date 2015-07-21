package org.nullbool.impl.analysers.collections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.InstructionIdentifier;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jul 2015 02:55:01
 */
@SupportedHooks(fields = { "head&DualNode" }, 
				methods = { "get&()DualNode", "remove&()DualNode" })
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
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
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
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode cn) {
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
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<MethodHook> find(ClassNode cn) {
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
	
	public class InsertMethodAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			String fdesc = "L" + dual().name + ";";
			System.out.println(fdesc);
			String[] pat1 = new String[]{"aload 1", "aload 0", "getfield .*"};
			
			String desc = "(L" + dual().name + ";)V";
			for(MethodNode m : cn.methods) {
				if(m.desc.equals(desc)) {
					System.out.println(identifyMethod(m, pat1));
					System.out.println(new InstructionIdentifier(m.instructions.toArray()).getInstList());
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
		return new Builder<IFieldAnalyser>().addAll(new HeadFieldAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new GetRemoveMethodAnalysers(), new InsertMethodAnalyser());
	}
}