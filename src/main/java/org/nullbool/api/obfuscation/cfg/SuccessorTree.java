package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.map.NullPermeableLinkedHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.Opcodes;

/**
 * @author Bibl (don't ban me pls)
 * @created 2 Jun 2015 18:15:49
 */
public class SuccessorTree implements Iterable<Successor> {

	private final NullPermeableLinkedHashMap<FlowBlock, List<Successor>> tree;

	public SuccessorTree() {
		tree = new NullPermeableLinkedHashMap<FlowBlock, List<Successor>>(new ValueCreator<List<Successor>>() {
			@Override
			public List<Successor> create() {
				return new ArrayList<Successor>();
			}
		});
	}

	public void map(ControlFlowGraph graph) {
		for(FlowBlock b : graph.blocks()) {
			tree.getNotNull(b);
			
			for(FlowBlock s : b.successors()) {
				Successor successor = new Successor(b, s, typeOf(graph, b, s));
				tree.getNotNull(b).add(successor);
			}
			for(FlowBlock s : b.exceptionSuccessors()) {
				Successor successor = new Successor(b, s, typeOf(graph, b, s));
				tree.getNotNull(b).add(successor);
			}
		}
	}

	/**
	 * Evaluates the relationship between two blocks that are linked.
	 * 
	 * @param graph
	 * @param b
	 * @param target
	 * @return
	 */
	public SuccessorType typeOf(ControlFlowGraph graph, FlowBlock b, FlowBlock target) {
		int cleansize = b.cleansize();
		if(cleansize == 1 && b.last().opcode() == Opcodes.GOTO) {
			return SuccessorType.EMTPY;
		} else if(InstructionUtil.isExit(b.last().opcode())) {
			return SuccessorType.EXIT;
		}

		Iterator<FlowBlock> it = graph.blocks().iterator();
		while(it.hasNext()) {
			FlowBlock current = it.next();
			if(current.equals(b)) {
				FlowBlock next = it.next();
				if(target.equals(next))
					return SuccessorType.IMMEDIATE;
			}
		}


		return null;
	}

	public void release() {
		tree.clear();
	}

	public Map<FlowBlock, List<Successor>> tree() {
		return tree;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Entry<FlowBlock, List<Successor>> e : tree.entrySet()) {
			for(Successor s : e.getValue()) {
				sb.append(s).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	//		1  procedure DFS(G,v):
	//		2      label v as discovered
	//		3      for all edges from v to w in G.adjacentEdges(v) do
	//		4          if vertex w is not labeled as discovered then
	//		5              recursively call DFS(G,w)

	//		1  procedure DFS-iterative(G,v):
	//		2      let S be a stack
	//		3      S.push(v)
	//		4      while S is not empty
	//		5            v = S.pop() 
	//		6            if v is not labeled as discovered:
	//		7                label v as discovered
	//		8                for all edges from v to w in G.adjacentEdges(v) do
	//		9                    S.push(w)

	public void dfs(FlowBlock entry) {
		Set<FlowBlock> visited = new HashSet<FlowBlock>();

		Stack<FlowBlock> stack = new Stack<FlowBlock>();
		stack.push(entry);
		while(!stack.isEmpty()) {
			FlowBlock v = stack.pop();
			if(!visited.contains(v)) {
				visited.add(v);
				System.out.print(v.id() + " ");
				if(v.last() != null && InstructionUtil.isExit(v.last().opcode())) {
					System.out.print("...");
				}
				List<Successor> succs = tree.get(v);
				
				if(succs == null || succs.isEmpty())
					continue;

				/* Do the others. */
				ListIterator<Successor> it = succs.listIterator(succs.size());
				while(it.hasPrevious()) {
					Successor s = it.previous();
					if(!SuccessorType.IMMEDIATE.equals(s.type)) {
						stack.push(s.block);
					}
				}

				/* Favour successors. */
				it = succs.listIterator(succs.size());
				while(it.hasPrevious()) {
					Successor s = it.previous();
					if(SuccessorType.IMMEDIATE.equals(s.type)) {
						stack.push(s.block);
					}
				}
			}
		}
		
		System.out.println();
	}

	public List<Successor> collapsedList() {
		List<Successor> list = new ArrayList<Successor>();
		for(List<Successor> succs : tree.values()) {
			list.addAll(succs);
		}
		return list;
	}

	@Override
	public Iterator<Successor> iterator() {
		List<Successor> list = new ArrayList<Successor>();
		for(List<Successor> succs : tree.values()) {
			list.addAll(succs);
		}
		return list.iterator();
	}

	public static enum SuccessorType {
		IMMEDIATE(), EMTPY(), EXIT();
	}

	public static class Successor {
		private final FlowBlock parent;
		private final FlowBlock block;
		private final SuccessorType type;

		public Successor(FlowBlock parent, FlowBlock block, SuccessorType type) {
			this.parent = parent;
			this.block = block;
			this.type = type;
		}

		public FlowBlock parent() {
			return parent;
		}

		public FlowBlock block() {
			return block;
		}

		public SuccessorType type() {
			return type;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(parent.id()).append(" -> ").append(block.id());
			if(type != null)
				sb.append(" (").append(type.name().toLowerCase()).append(")");
			return sb.toString();
		}
	}
}