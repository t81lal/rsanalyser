package org.nullbool.api.obfuscation.cfg;

import static org.objectweb.custom_asm.commons.util.Assembly.OPCODES;
import static org.objectweb.custom_asm.commons.util.Assembly.pad;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.FIELD_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.IINC_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.INT_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.JUMP_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.LDC_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.METHOD_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.TABLESWITCH_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.TYPE_INSN;
import static org.objectweb.custom_asm.tree.AbstractInsnNode.VAR_INSN;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.nullbool.api.util.InstructionUtil;
import org.objectweb.custom_asm.commons.util.Assembly;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.IincInsnNode;
import org.objectweb.custom_asm.tree.InsnList;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.JumpInsnNode;
import org.objectweb.custom_asm.tree.LabelNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.LookupSwitchInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MultiANewArrayInsnNode;
import org.objectweb.custom_asm.tree.TableSwitchInsnNode;
import org.objectweb.custom_asm.tree.TypeInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015
 */
public class FlowBlock {

	private static final String PADDING = "=====";
	private static final String PREFIX  = System.lineSeparator() + "   ";
	
	private final String id;
	private final List<AbstractInsnNode> insns;
	private final List<FlowBlock> predecessors;
	private final List<FlowBlock> successors;
	private final List<FlowBlock> excPredecessors;
	private final List<FlowBlock> excSuccessors;
	
	private FlowBlock next, prev, target;
	private boolean visited;

	public FlowBlock(String id) {
		this.id         = id;
		insns           = new ArrayList<AbstractInsnNode>();
		predecessors    = new ArrayList<FlowBlock>();
		successors      = new ArrayList<FlowBlock>();
		excPredecessors = new ArrayList<FlowBlock>();
		excSuccessors   = new ArrayList<FlowBlock>();
	}
	
	public boolean branches() {
		return InstructionUtil.isConditional(lastOpcode()) || InstructionUtil.isSwitch(lastOpcode());
	}
	
	public String id() {
		return id;
	}
	
	public List<AbstractInsnNode> insns() {
		return insns;
	}
	
	public int size() {
		return insns.size();
	}
	
	public int cleansize() {
		int i = 0;
		ListIterator<AbstractInsnNode> it = insns.listIterator();
		while(it.hasNext()) {
			AbstractInsnNode ain = it.next();
			if(ain.getOpcode() != -1)
				i++;
		}
		return i;
	}
	
	public AbstractInsnNode first() {
		if(insns.size() > 0)
			return insns.get(0);
		return null;
	}
	
	public AbstractInsnNode last() {
		if(insns.size() > 0)
			return insns.get(insns.size() - 1);
		return null;
	}
	
	public int lastOpcode() {
		AbstractInsnNode last = last();
		if(last == null)
			return -1;
		return last.getOpcode();
	}
	
	public void removeLast() {
		AbstractInsnNode last = last();
		if(last != null) {
			insns.remove(last);
		}
	}
	
	public void addPredecessor(FlowBlock block) {
		if(!predecessors.contains(block)) {
			predecessors.add(block);
		}
	}
	
	public void removePredecessor(FlowBlock block) {
		while (predecessors.remove(block));
	}
	
	public List<FlowBlock> predecessors() {
		return predecessors;
	}
	
	public void addSuccessor(FlowBlock block) {
		if(!successors.contains(block)) {
			successors.add(block);
		}
	}
	
	public void removeSuccessor(FlowBlock block) {
		while (successors.remove(block));
	}
	
	public List<FlowBlock> successors() {
		return successors;
	}
	
	public void addExceptionPredecessor(FlowBlock block) {
		if(!excPredecessors.contains(block)) {
			excPredecessors.add(block);
		}
	}
	
	public void removeExceptionPredecessor(FlowBlock block) {
		while (excPredecessors.remove(block));
	}

	public List<FlowBlock> exceptionPredecessors() {
		return excPredecessors;
	}
	
	public void addExceptionSuccessor(FlowBlock block) {
		if(!excSuccessors.contains(block)) {
			excSuccessors.add(block);
		}
	}
	
	public void removeExceptionSuccessor(FlowBlock block) {
		while (excSuccessors.remove(block));
	}
	
	public List<FlowBlock> exceptionSuccessors() {
		return excSuccessors;
	}
	
	public void replaceSuccessor(FlowBlock oldBlock, FlowBlock newBlock) {
		for (int i = 0; i < successors.size(); i++) {
			if (successors.get(i).id == oldBlock.id) {
				successors.set(i, newBlock);
				oldBlock.removePredecessor(this);
				newBlock.addPredecessor(this);
			}
		}

		for (int i = 0; i < excSuccessors.size(); i++) {
			if (excSuccessors.get(i).id == oldBlock.id) {
				excSuccessors.set(i, newBlock);
				oldBlock.removeExceptionSuccessor(this);
				newBlock.addExceptionPredecessor(this);
			}
		}
	}
	
	public boolean isSuccessor(FlowBlock block) {
		for (FlowBlock succ : successors) {
			if (succ.id == block.id) {
				return true;
			}
		}
		return false;
	}

	public boolean isPredecessor(FlowBlock block) {
		for(FlowBlock pred : predecessors) {
			if(pred.id == block.id)
				return true;
		}
		return false;
	}
	
	public FlowBlock next() {
		return next;
	}

	public void setNext(FlowBlock next) {
		this.next = next;
	}

	public FlowBlock prev() {
		return prev;
	}

	public void setPrev(FlowBlock prev) {
		this.prev = prev;
	}

	public FlowBlock target() {
		return target;
	}

	public void setTarget(FlowBlock target) {
		this.target = target;
	}

	public boolean visited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public void transfer(InsnList list, AbstractInsnNode pos) {
		for(AbstractInsnNode ain : insns) {
			list.insert(pos, ain);
			pos = ain;
		}
	}
	
	public void transfer(InsnList list) {
		for(AbstractInsnNode ain : insns) {
//			if(!(ain instanceof LabelNode)) {
				list.add(ain);
//			}
		}
	}
	
	public void transfer(List<AbstractInsnNode> ains) {
		ains.addAll(insns);
	}
	
	@Override
	public String toString() {
		return String.format("Block %s", id);
	}
	
	public String toVerboseString(Map<LabelNode, FlowBlock> labels) {
		StringBuilder sb = new StringBuilder(PADDING);

		int size = size();

		sb.append("Block #").append(id);
		sb.append("(len=").append(size);
		sb.append("(").append(cleansize()).append(")");
		sb.append(", pred=").append(predecessors.size());
		sb.append(", succ=").append(successors.size());
		sb.append(")");
		sb.append(PADDING);

		if(size > 0 && labels != null) {
			ListIterator<AbstractInsnNode> it = insns().listIterator();
			int i = 0;
			while(it.hasNext()) {
				AbstractInsnNode ain = it.next();
				i++;
				sb.append(PREFIX).append(i).append(". ").append(toString(ain, labels));
			}
		}
		
		for(FlowBlock block : predecessors) {
			sb.append(PREFIX).append("  > pred: #").append(block.id());
		}
		
		for(FlowBlock block : successors) {
			sb.append(PREFIX).append("  > succ: #").append(block.id());
		}
		
		for(FlowBlock block : excPredecessors) {
			sb.append(PREFIX).append("  > epred #").append(block.id());
		}
		
		for(FlowBlock block : excSuccessors) {
			sb.append(PREFIX).append("  > esucc #").append(block.id());
		}

		return sb.toString();
	}

	private String toString(AbstractInsnNode insn, Map<LabelNode, FlowBlock> labels) {
		if (insn == null) {
			return "null";
		}
		int op = insn.getOpcode();
		if (op == -1) {
			if(insn instanceof LabelNode) {
				LabelNode label = (LabelNode) insn;
				return "#" + labels.get(label);
			}
			return insn.toString();
		}
		StringBuilder sb = new StringBuilder();
		/* pad the opcode name so that all the extra information for the instructions is aligned on the column.
		 * TODO: maybe change the column length to the longest opcode name in the instruction set rather than
		 * out of all the possible ones(statically, the longest opcode name is invokedynamic).*/
		sb.append(pad(OPCODES[op].toLowerCase(), Assembly.LONGEST_OPCODE_NAME));

		switch (insn.getType()) {
			case INT_INSN:
				sb.append(((IntInsnNode) insn).operand);
				break;
			case VAR_INSN:
				sb.append('#').append(((VarInsnNode) insn).var);
				break;
			case TYPE_INSN:
				sb.append(((TypeInsnNode) insn).desc);
				break;
			case FIELD_INSN:
				FieldInsnNode fin = (FieldInsnNode) insn;
				sb.append(fin.owner).append('.').append(fin.name).append(' ').append(fin.desc);
				break;
			case METHOD_INSN:
				MethodInsnNode min = (MethodInsnNode) insn;
				sb.append(min.owner).append('.').append(min.name).append(' ').append(min.desc);
				break;
			case JUMP_INSN:
				LabelNode target = ((JumpInsnNode) insn).label;
				sb.append('#');
				FlowBlock targ = labels.get(target);
				sb.append(targ != null ? targ.id() : "null");
				break;
			case LDC_INSN:
				Object cst = ((LdcInsnNode) insn).cst;
				if(cst instanceof String) {
					sb.append("\"").append(cst).append("\"");
				} else {
					sb.append(cst);
				}
				sb.append(" (").append(cst.getClass().getName()).append(")");
				break;
			case IINC_INSN:
				IincInsnNode iin = (IincInsnNode) insn;
				sb.append('#').append(iin.var).append(' ').append(iin.incr);
				break;
			case TABLESWITCH_INSN: {
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
				sb.append("[def -> #").append(labels.get(tsin.dflt) != null ? labels.get(tsin.dflt).id : "null").append("]");
				
				for(int i=tsin.min; i <= tsin.max; i++) {
					sb.append(", ");
					LabelNode l = tsin.labels.get(i - tsin.min);
					sb.append("[").append(i).append(" -> #").append(labels.get(l) != null ? labels.get(l).id : "null").append("]");
				
				}
			}
				break;
			case LOOKUPSWITCH_INSN: {
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
				sb.append("[def -> #").append(labels.get(lsin.dflt) != null ? labels.get(lsin.dflt).id : "null").append("]");
				
				for(int i=0; i < lsin.keys.size(); i++) {
					sb.append(", ");
					int k = lsin.keys.get(i);
					LabelNode l = lsin.labels.get(i);
					sb.append("[").append(k).append(" -> #").append(labels.get(l) != null ? labels.get(l).id : "null").append("]");
				}
			}
				break;
			case MULTIANEWARRAY_INSN:
				MultiANewArrayInsnNode m = (MultiANewArrayInsnNode) insn;
				sb.append(m.desc).append(' ').append(m.dims);
				break;
		}
		return sb.toString();
	}
}