package org.nullbool.api.obfuscation.flow;

import static org.objectweb.asm.commons.util.Assembly.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
public class FlowBlock {

	private static final String PADDING = "=====";
	private static final String PREFIX  = System.lineSeparator() + "   ";

	private final boolean movable;
	private final String id;
	private final MethodNode method;
	private final LabelNode label;
	private final Map<LabelNode, String> labels;
	private final List<AbstractInsnNode> insns;
	private final List<FlowBlock> predecessors;
	private final List<FlowBlock> successors;

	private FlowBlock prevBlock;
	private FlowBlock nextBlock;

	public FlowBlock(String id, MethodNode method, LabelNode label, Map<LabelNode, String> labels) {
		this(true, id, method, label, labels);
	}
	
	public FlowBlock(boolean movable, String id, MethodNode method, LabelNode label, Map<LabelNode, String> labels) {
		this.movable = movable;
		this.id      = id;
		this.method  = method;
		this.label   = label;
		this.labels  = labels;
		this.insns   = new ArrayList<AbstractInsnNode>();
		predecessors = new ArrayList<FlowBlock>();
		successors   = new ArrayList<FlowBlock>();
	}

	public boolean isMovable() {
		return movable;
	}
	
	public String id() {
		return id;
	}

	public MethodNode method() {
		return method;
	}
	
	public LabelNode label() {
		return label;
	}

	public List<AbstractInsnNode> insns() {
		return insns;
	}
	
	public AbstractInsnNode first() {
		return insns.get(0);
	}
	
	public AbstractInsnNode last() {
		return insns.get(insns.size() - 1);
	}

	public int size() {
		return insns().size();
	}

	public List<FlowBlock> predeccessors() {
		return predecessors;
	}

	public List<FlowBlock> successors() {
		return successors;
	}

	public FlowBlock previous() {
		return prevBlock;
	}

	public void setPrevious(FlowBlock prevBlock) {
		this.prevBlock = prevBlock;
	}

	public FlowBlock next() {
		return nextBlock;
	}

	public void setNext(FlowBlock nextBlock) {
		this.nextBlock = nextBlock;
	}

	public BlockType type() {
		if (BlockType.EMPTY.filter.accept(this)) {
			return BlockType.EMPTY;
		} else if (BlockType.END.filter.accept(this)) {
			return BlockType.END;
		}
		return BlockType.IMMEDIATE;
	}
	
	public boolean accept(Filter<AbstractInsnNode> filter) {
		for (AbstractInsnNode ain : insns) {
			if (filter.accept(ain))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(PADDING);

		int size = size();

		sb.append("Block #").append(id);
		sb.append("(len=").append(size);
		sb.append(", ");
		if(movable) {
			sb.append("movable");
		} else {
			sb.append("immovable");
		}
		sb.append(")");
		sb.append(PADDING);

		if(size > 0) {
			ListIterator<AbstractInsnNode> it = insns().listIterator();
			while(it.hasNext()) {
				AbstractInsnNode ain = it.next();
				sb.append(PREFIX).append(toString(ain));
			}
		}
		
		for(FlowBlock block : predecessors) {
			sb.append(PREFIX).append("  > pred: #").append(block.id());
		}
		
		for(FlowBlock block : successors) {
			sb.append(PREFIX).append("  > succ: #").append(block.id());
		}

		return sb.toString();
	}

	private String toString(AbstractInsnNode insn) {
		if (insn == null) {
			return "null";
		}
		int op = insn.opcode();
		if (op == -1) {
			return insn.toString();
		}
		StringBuilder sb = new StringBuilder();
		/* pad the opcode name so that all the extra information for the instructions is aligned on the column.
		 * TODO: maybe change the column length to the longest opcode name in the instruction set rather than
		 * out of all the possible ones(statically, the longest opcode name is invokedynamic).*/
		sb.append(pad(OPCODES[op].toLowerCase(), LONGEST_OPCODE_NAME));

		switch (insn.type()) {
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
				sb.append(labels.get(target));
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
			case TABLESWITCH_INSN:
				//TODO: switches
				//TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
				//sb.append("def: #").append(labels.get(tsin.dflt));
				break;
			case LOOKUPSWITCH_INSN:
				break;
			case MULTIANEWARRAY_INSN:
				MultiANewArrayInsnNode m = (MultiANewArrayInsnNode) insn;
				sb.append(m.desc).append(' ').append(m.dims);
				break;
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(!(o instanceof FlowBlock))
			return false;
		
		FlowBlock b2 = (FlowBlock) o;
		return id.equals(b2.id);
	}
}