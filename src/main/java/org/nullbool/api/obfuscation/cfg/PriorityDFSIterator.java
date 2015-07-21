package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class PriorityDFSIterator implements Iterator<FlowBlock> {

	private final Comparator<FlowBlock> comparator;
	private final Set<FlowBlock> visited;
	private final Deque<Iterator<FlowBlock>> stack;
	private transient FlowBlock next;

	public PriorityDFSIterator(Comparator<FlowBlock> comparator, FlowBlock entry) {
		this.comparator = comparator;
		visited         = new HashSet<FlowBlock>();
		stack           = new LinkedList<Iterator<FlowBlock>>();
		next            = entry;
		
		add(next);
	}
	
	void add(FlowBlock next) {
		stack.push(next.exceptionSuccessors().iterator());
		List<FlowBlock> succs = new ArrayList<FlowBlock>(next.successors());
		
		ListIterator<FlowBlock> it = succs.listIterator();
		while(it.hasNext()) {
			if(it.next() instanceof DummyExitBlock) {
				it.remove();
			}
		}
		
		Collections.sort(succs, comparator);
		stack.push(succs.iterator());
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