package org.nullbool.api.obfuscation.cfg;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

public class DFSIterator implements Iterator<FlowBlock> {

	protected final Set<FlowBlock> visited;
	protected final Deque<Iterator<FlowBlock>> stack;
	private transient FlowBlock next;

	public DFSIterator(FlowBlock entry, boolean exceptions) {
		visited = new HashSet<FlowBlock>();
		stack   = new LinkedList<Iterator<FlowBlock>>();
		next    = entry;
		
		init(exceptions);
	}
	
	public DFSIterator(FlowBlock entry) {
		this(entry, true);
	}
	
	public void init(boolean exceptions) {
		stack.push(next.successors().listIterator());
		if(exceptions)
			stack.push(next.exceptionSuccessors().iterator());
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
		
		stack.push(next.successors().iterator());
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