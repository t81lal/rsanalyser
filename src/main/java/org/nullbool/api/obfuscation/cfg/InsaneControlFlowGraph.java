package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.LabelHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class InsaneControlFlowGraph extends AbstractControlFlowGraph {

	@Override
	public void createBlocks(MethodNode method) {
		List<FlowBlock> blocks = blocks();
		if(!blocks.isEmpty())
			throw new IllegalStateException();
		
		AbstractInsnNode[] arr = method.instructions.toArray();
		
		int b_count = 1;
		FlowBlock block = null;
		
		for(int i=0; i < arr.length; i++) {
			AbstractInsnNode ain = arr[i];
			
			if(block == null) {
				block = new FlowBlock(LabelHelper.createBlockName(b_count++));
			}
			
			if(ain instanceof LabelNode) {
				// start of a new block
				if(block.size() > 0) {
					blocks.add(block);
					
					FlowBlock next = new FlowBlock(LabelHelper.createBlockName(b_count++));
					next.insns().add(ain);
					block = next;
				}
			} else {
				block.insns().add(ain);
			}
		}
		
		if(block != null && block.size() > 0) {
			blocks.add(block);
		}
	}
	
	@Override
	public void associateBlocks() throws ControlFlowException {
		FlowBlock previous = null;
		ListIterator<FlowBlock> it = blocks().listIterator();
		while(it.hasNext()) {
			FlowBlock block = it.next();
			
			if (previous != null) {
				previous.setNext(block);
				block.setPrev(previous);
			}
			
			for(AbstractInsnNode ain : block.insns()) {
				if(ain instanceof JumpInsnNode) {
					FlowBlock target = findTarget(((JumpInsnNode) ain).label);
					linkBlocks(target, block, ain.equals(block.last()));
				} else if(ain instanceof TableSwitchInsnNode) {
					Set<LabelNode> targets = InstructionUtil.calcSwitchTargets((TableSwitchInsnNode)ain, new HashSet<LabelNode>());
					super.linkBlocks(block, targets);
				} else if(ain instanceof LookupSwitchInsnNode) {
					Set<LabelNode> targets = InstructionUtil.calcSwitchTargets((LookupSwitchInsnNode)ain, new HashSet<LabelNode>());
					super.linkBlocks(block, targets);
				} else if(InstructionUtil.isExit(ain.opcode()) && ain.equals(block.last())) {
					
				} else {
					if(it.hasNext()) {
						FlowBlock next = it.next();
						it.previous();
						block.addSuccessor(next);
						next.addPredecessor(block);
					}
				}
			}
			
			previous = block;
		}
	}
	
	// TODO: Validate
	@Override
	public void associateExceptions(MethodNode method) throws ControlFlowException {
		Map<String, ExceptionData> mapRanges = new HashMap<String, ExceptionData>();
		
		Map<LabelNode, FlowBlock> labels = labels();
		Map<String, FlowBlock> blockNames = blockNames();
		List<ExceptionData> exceptions = exceptions();
		
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
					i++;
				} while(i < i_to);
				
				List<String> types = new ArrayList<String>();
				types.add(tcbn.type);
				ExceptionData ed = new ExceptionData(handler, range, types);
				mapRanges.put(key, ed);
				exceptions.add(ed);
			}
		}
	}
	
	public void linkBlocks(FlowBlock target, FlowBlock block, boolean next) {
		if (target != null) {
			if(next) {
				block.setTarget(target);
			}
			block.addSuccessor(target);
			target.addPredecessor(block);
		}
	}
}