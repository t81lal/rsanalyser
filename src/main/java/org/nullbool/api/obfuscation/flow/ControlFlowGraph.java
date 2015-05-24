package org.nullbool.api.obfuscation.flow;

import static org.nullbool.api.util.LabelHelper.createBlockName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
public class ControlFlowGraph implements Opcodes {

	private final List<FlowBlock> blocks;
	private final Map<LabelNode, String> labels;
	private final Map<String, LabelNode> reverseLabels;
	private final List<LabelNode> handlerLabels;
	private final Map<LabelNode, FlowBlock> labelMap;
	private final List<FlowBlock> emptyBlocks;
	
	private MethodNode method;
	
	public ControlFlowGraph() {
		blocks        = new ArrayList<FlowBlock>();
		labels        = new HashMap<LabelNode, String>();
		reverseLabels = new HashMap<String, LabelNode>();
		handlerLabels = new ArrayList<LabelNode>();
		labelMap      = new HashMap<LabelNode, FlowBlock>();
		emptyBlocks   = new ArrayList<FlowBlock>();
	}
	
	public void destroy() {
		blocks.clear();
		labels.clear();
		reverseLabels.clear();
		handlerLabels.clear();
		labelMap.clear();
	}
	
	public void create(MethodNode method) {
		this.method = method;
		
		captureHandlerBlocks();
		constructBlocks();
		mapBlockLabels();
		
		System.out.printf("Structured %d flowblocks.%n", blocks.size());
		
		/* now we have to go through the constructed blocks and add predecessors and
		 * successors to each block.
		 * 
		 * rules:
		 *  if the block ends with an unconditional jump (goto), the next block IS NOT
		 *  an immediate successor to this block and the block is not a predecessor to
		 *  this one.
		 *  
		 *  if the block ends with a conditional jump, then the next block and the 
		 *  target block are successors and those blocks have this block as a 
		 *  predecessors.
		 *  
		 *  if the block ends with a return
		 *  
		 *  if the block ends with a throw, the handler block is the successor to it and
		 *  the handler block is a predecessors.
		 * */
		
		collateEmptyBlocks();
		
		System.out.printf("%d empty blocks.%n", emptyBlocks.size());
		blocks.removeAll(emptyBlocks);
		
		graphExceptions();
		graphTogether();
		
		merge();
		
		System.out.println(this);
		
	}
	
	private void captureHandlerBlocks() {
		for(TryCatchBlockNode block : method.tryCatchBlocks) {
			handlerLabels.add(block.handler);
		}
	}
	
	private void constructBlocks() {
		FlowBlock currentBlock = null;
		int b_count = 0;
		
		AbstractInsnNode[] ains = method.instructions.toArray();
		for(int i=0; i < ains.length; i++) {
			AbstractInsnNode ain = ains[i];
			if(ain instanceof LabelNode) {
				if(currentBlock != null) {
					blocks.add(currentBlock);
				}
				
				String name  = createBlockName(++b_count);
				LabelNode label = (LabelNode) ain;
				currentBlock = new FlowBlock((currentBlock == null || handlerLabels.contains(label)) ? false : true, name, method, label, labels);
				labels.put(label, name);
				reverseLabels.put(name, label);
			} else if(ain.opcode() != -1) {
				currentBlock.insns().add(ain);
			}
		}
		
		/* add the last block since it wont be added until we meet a LabelNode and we can
		 * only meet a LabelNode at the start of the block (and the start of a block
		 * won't be at the end of the code.*/
		if(currentBlock != null) {
			blocks.add(currentBlock);
		}
	}
	
	private void collateEmptyBlocks() {
		for (FlowBlock block : blocks) {
			if (block.type() == BlockType.EMPTY)
				emptyBlocks.add(block);
		}
	}
	
	private void mapBlockLabels() {
		for(FlowBlock block : blocks) {
			labelMap.put(block.label(), block);
		}
	}
	
	private void graphExceptions() {
		/*for(TryCatchBlockNode block : method.tryCatchBlocks) {
		FlowBlock start = labelMap.get(block.start);
		FlowBlock end   = labelMap.get(block.end);
		System.out.println("end " + block.end);
		FlowBlock handl = labelMap.get(block.handler); 
		System.out.println(start.id());
		System.out.println(end.id());
		System.out.println(handl.id());
		for(int i=numeric(start.id()); i < numeric(end.id()); i++) {
			String name = createBlockName(i);
			FlowBlock b = labelMap.get(reverseLabels.get(name));

			handl.predeccessors().add(b);
			b.successors().add(handl);
		}

		System.out.println("handler: " + labels.get(block.handler));
		System.out.println("start: " + labels.get(block.start));
		System.out.println("end: " + labels.get(block.end));
		}*/
	}
	
	private void graphTogether() {
		for(FlowBlock block : blocks) {
			AbstractInsnNode last = block.last();
			switch(last.opcode()) {
				case GOTO: {
					JumpInsnNode jin = (JumpInsnNode) last;
					LabelNode label  = jin.label;
					FlowBlock target = labelMap.get(label);
					block.successors().add(target);
					target.predeccessors().add(block);
					continue;
				}
				
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
				case ARETURN:
				case RETURN:
					
					continue;
			}
		}
	}
	
	private void merge() {
		/* we can merge a block if it is movable and has 1 predecessor*/
		
		/*InsnList newInsns = new InsnList();
		for(int i=graph.blocks.size(); i >= 1; i--) {
			FlowBlock block = graph.blocks.get(i - 1);
			if(block.isMovable()) {
				if(block.successors().size() == 1) {
					
				}
			} else {
				newInsns.add(block.label());
				for(AbstractInsnNode ain : block.insns()) {
					newInsns.add(ain);
				}
			}
		}*/
		
		FlowBlock previous = null;
		for (FlowBlock block : blocks) {
			if (previous != null) {
				previous.setNext(block);
				block.setPrevious(previous);
			}
			FlowBlock target = targetOf(block);
			if (target != null) {
				block.setTarget(target);
				block.addSuccessor(target);
				target.addPredecessor(block);
			}
			previous = block;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		ListIterator<FlowBlock> it = blocks.listIterator();
		while(it.hasNext()) {
			FlowBlock block = it.next();
			sb.append(block.toString());
			
			if(it.hasNext())
				sb.append(System.lineSeparator());
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}
}