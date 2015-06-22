/**
 * 
 */
package org.nullbool.api.obfuscation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.DFSIterator;
import org.nullbool.api.obfuscation.cfg.ExceptionData;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 20 Jun 2015 14:53:51
 */
public class ControlFlowFixer {

	public void fix(MethodNode m, IControlFlowGraph graph) {
		DFSIterator it = new DFSIterator(graph.entry(), true);
		List<FlowBlock> dfs = new ArrayList<FlowBlock>();
		while(it.hasNext()) {
			dfs.add(it.next());
		}
		// Remove dead blocks
		removeDeadBlocks(m, graph, dfs);
		
		
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
}