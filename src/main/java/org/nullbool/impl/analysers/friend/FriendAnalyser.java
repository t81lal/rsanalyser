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
import org.objectweb.asm.Opcodes;
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
 * @created 24 Jun 2015 14:06:29
 */
@SupportedHooks(
		fields = { "getDisplayName&Ljava/lang/String;", "getPreviousName&Ljava/lang/String;", "getWorld&I"}, 
		methods = { }
)
public class FriendAnalyser extends ClassAnalyser implements Opcodes {

	private final InstructionPattern classPattern;
	private String className;
	private FieldInsnNode displayName;
	private FieldInsnNode previousName;
	private final InstructionPattern worldPattern;
	
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

            
		classPattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
				new FieldInsnNode(GETSTATIC, null, null, "[Ljava/lang/String;"),
				new VarInsnNode(ILOAD, 7),
				new IincInsnNode(7, 1),
				new FieldInsnNode(GETSTATIC, "client", null, null),
				new VarInsnNode(ILOAD, 16),
				new InsnNode(AALOAD),
				new FieldInsnNode(GETFIELD, null, null, "Ljava/lang/String;"),
				new InsnNode(AASTORE)
		}));
		
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
		
		worldPattern = new InstructionPattern(InstructionPattern.translate(new AbstractInsnNode[]{
				new FieldInsnNode(GETSTATIC, null, null, "[I"),
				new VarInsnNode(ILOAD, 6),
				new IincInsnNode(6, 1),
				new FieldInsnNode(GETSTATIC, "client", null, null),
				new VarInsnNode(ILOAD, 16),
				new InsnNode(AALOAD),
				new FieldInsnNode(GETFIELD, null, null, "I"),
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
				FlowBlock checkBlock = caseAnalyser.findBlock(3601).iterator().next();
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
				
				BlockTraverser traverser = new BlockTraverser(next) {
				
					void doStuff(List<AbstractInsnNode> ains) {
						InstructionSearcher searcher = new InstructionSearcher(ains, classPattern);
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
						if(jin.opcode() == IF_ICMPNE && jin.getPrevious().opcode() == ICONST_2)
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
			
			list.add(asFieldHook(displayName, "getDisplayName"));
			list.add(asFieldHook(previousName, "getPreviousName"));
			
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

			try {
				CaseAnalyser caseAnalyser = Context.current().getCaseAnalyser();
				IControlFlowGraph graph = Context.current().getCFGCache().get(caseAnalyser.getMethod());
				FlowBlock checkBlock = caseAnalyser.findBlock(3602).iterator().next();
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
				
				BlockTraverser traverser = new BlockTraverser(next) {
				
					void doStuff(List<AbstractInsnNode> ains) {
						InstructionSearcher searcher = new InstructionSearcher(ains, worldPattern);
						if(searcher.search()) {
							FieldInsnNode worldFin = (FieldInsnNode) searcher.getMatches().get(0)[6];
							list.add(asFieldHook(worldFin, "getWorld"));
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
						if(jin.opcode() == IF_ICMPNE && jin.getPrevious().opcode() == ICONST_2)
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
				e.printStackTrace();
			}
			
			return list;
		}
	}
}