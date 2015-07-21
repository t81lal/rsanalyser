/**
 * 
 */
package org.nullbool.api.obfuscation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.ExceptionData;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTreeDFSIterator;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 20 Jun 2015 14:53:51
 */
public class ControlFlowFixer {

	public void fix(MethodNode m, IControlFlowGraph graph) {
		// DFSIterator it = new DFSIterator(graph.entry(), true);
		// List<FlowBlock> dfs = new ArrayList<FlowBlock>();
		// while(it.hasNext()) {
		// 	dfs.add(it.next());
		// }
		// Remove dead blocks, rs obfuscator doesn't 
		// seem to add dead code tho (at least in
		//  real methods)
		// removeDeadBlocks(m, graph, dfs);
		
//		System.out.println(graph.toString());
		
//		DominatorTree dt = new DominatorTree(graph);
//		m.instructions.removeAll(true);
//		List<FlowBlock> re = new ArrayList<FlowBlock>();
//		for(FlowBlock b : dt.topologicalTraversal()) {
//			System.out.println(b);
//			re.add(b);
//			b.transfer(m.instructions);
//		}
//
//		System.out.println(((SaneControlFlowGraph) graph).toString(re));
		
		if(m.owner.name.equals("af") && m.name.equals("bf")) {

			System.out.println(graph.toString());
		}
		
		SuccessorTree tree = new SuccessorTree();
		tree.map(graph);
		SuccessorTreeDFSIterator it = new SuccessorTreeDFSIterator(tree, graph.entry());
		m.instructions.removeAll(true);
		List<FlowBlock> re = new ArrayList<FlowBlock>();
		while(it.hasNext()) {
			FlowBlock n = it.next();
			re.add(n);
			n.transfer(m.instructions);
		}


		
	}
	
	// dom(N0) = {N0} where N0 is the start node (this set never changes)
	// For all nodes but N0, initially set dom(N) = {all nodes}
	// Push each node but N0 onto a worklist.
	// Now remove any node, Z, from the worklist.
	// 	Compute a new value for dom(Z) using the above formula.
	// 	If the new value of dom(Z) differs from the current value,
	// 	use the new value.
	// 	Add all successors to Z to the worklist (if they are
	// 	 not already on the list, except N0 whose value is known).
	// Repeat until the worklist is empty.
	
	public static Map<FlowBlock, Set<FlowBlock>> computeDominators(IControlFlowGraph graph) {
		final FlowBlock n0 = graph.entry();
		List<FlowBlock> allBlocks = graph.blocks();
		Set<FlowBlock> others = new HashSet<FlowBlock>(allBlocks);
		others.remove(n0);
		
		Map<FlowBlock, Set<FlowBlock>> domtree = new HashMap<FlowBlock, Set<FlowBlock>>();
		Deque<FlowBlock> worklist = new LinkedList<FlowBlock>();
		
		for(FlowBlock block : allBlocks) {
			// set dom(N) = {all nodes}, except for n0
			domtree.put(block, new HashSet<FlowBlock>(allBlocks));
		}
		
		worklist.addAll(others);
		
		while(!worklist.isEmpty()) {
			// dom(Z) = {Z} Union (Intersect (over Y in Pred(Z)) dom(Y))
			FlowBlock block = worklist.pop();
			
			Set<FlowBlock> doms = new HashSet<FlowBlock>(allBlocks);
			for(FlowBlock p : block.predecessors()) {
				doms.retainAll(domtree.get(p));
			}
			for(FlowBlock p : block.exceptionPredecessors()) {
				doms.retainAll(domtree.get(p));
			}
			doms.add(block);
						
			Set<FlowBlock> old = domtree.get(block);
			if(!equals(old, doms)) {
				domtree.put(block, doms);
				
				for(FlowBlock s : block.successors()) {
					if(!worklist.contains(s) && s != n0)
						worklist.add(s);
				}
				for(FlowBlock s : block.exceptionSuccessors()) {
					if(!worklist.contains(s) && s != n0)
						worklist.add(s);
				}
			}
		}
		
		return domtree;
	}
	
	public static <T> boolean equals(Set<T> s1, Set<T> s2) {
		if(s1.size() != s2.size())
			return false;
		
		for(T t : s1) {
			if(!s2.contains(t))
				return false;
		}
		return true;
	}
	
	//	1  procedure DFS-iterative(G,v):
	//	2      let S be a stack
	//	3      S.push(v)
	//	4      while S is not empty
	//	5            v = S.pop() 
	//	6            if v is not labeled as discovered:
	//	7                label v as discovered
	//	8                for all edges from v to w in G.adjacentEdges(v) do
	//	9                    S.push(w)

	public static List<FlowBlock> orderDfs(IControlFlowGraph graph) {
		List<FlowBlock> dfs = new ArrayList<FlowBlock>();
		List<ExceptionData> exceptions = graph.exceptions();
		List<FlowBlock> handlers = new ArrayList<FlowBlock>();
		for(ExceptionData ed : exceptions) {
			handlers.add(ed.handler());
		}
		
		Deque<FlowBlock> stack = new ArrayDeque<FlowBlock>();
		
		stack.push(graph.entry());
		while(!stack.isEmpty()) {
			FlowBlock v = stack.pop();
			if(!dfs.contains(v)) {
				//TODO:
				dfs.add(v);
			}
		}
		
		return dfs;
	}
	
	private void removeDeadBlocks(MethodNode m, IControlFlowGraph graph, List<FlowBlock> dfs) {		
		Set<FlowBlock> dead = new HashSet<FlowBlock>();
		dead.addAll(graph.blocks());
		dead.removeAll(dfs);
		
		if(dead.size() > 0) {
			System.out.println(m + " has " + dead.size() + " dead blocks.");
		}
	}
	
	public static void test(int k) {
		if(k > 10) {
			System.out.println("1");
		} else if(k == 5) {
			System.out.println("2");
		}
		
		System.out.println("s3");
		
		if((k + 100) == 999) {
			System.out.println("4");
		} else {
			System.out.println("5");
		}
		
		System.out.println("6");
	}
	
	/*public static void main(String[] args) throws ControlFlowException {
		ClassNode cn = ClassStructure.create(ControlFlowFixer.class.getCanonicalName());
		for(MethodNode m : cn.methods) {
			if(m.name.equals("test")) {
				SaneControlFlowGraph graph = new SaneControlFlowGraph();
				graph.create(m);
				System.out.println(graph.toString(graph.blocks()));
				Map<FlowBlock, Set<FlowBlock>> domtree = computeDominators(graph);
				for(Entry<FlowBlock, Set<FlowBlock>> e : domtree.entrySet()) {
					System.out.println(e.getKey().id() + " : " + e.getValue());
				}
			}
		}
	}*/
}