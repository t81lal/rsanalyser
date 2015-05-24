package org.nullbool.api.obfuscation.flow;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.commons.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class BasicBlock {

	public int id = 0;
	public int mark = 0;

	private final List<Integer> instrOldOffsets;
	private InsnList insns;
	private List<BasicBlock> preds;
	private List<BasicBlock> succs;
	private List<BasicBlock> predExceptions;
	private List<BasicBlock> succExceptions;

	public BasicBlock(int id) {
		this.id = id;

		instrOldOffsets = new ArrayList<Integer>();
		insns           = new InsnList();
		preds           = new ArrayList<BasicBlock>();
		succs           = new ArrayList<BasicBlock>();
		predExceptions  = new ArrayList<BasicBlock>();
		succExceptions  = new ArrayList<BasicBlock>();
	}
	@Override
	public Object clone() {
		BasicBlock block = new BasicBlock(id);

		//TODO: clone
		block.setSeq(insns);
		block.instrOldOffsets.addAll(instrOldOffsets);

		return block;
	}

	public void free() {
		preds.clear();
		succs.clear();
		instrOldOffsets.clear();
		succExceptions.clear();
		insns = new InsnList();
	}

	public AbstractInsnNode getInstruction(int index) {
		return insns.get(index);
	}

	public AbstractInsnNode getLastInstruction() {
		return insns.getLast();
	}

	public int getOldOffset(int index) {
		if(index < instrOldOffsets.size()) {
			return instrOldOffsets.get(index);
		} else {
			return -1;
		}
	}

	public int size() {
		return insns.size();
	}

	public void addPredecessor(BasicBlock block) {
		preds.add(block);
	}

	public void removePredecessor(BasicBlock block) {
		while (preds.remove(block)) ;
	}

	public void addSuccessor(BasicBlock block) {
		succs.add(block);
		block.addPredecessor(this);
	}

	public void removeSuccessor(BasicBlock block) {
		while (succs.remove(block)) ;
		block.removePredecessor(this);
	}

	// FIXME: unify block comparisons: id or direkt equality
	public void replaceSuccessor(BasicBlock oldBlock, BasicBlock newBlock) {
		for (int i = 0; i < succs.size(); i++) {
			if (succs.get(i).id == oldBlock.id) {
				succs.set(i, newBlock);
				oldBlock.removePredecessor(this);
				newBlock.addPredecessor(this);
			}
		}

		for (int i = 0; i < succExceptions.size(); i++) {
			if (succExceptions.get(i).id == oldBlock.id) {
				succExceptions.set(i, newBlock);
				oldBlock.removePredecessorException(this);
				newBlock.addPredecessorException(this);
			}
		}
	}

	public void addPredecessorException(BasicBlock block) {
		predExceptions.add(block);
	}

	public void removePredecessorException(BasicBlock block) {
		while (predExceptions.remove(block)) ;
	}

	public void addSuccessorException(BasicBlock block) {
		if (!succExceptions.contains(block)) {
			succExceptions.add(block);
			block.addPredecessorException(this);
		}
	}

	public void removeSuccessorException(BasicBlock block) {
		while (succExceptions.remove(block)) ;
		block.removePredecessorException(this);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		return String.format("%d:%n%s", id, instructionsToString(indent));
	}

	public String instructionsToString(int indent) {
		StringBuilder sb = new StringBuilder();
		AbstractInsnNode[] ains = insns.toArray();
		for (int i = 0; i < ains.length; i++) {
			AbstractInsnNode ain = ains[i];
			sb.append(String.format("   %s%n", Assembly.toString(ain)));

			//buf.append(InterpreterUtil.getIndentString(indent));
			//buf.append(collinstr.getKey(i).intValue());
			//buf.append(": ");
			//buf.append(collinstr.get(i).toString());
			//buf.append(new_line_separator);
		}
		return sb.toString();
	}

	public String toStringOldIndices() {
		StringBuilder buf = new StringBuilder();
		AbstractInsnNode[] ains = insns.toArray();
		for (int i = 0; i < ains.length; i++) {
			if (i < instrOldOffsets.size()) {
				buf.append(instrOldOffsets.get(i));
			}
			else {
				buf.append("-1");
			}
			AbstractInsnNode ain = ains[i];
			buf.append(": ").append(String.format("%s%n", Assembly.toString(ain)));
			//buf.append(seq.getInstr(i).toString());
			//buf.append(new_line_separator);
		}

		return buf.toString();
	}

	public boolean isSuccessor(BasicBlock block) {
		for (BasicBlock succ : succs) {
			if (succ.id == block.id) {
				return true;
			}
		}
		return false;
	}

	public boolean isPredecessor(BasicBlock block) {
		for (int i = 0; i < preds.size(); i++) {
			if (preds.get(i).id == block.id) {
				return true;
			}
		}
		return false;
	}
	
	public List<Integer> getInstrOldOffsets() {
		return instrOldOffsets;
	}

	public List<BasicBlock> getPredecessors() {
		List<BasicBlock> lst = new ArrayList<BasicBlock>(preds);
		lst.addAll(predExceptions);
		return lst;
	}

	public List<BasicBlock> getPreds() {
		return preds;
	}

	public void setPreds(List<BasicBlock> preds) {
		this.preds = preds;
	}

	public InsnList getSeq() {
		return insns;
	}

	public void setSeq(InsnList insns) {
		this.insns = insns;
	}

	public List<BasicBlock> getSuccs() {
		return succs;
	}

	public void setSuccs(List<BasicBlock> succs) {
		this.succs = succs;
	}


	public List<BasicBlock> getSuccExceptions() {
		return succExceptions;
	}


	public void setSuccExceptions(List<BasicBlock> succExceptions) {
		this.succExceptions = succExceptions;
	}

	public List<BasicBlock> getPredExceptions() {
		return predExceptions;
	}

	public void setPredExceptions(List<BasicBlock> predExceptions) {
		this.predExceptions = predExceptions;
	}
}