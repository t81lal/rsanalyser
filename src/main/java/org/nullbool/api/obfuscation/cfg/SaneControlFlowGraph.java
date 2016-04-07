package org.nullbool.api.obfuscation.cfg;

import java.util.List;

import org.nullbool.api.util.LabelHelper;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.LabelNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * A 'graph' or Collection of FlowBlocks constructed from the code of a MethodNode
 * which can be used to find the relationship between different blocks and the 
 * state and role of each block of instructions in the method.
 * <p>
 * A call can be made to {@link #create(MethodNode)} to begin analysing the methods
 * code to turn it into a List of FlowBlocks. Blocks in the bytecode are constructed
 * based on the execution path of the code. The most common approach is to split code 
 * depending on where the programs instruction pointer is by use of instructions
 * such as the conditional (IFNE, IFGE, IFLT...) and unconditional branching 
 * instructions (GOTO, JSR, RET) . Blocks can also be split by a void method call which
 * is followed by an ASM LabelNode and also instructions that end a 
 * method (ATHROW, RETURN, IRETURN...) as well as the switch instructions (LOOKUPSWITCH
 * and TABLESWITCH).
 * <p>
 * Blocks can also be retrieved by a reference to a LabelNode that was previously in the 
 * code. This is either cached or looked up and so can produce erroneous results.
 * 
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015
 */
public class SaneControlFlowGraph extends AbstractControlFlowGraph {

	public void createBlocks(MethodNode method) {
		List<FlowBlock> blocks = blocks();
		if(!blocks.isEmpty())
			throw new IllegalStateException();
		
		FlowBlock block = null;
		int b_count = 1;

		AbstractInsnNode[] ains = method.instructions.toArray();

		if(debug)
			System.out.printf("Method %s contains %d instructions.%n", method.key(), method.instructions.size());

		for(int i=0; i < ains.length; i++) {
			AbstractInsnNode ain = ains[i];
			/* ignore meta instructions. 
			 * 
			 * 25/05/15, 21:43 only ignore LabelNodes rather than other meta
			 * instructions (such as LineNumberNodes) so that we can target
			 * blocks more easily. Previously, all instructions with an opcode
			 * of -1 were ignored, which caused an error (specifically by ZKM/
			 * the Oldschool obfuscator) which meant that because we didn't
			 * include other meta nodes, we couldn't find the start instruction
			 * correctly. 
			 * 
			 * 26/05/15, 14:02  now we don't care if we add a meta instruction
			 * 					to the blocks instructions as we want to save
			 * 					all the instructions original data.*/
			
			 //if(ain.opcode() == -1) 
			 //continue;
			 //if(ain instanceof LabelNode)
			 //	continue;

			if(block == null) {
				block = new FlowBlock(LabelHelper.createBlockName(b_count++));
			}

			block.insns().add(ain);

			/* 26/05/15, 13:54 Before we were splitting blocks on a void method call
			 *                 that was succeeded by a boundary instruction,
			 *                 this, however, is wrong (the boundary part).
			 *                 #see SwitchTest. 
			 * 
			 * 26/05/15, 14:06  We might be able to get away with checking if there is
			 * 					LabelNode after the method call as ASM will have inserted 
			 * 					these when it attempted to mark the code.
			 * 					The only case I saw a problem with this was in Buffer.writeInvertedLEInt
			 * 					in the Oldschool r78 gamepack where an exception block was split up
			 * 					by a void call (invokespecial construction call) which split the block.
			 */
			if(isBoundary(ain) || (ain instanceof MethodInsnNode && ((MethodInsnNode) ain).desc.endsWith("V") && ain.getNext() instanceof LabelNode /* && isBoundary(ain.getNext())*/)) {
				blocks.add(block);
				block = new FlowBlock(LabelHelper.createBlockName(b_count++));
			}
		}

		if(block != null && block.size() > 0) {
			blocks.add(block);
		}
	}
}