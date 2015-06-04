package org.nullbool.api.obfuscation.cfg;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Bibl (don't ban me pls)
 * @created 2 Jun 2015 18:57:42
 */
public class SuccessorTreeDFSIterator implements Iterator<FlowBlock> {

	private final Set<FlowBlock> visited;
	private final Deque<Iterator<FlowBlock>> stack;
	private transient FlowBlock next;

	public SuccessorTreeDFSIterator(FlowBlock entry) {
		visited = new HashSet<FlowBlock>();
		stack   = new LinkedList<Iterator<FlowBlock>>();
		next    = entry;
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
		stack.push(next.exceptionSuccessors().iterator());
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