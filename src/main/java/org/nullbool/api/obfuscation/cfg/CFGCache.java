package org.nullbool.api.obfuscation.cfg;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 28 May 2015
 */
public class CFGCache {

	private final Map<MethodNode, ControlFlowGraph> cache;
	
	public CFGCache() {
		cache = new HashMap<MethodNode, ControlFlowGraph>();
	}
	
	public void add(MethodNode m, ControlFlowGraph graph) {
		cache.put(m, graph);
	}
	
	public ControlFlowGraph get(MethodNode m) throws ControlFlowException {
		if(cache.containsKey(m)) {
			return cache.get(m);
		} else {
			ControlFlowGraph graph = new ControlFlowGraph();
			graph.create(m);
			cache.put(m, graph);
			return graph;
		}
	}
	
	public int size() {
		return cache.size();
	}
}