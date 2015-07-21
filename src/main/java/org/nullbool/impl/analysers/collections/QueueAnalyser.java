package org.nullbool.impl.analysers.collections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.PriorityDFSIterator;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.SuccessorType;
import org.nullbool.api.util.LabelHelper;
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
			
			String[] pat1 = new String[]{"aload 1", "aload 0", "getfield " + cn.name + ".*" + fdesc, "getfield .*" + fdesc, "putfield .*" + fdesc};
			String[] pat2 = new String[]{"aload 1", "aload 0", "getfield " + cn.name + ".*" + fdesc, "putfield .*" + fdesc};
			String desc = "(L" + dual().name + ";)V";
			for(MethodNode m : cn.methods) {
				if(m.desc.equals(desc)) {
					
					AbstractInsnNode ain1 = pointOf(m, pat1).instruction();
					AbstractInsnNode ain2 = pointOf(m, pat2).instruction();
					int i1 = 0;
					int i2= 0;
					
					try {
						IControlFlowGraph graph = Context.current().getCFGCache().get(m);
						//System.out.println(m);
						//System.out.println(graph);
						
						SuccessorTree tree = new SuccessorTree();
						tree.map(graph);

						Comparator<FlowBlock> comparator = new Comparator<FlowBlock>() {
							@Override
							public int compare(FlowBlock o1, FlowBlock o2) {
								Successor s = tree.findRelationship(o1, o2);
								if(s == null) {
									// System.out.println("No fucking relationship between " + o1.id() + " and " + o2.id());
									return 0;
								}
								if(s.type() == SuccessorType.IMMEDIATE)
									return 1;
								if(o1.lastOpcode() == IFNULL)
									return 1;
								return -1;
							}
						};
						
						PriorityDFSIterator it = new PriorityDFSIterator(comparator, graph.entry());
						int i = 0;
						while(it.hasNext()) {
							i++;
							FlowBlock b = it.next();
							if(b.insns().contains(ain1)) {
								i1 = i;
							} else if(b.insns().contains(ain2)) {
								i2 = i;
							}
						}
						
						if(i1 > i2) {
							list.add(asMethodHook(m, "insert"))
						} else if(i2 < i1) {
							
						} else {
							throw new RuntimeException();
						}
					} catch (ControlFlowException e) {
						e.printStackTrace();
					}
					
					//System.out.println(new InstructionIdentifier(m.instructions.toArray()).getInstList());
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
		return new Builder<IMethodAnalyser>().addAll(new GetRemoveMethodAnalysers(), new InsertMethodAnalyser());
	}
}