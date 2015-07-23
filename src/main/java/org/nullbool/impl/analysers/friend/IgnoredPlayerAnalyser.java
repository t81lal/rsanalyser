/**
 * 
 */
package org.nullbool.impl.analysers.friend;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.rs.CaseAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.IincNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 19:49:29
 */
@SupportedHooks(fields = { "displayName&Ljava/lang/String;", "previousName&Ljava/lang/String;"}, methods = { })
public class IgnoredPlayerAnalyser extends ClassAnalyser {
    
	private String className;
	private final FieldInsnNode[] insns = new FieldInsnNode[2];
	
	/**
	 * @param name
	 */
	public IgnoredPlayerAnalyser() {
		super("IgnoredPlayer");
		
		//        getstatic al.u:int[]
		//        iinc 6 -1
		//        iload6
		//        iaload
		//        istore16
		//    }
		//    L1270 {
		//        getstatic client.op:int
		//        ldc 870945711 (java.lang.Integer)
		//        imul
		//        ifeq L1271
		//        iload16
		//        getstatic client.ps:int
		//        ldc -2040316809 (java.lang.Integer)
		//        imul
		//        if_icmpge L1271
		//        goto L1272

        
		//    L1272 {
		//        getstatic al.o:java.lang.String[]
		//        iload7
		//        iinc 7 1
		//        getstatic client.pe:o[]
		//        iload16
		//        aaload
		//        getfield o.b:java.lang.String
		//        aastore
		//    }
		//    L1273 {
		//        getstatic al.o:java.lang.String[]
		//        iload7
		//        iinc 7 1
		//        getstatic client.pe:o[]
		//        iload16
		//        aaload
		//        getfield o.e:java.lang.String
		//        aastore
		//        goto L63
		//    }
		
		// pattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
		//		new FieldInsnNode(GETSTATIC, null, null, "[Ljava/lang/String;"),
		//		new VarInsnNode(ILOAD, 7),
		//		new IincInsnNode(7, 1),
		//		new FieldInsnNode(GETSTATIC, "client", null, null),
		//		new VarInsnNode(ILOAD, 16),
		//		new InsnNode(AALOAD),
		//		new FieldInsnNode(GETFIELD, null, null, "Ljava/lang/String;"),
		//		new InsnNode(AASTORE)
		//}));
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(className == null) {
			
				CaseAnalyser caseAnalyser = Context.current().getCaseAnalyser();
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
						if(nn.number() == 3622) {
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
		return new Builder<IFieldAnalyser>().add(new StringFieldsAnalyser());
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
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			list.add(asFieldHook(insns[0], "displayName"));
			list.add(asFieldHook(insns[1], "previousName"));
			
			return list;
		}
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