/**
 * 
 */
package org.nullbool.api.obfuscation;

import java.util.List;

import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 00:09:37
 */
public class RedunantGotoTransformer implements Opcodes {

	public void restructure(MethodNode m, IControlFlowGraph graph) {
		int simple = 0;
		int complex = 0;

		List<FlowBlock> blocks = graph.blocks();
		for(int i=0; i < blocks.size(); i++) {
			FlowBlock b = blocks.get(i);
			if(((i + 1) < blocks.size()) && b.cleansize() == 1 && b.lastOpcode() == GOTO) {
				FlowBlock n = blocks.get(i + 1);
				if(n == null || b.target() == null) {
					throw new RuntimeException();
				}
				
				if(n == b.target()) {
					if((n.predecessors().size() + n.exceptionPredecessors().size()) == 1) {
						// L1: goto L2
						// L2: ...
						
						graph.removeBlock(b);
						m.instructions.remove(b.last());
						
						simple++;
					} else {
//						LabelNode targ = ((JumpInsnNode) b.last()).label;
						
						// redirect all predecessors to the real target
//						for(FlowBlock p : b.predecessors()) {
//							AbstractInsnNode last = null;
//							if(InstructionUtil.isUnconditional(((last = p.last())).opcode())) {
//								((JumpInsnNode) last).label = targ;
//							}
//						}
						
//						for(FlowBlock p : b.exceptionPredecessors()) {
//							AbstractInsnNode last = null;
//							if(InstructionUtil.isUnconditional(((last = p.last())).opcode())) {
//								((JumpInsnNode) last).label = targ;
//							}
//						}
						
//						graph.removeBlock(b);
//						m.instructions.remove(b.last());
//						
//						complex++;
					}
				}
			}
		}
		
		//if(simple > 0 || complex > 0) {
		//	 System.out.printf("%s %d simple, %d complex.%n", m, simple, complex);
		//}
	}
}