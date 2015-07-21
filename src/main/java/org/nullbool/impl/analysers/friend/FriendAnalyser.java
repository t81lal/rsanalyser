package org.nullbool.impl.analysers.friend;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.rs.CaseAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.IincNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 14:06:29
 */
@SupportedHooks(
		fields = { "displayName&Ljava/lang/String;", "previousName&Ljava/lang/String;", "world&I"}, 
		methods = { }
)
public class FriendAnalyser extends ClassAnalyser implements Opcodes {

	private String className;
	private final FieldInsnNode[] insns = new FieldInsnNode[2];
	// private final InstructionPattern worldPattern;
	
	/**
	 * @param name
	 */
	public FriendAnalyser() {
		super("Friend");
		
        //L1148 {
        //    getstatic al.stringOperandStack:java.lang.String[]
        //    iload7
        //    iinc 7 1
        //    getstatic Client.friends:Friend[]
        //    iload16
        //    aaload
        //    getfield Friend.displayName:java.lang.String
        //    aastore
        //}
        //L1149 {
        //    getstatic al.stringOperandStack:java.lang.String[]
        //    iload7
        //    iinc 7 1
        //    getstatic Client.friends:Friend[]
        //    iload16
        //   aaload
        //    getfield Friend.previousName:java.lang.String
        //   aastore

            
		// classPattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
		//		new FieldInsnNode(GETSTATIC, null, null, "[Ljava/lang/String;"),
		//		new VarInsnNode(ILOAD, 7),
		//		new IincInsnNode(7, 1),
		//		new FieldInsnNode(GETSTATIC, "client", null, null),
		//		new VarInsnNode(ILOAD, 16),
		//		new InsnNode(AALOAD),
		//		new FieldInsnNode(GETFIELD, null, null, "Ljava/lang/String;"),
		//		new InsnNode(AASTORE)
		//}));
		
        //getstatic al.u:int[]
        //iload6
        //iinc 6 1
        //getstatic Client.om:Friend[]
        //iload16
        //aaload
        //getfield Friend.a:int
        //ldc -1825973503 (java.lang.Integer)
        //imul
        //iastore
		
		// worldPattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
		//		new FieldInsnNode(GETSTATIC, null, null, "[I"),
		//		new VarInsnNode(ILOAD, 6),
		//		new IincInsnNode(6, 1),
		//		new FieldInsnNode(GETSTATIC, "client", null, null),
		//		new VarInsnNode(ILOAD, 16),
		//		new InsnNode(AALOAD),
		//		new FieldInsnNode(GETFIELD, null, null, "I"),
		//}));
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(className == null) {
			
			// try {
				CaseAnalyser caseAnalyser = Context.current().getCaseAnalyser();
				// IControlFlowGraph graph = Context.current().getCFGCache().get(caseAnalyser.getMethod());
				// FlowBlock checkBlock = caseAnalyser.findBlock(3601).iterator().next();
				NodeVisitor nv = new NodeVisitor() {
					boolean stopped = false;
					boolean started = false;
					int pos = 0;
					
					@Override
					public void visitAny(AbstractNode an) {
						if(pos >= 2) {
							stopped = true;
						}
					}
					
					@Override
					public void visitNumber(NumberNode nn) {
						if(stopped || started)
							return;
						if(nn.number() == 3601) {
							started = true;
						}
					}
					
					@Override
					public void visit(AbstractNode an) {
						if(stopped || !started)
							return;
						if(an.opcode() == AASTORE) {
							FieldMemberNode _fmn = an.t_first(GETSTATIC);
							if(_fmn == null || !_fmn.desc().equals("[Ljava/lang/String;")) {
								return;
							}
							FieldMemberNode fmn = an.t_first(GETFIELD);
							if(fmn == null || !fmn.desc().equals("Ljava/lang/String;")) {
								return;
							}
							IincNode inc = an.firstIinc();
							VariableNode vn = an.firstVariable();
							if(inc == null || inc.increment() != 1 || vn == null || vn.opcode() != ILOAD || vn.var() != inc.var()) {
								return;
							}
							
							AbstractNode load = fmn.t_first(AALOAD);
							if(load != null) {
								
								FieldMemberNode f = load.firstField();
								VariableNode v = load.firstVariable();
								if(f == null || v == null || f.opcode() != GETSTATIC || !f.desc().startsWith("[L")) {
									return;
								}
								
								insns[pos++] = fmn.fin();
							}
							
						}
					}
				};
				TreeBuilder tb = new TreeBuilder();
				tb.build(caseAnalyser.getMethod()).accept(nv);

				FieldInsnNode f0 = insns[0];
				FieldInsnNode f1 = insns[1];
				if(f0 != null && f1 != null && f0.owner.equals(f1.owner)) {
					className = f0.owner;
				}
				
			// } catch(ControlFlowException e) {
			// 	e.printStackTrace();
			// }
			
			if(className == null) {
				className = "";
			}
		}
		
		return className.equals(cn.name);
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new StringFieldsAnalyser(), new WorldFieldAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
	
	public class StringFieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			list.add(asFieldHook(insns[0], "displayName"));
			list.add(asFieldHook(insns[1], "previousName"));
			
			return list;
		}
	}
	
	public class WorldFieldAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			CaseAnalyser caseAnalyser = Context.current().getCaseAnalyser();
			NodeVisitor nv = new NodeVisitor() {
				boolean stopped = false;
				boolean started = false;
				
				@Override
				public void visitNumber(NumberNode nn) {
					if(stopped || started)
						return;
					if(nn.number() == 3602) {
						started = true;
					}
				}
				
				@Override
				public void visit(AbstractNode an) {
					if(stopped || !started)
						return;
					if(an.opcode() == IASTORE) {
						FieldMemberNode _fmn = an.t_first(GETSTATIC);
						if(_fmn == null || !_fmn.desc().equals("[I")) {
							return;
						}
						ArithmeticNode arn = an.t_first(IMUL);
						if(arn == null) {
							return;
						}
						FieldMemberNode fmn = arn.t_first(GETFIELD);
						if(fmn == null || !fmn.owner().equals(cn.name) || !fmn.desc().equals("I")) {
							return;
						}
						IincNode inc = an.firstIinc();
						VariableNode vn = an.firstVariable();
						if(inc == null || inc.increment() != 1 || vn == null || vn.opcode() != ILOAD || vn.var() != inc.var()) {
							return;
						}
						
						AbstractNode load = fmn.t_first(AALOAD);
						if(load != null) {
							FieldMemberNode f = load.firstField();
							VariableNode v = load.firstVariable();
							if(f == null || v == null || f.opcode() != GETSTATIC || !f.desc().startsWith("[L")) {
								return;
							}
							
							list.add(asFieldHook(fmn.fin(), "world"));
							stopped = true;
						}
						
					}
				}
			};
			TreeBuilder tb = new TreeBuilder();
			tb.build(caseAnalyser.getMethod()).accept(nv);
			
			return list;
		}
	}
}