package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015
 */
public class EmptyGotoCollapser implements Opcodes {

	public void collapse(MethodNode method, ControlFlowGraph graph) {
		SuccessorTree tree = new SuccessorTree();
		tree.map(graph);
		
		List<FlowBlock> ordered = new ArrayList<FlowBlock>();
		for(Successor s : tree) {
			if(s.type() != SuccessorTree.SuccessorType.EMTPY) {
				
			}
		}
		
		System.err.println(tree);
		
		tree.dfs(graph.entry());
	}
}