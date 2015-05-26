package org.nullbool.api.obfuscation.cfg;

import static org.nullbool.api.util.InstructionUtil.*;
import static org.objectweb.asm.Opcodes.LOOKUPSWITCH;
import static org.objectweb.asm.Opcodes.TABLESWITCH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.LabelHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;


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
public class ControlFlowGraph {

	private final List<FlowBlock> blocks;
	private final Map<AbstractInsnNode, FlowBlock> blockStarts;
	private final Map<LabelNode, FlowBlock> labels;

	public ControlFlowGraph() {
		blocks      = new ArrayList<FlowBlock>();
		blockStarts = new HashMap<AbstractInsnNode, FlowBlock>();
		labels      = new HashMap<LabelNode, FlowBlock>();
	}

	public void destroy() {
		blocks.clear();
		blockStarts.clear();
		labels.clear();
	}

	/**
	 * Finds and constructs the blocks of the cfg.
	 * @param method The MethodNode to graph.
	 * @throws ControlFlowException If there is a graphing error.
	 */
	public void create(MethodNode method) throws ControlFlowException {
		/* get rid of the old graph data.*/
		destroy();

		createBlocks(method);
		System.out.printf("Constructed %d flowblocks.%n", blocks.size());

//		for(String s : new InsnListPrinter(method.instructions).createPrint()) {
//			System.out.println(s);
//		}

		/* 25/05/15, 21:17 mapped blocks with their starting instruction. */
		mapPositions();
		calculateBranchTargets(method);
		
		associateBlocks();
		
		for(FlowBlock block : blocks) {
			System.out.println(block.toVerboseString(labels));
		}
		
//		method.instructions.clear();
//		
//		visit(blocks.get(0), method.instructions, true);
//		
//		for(AbstractInsnNode ain : method.instructions.toArray()) {
//			MarkerInsnNode marker = (MarkerInsnNode) ain;
//			FlowBlock block = marker.block;
//			
//			block.transfer(method.instructions, marker);
//			for(Entry<LabelNode, FlowBlock> e : labels.entrySet()) {
//				if(e.getValue().equals(block))
//					method.instructions.insertBefore(marker, e.getKey());
//			}
//			method.instructions.remove(marker);
//		}
//
//		for(String s : new InsnListPrinter(method.instructions).createPrint()) {
//			System.out.println(s);
//		}
//		for(FlowBlock block : blocks) {
//			System.out.println(block.toVerboseString(labels));
//		}
	}

	private void createBlocks(MethodNode method) {
		FlowBlock block = null;
		int b_count = 1;

		AbstractInsnNode[] ains = method.instructions.toArray();

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

	/* We need to resolve the LabelNodes for the method to account for the branching pattern
	 * that is created by the ASM library. Basically, when creating basic blocks, we can't 
	 * trust where ASM puts LabelNodes and rely on those since there are many reasons other
	 * than branching that the node could be there (such as line numbers and probably other
	 * things). So after we create the blocks based on what instructions we have, we need to
	 * go through the method and find each LabelNode and associate it with a block. Since the
	 * block creation is based on actual method structure and flow, the blocks should be
	 * aligned with the LabelNodes in the code. When we find a LabelNode, we first get the 
	 * instruction after it, which SHOULD (we should probably verify this) be the starting
	 * instruction of the block (since the LabelNodes are aligned (or so is seems)). We then
	 * take this instruction and find the block that is associated with it and then put the
	 * LabelNode and the FlowBlock into a map. This is done so that we can easily retrieve
	 * the target branch of a blocks jump (if it has one), ie. a GOTO, JSR, RET, IFNE, IFGT
	 * etc.
	 */
	private void calculateBranchTargets(MethodNode method) throws ControlFlowException {
		AbstractInsnNode[] ains = method.instructions.toArray();
		for(int i=0; i < ains.length; i++) {
			AbstractInsnNode ain = ains[i];

			if(ain instanceof LabelNode) {
				/* 25/05/15, 21:17: We now do this.
				 * 
				 * We could probably add each block and its starting instruction
				 * into a map and then retrieve it that way. This may make a 
				 * difference in the updater because of the ridiculous amount 
				 * of methods and blocks that might (probably will) be in the 
				 * gamepack. */

				/* the blocks (supposedly) first instruction. */
				AbstractInsnNode bStart = ain.getNext();
				FlowBlock block = blockStarts.get(bStart);
				
				/* if the lookup failed, manually look. */
				if(block == null) {
					for(FlowBlock b : blocks) {
						if(b.insns().contains(bStart)) {
							block = b;
							blockStarts.put(bStart, b);
						}
					}
				}
				
				//System.out.printf("%d + 1 = %s, %s.%n", i, bStart, block);
				
				if(block != null) {
					labels.put((LabelNode) ain, block);
				} else {
					throw new ControlFlowException(String.format("Couldn't find block for label at %d.", i));
				}
			}
		}
	}

	private void mapPositions() {
		for(FlowBlock b : blocks) {
			AbstractInsnNode bs = b.first();
			if(bs != null) {
				blockStarts.put(bs, b);
			}
		}
	}
	
	private void associateBlocks() throws ControlFlowException {
		FlowBlock previous = null;
		ListIterator<FlowBlock> it = blocks.listIterator();
		while(it.hasNext()) {
			FlowBlock block = it.next();
			
			if (previous != null) {
				previous.setNext(block);
				block.setPrev(previous);
			}
			
			FlowBlock target = findTarget(block);
			if (target != null) {
				block.setTarget(target);
				block.successors().add(target);
				target.predecessors().add(block);
			}
			
			AbstractInsnNode last = block.last();
			int fallThroughOpcode = last.opcode();
			
			/* Rather than calling isConditional() to check whether the 
			 * last instruction is part of a block which is predecessor
			 * to 2 blocks, we check whether it is not an exit(return
			 * or throw) and not an unconditional jump(goto, jsr, ret).
			 * 26/05/15, 13:43, we also need to make sure it's not a
			 * 					switch statement, see below.
			 * 
			 * This way, because we split void method calls into new 
			 * blocks, other instructions will count as fall throughs
			 * into the next block. (other instructions being either
			 * normal ones such as iload, invokevirtual etc. but also
			 * conditional jumps such as ifne, ifnonnull etc.) */
			if(!isUnconditional(fallThroughOpcode) && !isExit(fallThroughOpcode) && !isSwitch(fallThroughOpcode)) {
				/* If the last instruction of this block is
				 * conditional, it means that it will fall
				 * through into the next block. */
				if(it.hasNext()) {
					FlowBlock next = it.next();
					
					//if(isSwitch(fallThroughOpcode)) {
					//	System.out.println("ControlFlowGraph.associateBlocks()");
					//	System.out.println("next: " + next.id());
					//}
					
					/* Moves the iterators pointer backwards to
					 * undo the it.next() call.*/
					it.previous();
					block.successors().add(next);
					next.predecessors().add(block);
				}
			}
			
			/* 26/05/15, 13:35  We also need to check whether the instruction
			 * 					is a switch instruction to as they have more than 1 jump.
			 *					... checking to see if the fallthrough is different from 
			 *     				the switch's default block.
			 * 					... turns out that the default block is NOT the same as the
			 *     				fallthrough block, so I added the else here to check
			 *     				to make sure it's either a fallthrough or a default
			 *     				that is added. */
			
			//NOTO BENE: this else leads on from the if statement before.
			else if(fallThroughOpcode == TABLESWITCH) {
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) last;
				Set<LabelNode> lset = new HashSet<LabelNode>();
				lset.add(tsin.dflt);
				lset.addAll(tsin.labels);
				linkBlocks(block, lset);
			} else if(fallThroughOpcode == LOOKUPSWITCH) {
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) last;
				Set<LabelNode> lset = new HashSet<LabelNode>();
				lset.add(lsin.dflt);
				lset.addAll(lsin.labels);
				//System.out.println("ControlFlowGraph.123()");
				//System.out.println("def: " + labels.get(lsin.dflt).id());
				linkBlocks(block, lset);
			}
			
			previous = block;
		}
	}
	
	private void linkBlocks(FlowBlock block, Set<LabelNode> lset) throws ControlFlowException {
		for(LabelNode label : lset) {
			FlowBlock dest = labels.get(label);
			if(dest != null) {
				block.successors().add(dest);
				dest.predecessors().add(block);
			} else {
				throw new ControlFlowException(String.format("Couldn't find block for switch destination: %s", label));
			}
		}
	}
	
	/**
	 * Finds the block that is the target block of the input block. <br>
	 * Note that this method will return null if the input block does
	 * not end with a JumpInsnNode.
	 * @param b Input block
	 * @return The FlowBlock or null.
	 */
	public FlowBlock findTarget(FlowBlock b) {
		AbstractInsnNode last = b.last();
		if(last instanceof JumpInsnNode) {
			JumpInsnNode jin = (JumpInsnNode) last;
			LabelNode target = jin.label;
			return labels.get(target);
		} else {
			return null;
		}
	}
	
	/**
	 * Finds the block that either contains or is prepended
	 * by the specified LabelNode.
	 * @param label
	 * @return The FlowBlock or null
	 */
	public FlowBlock findTarget(LabelNode label) {
		return labels.get(label);
	}
	
	private void visit(FlowBlock block, InsnList insns, boolean b) {
		block.setVisited(true);
		
		if(b)
			insns.add(new MarkerInsnNode(block));


		for(FlowBlock succ : block.successors()) {
			if(!succ.visited()) {
				insns.add(new MarkerInsnNode(succ));
				visit(succ, insns, false);
			}
		}
	}

	private boolean isBoundary(AbstractInsnNode ain) {
		if(ain == null)
			return false;
		
		/*if(ain instanceof MethodInsnNode) {
			if(((MethodInsnNode) ain).desc.endsWith("V"))
				return true;
			else
				return false;
		}*/
		
		int opcode = ain.opcode();
		return isConditional(opcode) || isUnconditional(opcode) || isExit(opcode) || isSwitch(opcode);
	}

	public static void main(String[] args) throws ControlFlowException, IOException {
		
		ControlFlowGraph graph = new ControlFlowGraph();

		ClassStructure cs = ClassStructure.create(SwitchTest.class.getCanonicalName());
		for(MethodNode m : cs.methods) {
			if(m.name.equals("test")) {
				graph.create(m);
			}
		}
		
//		ControlFlowGraph graph = new ControlFlowGraph();
//
//		ClassStructure cs = ClassStructure.create(Test.class.getCanonicalName());
//		for(MethodNode m : cs.methods) {
//			if(m.name.equals("test")) {
//				graph.create(m);
//			}
//		}
//
//		graph.destroy();
//
//		for(int i=0; i < 5; i++) {
//			System.out.println();
//		}
//		
//		cs = ClassStructure.create(new File("res/Buffer.class").toURI().toURL().openStream());
//
//		
//		for(MethodNode m : cs.methods) {
//			if(m.name.equals("writeInvertedLEInt")) {
//				graph.create(m);
//			}
//		}
	}
}