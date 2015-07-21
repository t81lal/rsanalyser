package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.SuccessorType;

/**
 * @author Bibl (don't ban me pls)
 * @created 2 Jun 2015 18:57:42
 */
public class SuccessorTreeDFSIterator implements Iterator<FlowBlock> {

	private final SuccessorTree tree;
	private final Set<FlowBlock> visited;
	private final Deque<Iterator<FlowBlock>> stack;
	private transient FlowBlock next;

	public SuccessorTreeDFSIterator(SuccessorTree tree, FlowBlock entry) {
		this.tree = tree;
		visited   = new HashSet<FlowBlock>();
		stack     = new LinkedList<Iterator<FlowBlock>>();
		next      = entry;
		
		add(next);
	}
	
	void add(FlowBlock next) {
		stack.push(next.exceptionSuccessors().iterator());
		List<FlowBlock> succs = next.successors();
		List<FlowBlock> prioritised = new ArrayList<FlowBlock>();
		
		for(FlowBlock b : succs) {
			if(!(b instanceof DummyExitBlock)) {
				Successor succ = tree.findRelationship(next, b);
				if(succ.type() == SuccessorType.IMMEDIATE) {
					prioritised.add(succ.block());
				}
			}
		}

		for(FlowBlock b : succs) {
			if(!prioritised.contains(b) && !(b instanceof DummyExitBlock))
				prioritised.add(b);
		}
		
		stack.push(prioritised.iterator());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public FlowBlock next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		try {
			visited.add(next);
			return next;
		} finally {
			advance();
		}
	}

	private void advance() {
		Iterator<FlowBlock> successors = stack.peek();
		do {
			while (!successors.hasNext()) {
				stack.pop();
				if (stack.isEmpty()) {
					next = null;
					return;
				}
				successors = stack.peek();
			}

			next = successors.next();
		} while (visited.contains(next));

		add(next);
	}

	public Set<FlowBlock> visited() {
		return visited;
	}

	public FlowBlock last() {
		return next;
	}

	public Deque<Iterator<FlowBlock>> stack() {
		return stack;
	}
}