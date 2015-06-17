package org.nullbool.api.obfuscation.cfg;

import java.util.HashMap;
import java.util.Map;

import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 28 May 2015
 */
public class CFGCache {

	private final Map<MethodNode, IControlFlowGraph> cache;
	private final ValueCreator<IControlFlowGraph> graphCreator;
	
	public CFGCache(ValueCreator<IControlFlowGraph> graphCreator) {
		cache = new HashMap<MethodNode, IControlFlowGraph>();
		this.graphCreator = graphCreator;
	}
	
	public void add(MethodNode m, IControlFlowGraph graph) {
		cache.put(m, graph);
	}
	
	public IControlFlowGraph get(MethodNode m) throws ControlFlowException {
		if(cache.containsKey(m)) {
			return cache.get(m);
		} else {
			IControlFlowGraph graph = graphCreator.create();
			graph.create(m);
			cache.put(m, graph);
			return graph;
		}
	}
	
	public int size() {
		return cache.size();
	}
}