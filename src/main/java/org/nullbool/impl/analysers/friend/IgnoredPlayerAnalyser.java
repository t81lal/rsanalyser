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
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.rs.BlockTraverser;
import org.nullbool.api.rs.CaseAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 19:49:29
 */
@SupportedHooks(fields = { "displayName&Ljava/lang/String;", "previousName&Ljava/lang/String;"}, methods = { })
public class IgnoredPlayerAnalyser extends ClassAnalyser {
    
	private final InstructionPattern pattern;
	private String className;
	private FieldInsnNode displayName;
	private FieldInsnNode previousName;
	
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
		
		pattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
				new FieldInsnNode(GETSTATIC, null, null, "[Ljava/lang/String;"),
				new VarInsnNode(ILOAD, 7),
				new IincInsnNode(7, 1),
				new FieldInsnNode(GETSTATIC, "client", null, null),
				new VarInsnNode(ILOAD, 16),
				new InsnNode(AALOAD),
				new FieldInsnNode(GETFIELD, null, null, "Ljava/lang/String;"),
				new InsnNode(AASTORE)
		}));
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(className == null) {
			
			try {
				CaseAnalyser caseAnalyser = Context.current().getCaseAnalyser();
				IControlFlowGraph graph = Context.current().getCFGCache().get(caseAnalyser.getMethod());
				FlowBlock checkBlock = caseAnalyser.findBlock(3622).iterator().next();
				FlowBlock next = null;
				if(checkBlock.lastOpcode() == IF_ICMPEQ) {
					next = checkBlock.target();
				} else if(checkBlock.lastOpcode() == IF_ICMPNE) {
					next = checkBlock.next();
				} else {
					throw new RuntimeException();
				}

				if(next.cleansize() == 1 && next.lastOpcode() == GOTO) {
					next = next.next();
				}
				
				// System.out.println(next.toVerboseString(graph.labels()));
				
				BlockTraverser traverser = new BlockTraverser(next) {
				
					void doStuff(List<AbstractInsnNode> ains) {
						
						// for(int i=0; i < ains.size(); i++) {
						// 	System.out.println((i + 1) + ". " + ains.get(0));
						// }
						
						InstructionSearcher searcher = new InstructionSearcher(ains, pattern);
						if(searcher.search()) {
							displayName = (FieldInsnNode) searcher.getMatches().get(0)[6];
							previousName = (FieldInsnNode) searcher.getMatches().get(1)[6];
							className = displayName.owner;
						}
					}
					
					@Override
					protected FlowBlock unconditional(FlowBlock block) {
						doStuff(block.insns());
						return graph.findTarget(block);
					}
					
					@Override
					protected FlowBlock tableswitch(FlowBlock block) {
						doStuff(block.insns());
						return null;
					}
					
					@Override
					protected FlowBlock lookupswitch(FlowBlock block) {
						doStuff(block.insns());
						return null;
					}
					
					@Override
					protected void exit(FlowBlock block) {
						doStuff(block.insns());
					}
					
					@Override
					protected FlowBlock conditional(FlowBlock block) {						
						JumpInsnNode jin = (JumpInsnNode) block.last();
						if((jin.opcode() == IFEQ && jin.getPrevious().opcode() == IMUL) || (jin.opcode() == IF_ICMPEQ && jin.getPrevious().opcode() == ICONST_0))
							return block.next();
						
						if(jin.opcode() == IF_ICMPGE && jin.getPrevious().opcode() == IMUL)
							return block.next();
						
						return null;
					}
					
					@Override
					protected FlowBlock basic(FlowBlock block) {
						doStuff(block.insns());				
						return null;
					}
				};
				
				traverser.traverseFully();
			} catch (ControlFlowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			list.add(asFieldHook(displayName, "displayName"));
			list.add(asFieldHook(previousName, "previousName"));
			
			return list;
		}
	}
}