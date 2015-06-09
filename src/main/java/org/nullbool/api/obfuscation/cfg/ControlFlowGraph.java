package org.nullbool.api.obfuscation.cfg;

import static org.nullbool.api.util.InstructionUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.InsnListPrinter;
import org.nullbool.api.util.LabelHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;


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
public class ControlFlowGraph implements Opcodes, Iterable<FlowBlock> {
	
	private static int graphCount = 0;
	
	public boolean debug = false;
	
	private final List<FlowBlock> blocks;
	private final Map<AbstractInsnNode, FlowBlock> blockStarts;
	private final Map<LabelNode, FlowBlock> labels;
	private final Map<String, FlowBlock> blockNames;
	private final List<ExceptionData> exceptions;
	
	private FlowBlock entry, exit;
	private boolean loop;

	public ControlFlowGraph() {
		blocks      = new ArrayList<FlowBlock>();
		blockStarts = new HashMap<AbstractInsnNode, FlowBlock>();
		labels      = new HashMap<LabelNode, FlowBlock>();
		blockNames  = new HashMap<String, FlowBlock>();
		exceptions  = new ArrayList<ExceptionData>();
	}

	public void destroy() {
		blocks.clear();
		blockStarts.clear();
		labels.clear();
		blockNames.clear();
		exceptions.clear();
	}

	/**
	 * Finds and constructs the blocks of the cfg.
	 * @param method The MethodNode to graph.
	 * @throws ControlFlowException If there is a graphing error.
	 * @return this
	 */
	public ControlFlowGraph create(MethodNode method) throws ControlFlowException {
		///* get rid of the old graph data.*/
		//destroy();

		if(debug)
			System.out.printf("Building graph %d.%n", ++graphCount);
		
		if(debug) 
			System.out.println("createBlocks");
		
		createBlocks(method);
		if(debug)
			System.out.printf("Constructed %d flowblocks.%n", blocks.size());

		if(debug) 
			System.out.println("mapBlockNames");
		mapBlockNames();
		
		if(debug) 
			System.out.println("mapPositions");
		/* 25/05/15, 21:17 mapped blocks with their starting instruction. */
		mapPositions();
		
		if(debug) 
			System.out.println("calculateBranchTargets");
		calculateBranchTargets(method);
		
		if(debug) 
			System.out.println("associateBlocks");
		associateBlocks();
		
		if(debug) 
			System.out.println("associateExceptions");
		associateExceptions(method);
		
		if(debug) 
			System.out.println("captureEntryAndExit");
		captureEntryAndExit();
		
		if(method.key().equals("")) {
			debug = true;
		}
		
		if(debug) 
			System.out.println(this);
		
		loop = cycles();
		
		return this;
	}
	
	public void fix() throws ControlFlowException {
		if(debug) 
			System.out.println("removeDeadBlocks");
		removeDeadBlocks();
		
		if(debug) 
			System.out.println("removeGotos");
		removeGotos();
		
		if(debug) 
			System.out.println("removeEmptyBlocks");
		removeEmptyBlocks();
		
		if(debug) 
			System.out.println("mergeBlocks");
		mergeBlocks();
	}
	
	public void result(MethodNode method) throws ControlFlowException {
		//TODO: FIX,probably to do with this
		List<AbstractInsnNode> insns = new ArrayList<AbstractInsnNode>();
		
		for(FlowBlock b : blocks) {
			b.transfer(insns);
		}
		
		InsnList list = method.instructions;
		list.removeAll(true);
		
		for(int i=0; i < insns.size(); i++) {
			AbstractInsnNode ain = insns.get(i);
			
			if(debug) {
				if(method.instructions.contains(ain)) {
					System.out.println("ControlFlowGraph.result() " + method + " " + ain + " " + i);
				}
			}
			
			method.instructions.add(ain);
		}
		
		
		if(debug) {
			ControlFlowGraph graph = new ControlFlowGraph();
			graph.create(method);
			for(String s : new InsnListPrinter(method.instructions).createPrint()) {
				System.out.println(s);
			}
		}
	}
	
	private void createBlocks(MethodNode method) {
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
	
	public static boolean isBlockSplit(AbstractInsnNode ain) {
		return isBoundary(ain) || (ain instanceof MethodInsnNode && ((MethodInsnNode) ain).desc.endsWith("V") && ain.getNext() instanceof LabelNode /* && isBoundary(ain.getNext())*/);
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
				if(bStart == null) {
					System.err.println("TardASM put a LabelNode before a null insn (end).");
					continue;
				}
				
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

	private void mapBlockNames() {
		for(FlowBlock b : blocks) {
			blockNames.put(b.id(), b);
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
				block.addSuccessor(target);
				target.addPredecessor(block);
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
					block.addSuccessor(next);
					next.addPredecessor(block);
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
				block.addSuccessor(dest);
				dest.addPredecessor(block);
			} else {
				throw new ControlFlowException(String.format("Couldn't find block for switch destination: %s", label));
			}
		}
	}
	
	private void associateExceptions(MethodNode method) throws ControlFlowException {
		Map<String, ExceptionData> mapRanges = new HashMap<String, ExceptionData>();
		
		for(TryCatchBlockNode tcbn : method.tryCatchBlocks) {
			FlowBlock dest = labels.get(tcbn.end);
			FlowBlock from = labels.get(tcbn.start);
			FlowBlock handler = labels.get(tcbn.handler);
			
			if(dest == null || from == null || handler == null)
				throw new ControlFlowException(String.format("Couldn't find block for TryCatch label."));
			
			int i_to   = LabelHelper.numeric(dest.id());
			int i_from = LabelHelper.numeric(from.id());
			
			String key = from.id() + ":" + dest.id() + ":" + handler.id();
			
			if (mapRanges.containsKey(key)) {
				ExceptionData range = mapRanges.get(key);
				range.types().add(tcbn.type);
			} else {
				List<FlowBlock> range = new ArrayList<FlowBlock>();
				int i = i_from;
				/* 1/06/15, 19:33, changed this from a for loop to a do while as
				 * there are instances where i_from and i_to are the same and so
				 * the for loop does not execute properly. This meant that if there
				 * was 1 trycatch substructure in the method, that lasted for only
				 * 1 block, it would not be included, producing errorneous results
				 * later on.*/
				do {
					String id = LabelHelper.createBlockName(i);
					FlowBlock block = blockNames.get(id);
					if(block == null) 
						throw new ControlFlowException(String.format("Block #%s (%d) is not mapped.", id, i));
					range.add(block);
					handler.addExceptionPredecessor(block);
					block.addExceptionSuccessor(handler);
					
					ExceptionData ed = new ExceptionData(handler, range, Arrays.asList(tcbn.type));
					mapRanges.put(key, ed);
					exceptions.add(ed);
					
					i++;
				} while(i < i_to);
			}
		}
	}
	
	private void captureEntryAndExit() {
		entry = blocks.get(0);
		
		/* Here we create a fake exit node so that every node that was previously graphed
		 * leads into this one (even though the node isn't really present in the code). 
		 * This allows us to have a definite exit block for the method rather than the
		 * multiple possible ones there could be in natural code. 
		 * Note that we probably shouldn't save this in the blocks list as it is not
		 * a real block. */
		FlowBlock dummyExit = new DummyExitBlock(LabelHelper.createBlockName(blocks.size() + 1));
		
		for (FlowBlock b : blocks) {
			if (b.successors().isEmpty()) {
				b.addSuccessor(dummyExit);
				dummyExit.addPredecessor(b);
			}
		}
		
		exit = dummyExit;
	}
	
	private void removeGotos() throws ControlFlowException {		
		for (FlowBlock block : blocks) {
			AbstractInsnNode last = block.last();
			if (last != null && last.opcode() == GOTO) {
				block.insns().remove(last);
			}
		}
	}
	
	private void removeEmptyBlocks() throws ControlFlowException {
		boolean cont;
		do {
			cont = false;
			for (int i = blocks.size() - 1; i >= 0; i--) {
				FlowBlock block = blocks.get(i);
				if (removeEmptyBlock(block, false)) {
					cont = true;
					break;
				}
			}
		}
		while (cont);
	}
	
	public boolean removeEmptyBlock(FlowBlock block, boolean merging) throws ControlFlowException {
		boolean deletedRanges = false;
		if (block.cleansize() == 0) {
			if (block.successors().size() > 1) {
				if (block.predecessors().size() > 1) {
					throw new ControlFlowException(String.format("Empty block (%s) with multiple predecessors and successors.", block.id()));
				} else if (!merging) {
					throw new ControlFlowException(String.format("Empty block (%s) with multiple successors found.", block.id()));
				}
			}

			Set<FlowBlock> setExits = new HashSet<FlowBlock>(exit.predecessors());
			if (block.exceptionPredecessors().isEmpty() && (!setExits.contains(block) || block.predecessors().size() == 1)) {
				if (setExits.contains(block)) {
					FlowBlock pred = block.predecessors().get(0);
					if (pred.successors().size() != 1 || (pred.cleansize() != 0 && isSwitch(pred.last().opcode()))) {
						return false;
					}
				}

				Set<FlowBlock> preds = new HashSet<FlowBlock>(block.predecessors());
				Set<FlowBlock> succs = new HashSet<FlowBlock>(block.successors());

				/* Collate the common exception rangers of the 
				 * predecessors and successors of the block. */
				Set<FlowBlock> commonHandlers = null;
				for (int i = 0; i < 2; ++i) {
					for (FlowBlock pred : i == 0 ? preds : succs) {
						if (commonHandlers == null) {
							commonHandlers = new HashSet<FlowBlock>(pred.exceptionSuccessors());
						} else {
							commonHandlers.retainAll(pred.exceptionSuccessors());
						}
					}
				}
				if (commonHandlers != null && !commonHandlers.isEmpty()) {
					for (FlowBlock handler : commonHandlers) {
						if (!block.exceptionSuccessors().contains(handler)) {
							return false;
						}
					}
				}
				for (int i = exceptions.size() - 1; i >= 0; i--) {
					ExceptionData range = exceptions.get(i);
					List<FlowBlock> lst = range.range();
					if (lst.size() == 1 && lst.get(0) == block) {
						FlowBlock handler = range.handler();
						block.removeExceptionSuccessor(handler);
						handler.removeExceptionSuccessor(handler);
						exceptions.remove(i);
					}
				}
				if (merging) {
					FlowBlock pred = block.predecessors().get(0);
					pred.removeSuccessor(block);
					block.removePredecessor(pred);

					List<FlowBlock> lstSuccs = new ArrayList<FlowBlock>(block.successors());
					for (FlowBlock succ : lstSuccs) {
						block.removeSuccessor(succ);
						succ.removePredecessor(block);

						pred.addSuccessor(succ);
						succ.addPredecessor(pred);
					}
				} else {
					for (FlowBlock pred : preds) {
						for (FlowBlock succ : succs) {
							pred.replaceSuccessor(block, succ);
						}
					}
				}

				if (entry == block) {
					if (succs.size() != 1) {
						throw new ControlFlowException(String.format("Invalid number of entry blocks (%d).", succs.size()));
					} else {
						entry = succs.iterator().next();
					}
				}
				removeBlock(block);
				if (deletedRanges) {
					removeDeadBlocks();
				}
			}
		}

		return deletedRanges;
	}
	
	public void removeBlock(FlowBlock block) {
		
		if(debug) {
			System.out.println("Removing " + block.id());
			System.out.println("t1");
		}
		
		while (block.successors().size() > 0) {
			FlowBlock b2 = block.successors().get(0);
			block.removeSuccessor(b2);
			b2.removePredecessor(block);
		}

		if(debug) {
			System.out.println("t2");
		}
		
		while (block.exceptionSuccessors().size() > 0) {
			FlowBlock b2 = block.exceptionSuccessors().get(0);
			block.removeExceptionSuccessor(b2);
			b2.removeExceptionPredecessor(block);
		}
		
		if(debug) {
			System.out.println("t3");
			
			System.out.printf("block has %d and %d.%n", block.predecessors().size(), block.successors().size());
		}

		while (block.predecessors().size() > 0) {
			FlowBlock b2 = block.predecessors().get(0);
			b2.removeSuccessor(block);
			block.removePredecessor(b2);
		}

		if(debug) {
			System.out.println("t4");
		}
		
		while (block.exceptionPredecessors().size() > 0) {
			FlowBlock b2 = block.exceptionPredecessors().get(0);
			block.removeExceptionSuccessor(b2);
			b2.removeExceptionPredecessor(block);
		}
		
		if(debug) {
			System.out.println("t5");
		}

		exit.removePredecessor(block);
		blocks.remove(block);
		
		if(debug) {
			System.out.println("t6");
		}

		for (int i = exceptions.size() - 1; i >= 0; i--) {
			ExceptionData range = exceptions.get(i);
			if (range.handler() == block) {
				exceptions.remove(i);
			} else {
				List<FlowBlock> lstRange = range.range();
				lstRange.remove(block);

				if (lstRange.isEmpty()) {
					exceptions.remove(i);
				}
			}
		}

		/* Jagex's obfuscator doesn't create subroutines (thank god).*/
	}
	
	private void removeDeadBlocks() {
		LinkedList<FlowBlock> stack = new LinkedList<FlowBlock>();
		Set<FlowBlock> set = new HashSet<FlowBlock>();

		stack.add(entry);
		set.add(entry);

		while (!stack.isEmpty()) {
			FlowBlock block = stack.removeFirst();

			List<FlowBlock> lstSuccs = new ArrayList<FlowBlock>(block.successors());
			lstSuccs.addAll(block.exceptionSuccessors());

			for (FlowBlock succ : lstSuccs) {
				if (!set.contains(succ)) {
					stack.add(succ);
					set.add(succ);
				}
			}
		}

		Set<FlowBlock> setAllBlocks = new HashSet<FlowBlock>(blocks);
		setAllBlocks.removeAll(set);

		for (FlowBlock block : setAllBlocks) {
			
			if(debug) {
				System.out.println("ControlFlowGraph.removeDeadBlocks()");
			}
			removeBlock(block);
		}
		
		if(debug) {
			System.err.println("end");
		}
	}

	private void mergeBlocks() throws ControlFlowException {
		while (true) {
			boolean merged = false;
			for (FlowBlock block : blocks) {
				if (block.successors().size() == 1) {
					FlowBlock next = block.successors().get(0);
					if (next != exit && (block.insns().isEmpty() || !isSwitch(block.last().opcode()))) {
						if (next.predecessors().size() == 1 && next.exceptionPredecessors().isEmpty() && next != entry) {
							boolean sameRanges = true;
							for (ExceptionData range : exceptions) {
								if (range.range().contains(block) ^ range.range().contains(next)) {
									sameRanges = false;
									break;
								}
							}
							if (sameRanges) {
								block.insns().addAll(next.insns());
								next.insns().clear();
								removeEmptyBlock(next, true);
								merged = true;
								break;
							}
						}
					}
				}
			}

			if (!merged) {
				break;
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

	private static boolean isBoundary(AbstractInsnNode ain) {
		if(ain == null)
			return false;
		
		int opcode = ain.opcode();
		return isConditional(opcode) || isUnconditional(opcode) || isExit(opcode) || isSwitch(opcode);
	}

	public StringBuilder toString(List<FlowBlock> blocks) {
		StringBuilder sb = new StringBuilder();
		for(FlowBlock b : blocks) {
			sb.append(b.toVerboseString(labels)).append(System.lineSeparator());
		}
		return sb;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = toString(blocks);
		sb.append(System.lineSeparator());
		sb.append("Exit> ");
		sb.append(exit.toVerboseString(labels));
		return sb.toString();
	}
	
	public static void main1(String[] args) throws ControlFlowException, IOException {
		
		ControlFlowGraph graph = new ControlFlowGraph();

//		ClassStructure cs = ClassStructure.create(SwitchTest.class.getCanonicalName());
//		for(MethodNode m : cs.methods) {
//			if(m.name.equals("test")) {
//				graph.create(m);
//			}
//		}
		
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
//		ClassStructure cs = ClassStructure.create(new File("res/Buffer.class").toURI().toURL().openStream());
		ClassStructure cs = ClassStructure.create(ControlFlowGraph.class.getResourceAsStream("/Buffer.class"));
		
		for(MethodNode m : cs.methods) {
			if(m.name.equals("writeInvertedLEInt")) {
				graph.create(m);
			}
		}
	}

	public List<FlowBlock> blocks() {
		return blocks;
	}
	
	public FlowBlock entry() {
		return entry;
	}
	
	public FlowBlock exit() {
		return exit;
	}

	public ExceptionData getExceptionRange(FlowBlock handler, FlowBlock block) {
		for (int i = exceptions.size() - 1; i >= 0; i--) {
			ExceptionData ed = exceptions.get(i);
			if (ed.handler().equals(handler) && ed.range().contains(block)) {
				return ed;
			}
		}

		return null;
	}
	
	public List<ExceptionData> exceptions() {
		return exceptions;
	}

	@Override
	public DFSIterator iterator() {
		return new DFSIterator(entry);
	}
	
	public boolean hasLoop() {
		return loop;
	}
	
	public boolean cycles() {
		List<FlowBlock> dfs = new ArrayList<FlowBlock>();
		Set<FlowBlock> visited = new HashSet<FlowBlock>();
		Stack<FlowBlock> stack = new Stack<FlowBlock>();
		stack.push(entry);
		while(!stack.isEmpty()) {
			FlowBlock v = stack.pop();
			if(!visited.contains(v)) {
				visited.add(v);
				dfs.add(v);
				
				for(FlowBlock b : v.successors()) {
					if(visited.contains(b)) {
						return true;
					} else {
						stack.push(b);
					}
				}
				
				for(FlowBlock b : v.exceptionSuccessors()) {
					if(visited.contains(b)) {
						return true;
					} else {
						stack.push(b);
					}
				}
			}
		}
		
		return false;
	}
}