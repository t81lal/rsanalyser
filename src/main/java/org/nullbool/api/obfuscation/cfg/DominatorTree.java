package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.refactor.SetCreator;
import org.nullbool.api.util.map.NullPermeableHashMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 20 Jul 2015 16:06:34
 */
public class DominatorTree {

	private final Map<FlowBlock, Integer> semiDominators = new HashMap<FlowBlock, Integer>();
	private final List<FlowBlock> blocks = new ArrayList<FlowBlock>();
	private final Map<FlowBlock, FlowBlock> mapped = new HashMap<FlowBlock, FlowBlock>();
	private final BlockMap predecessors = new BlockMap();
	private final Map<FlowBlock, FlowBlock> parents = new HashMap<FlowBlock, FlowBlock>();
	private final Map<FlowBlock, FlowBlock> ancestors = new HashMap<FlowBlock, FlowBlock>();
	private final BlockMap linkde = new BlockMap();
	private final Map<FlowBlock, FlowBlock> idoms = new HashMap<FlowBlock, FlowBlock>();

	private BlockMap dominatorTree = null;
	private BlockMap dominanceFrontiers = null;
	private LinkedList<FlowBlock> topologicallySorted = null;

	public DominatorTree(IControlFlowGraph graph) {
		SuccessorTree tree = new SuccessorTree();
		tree.map(graph);
		
		dfs(tree, graph.entry());
		for(ExceptionData ed : graph.exceptions()) {
			dfs(tree, ed.handler());
		}
		
		computeDominators();
	}

	public BlockMap getDominatorTree() {
		if (dominatorTree == null) {
			dominatorTree = new BlockMap();
			for (FlowBlock node : idoms.keySet())
				dominatorTree.getNonNull(idoms.get(node)).add(node);
		}

		return dominatorTree;
	}

	public BlockMap getDominanceFrontiers() {
		if (dominanceFrontiers == null) {
			dominanceFrontiers = new BlockMap();

			// touch the dominator tree
			getDominatorTree();

			for (FlowBlock b : reverseTopologicalTraversal()) {
				Set<FlowBlock> dfx = dominanceFrontiers.getNonNull(b);

				// Compute DF(local)
				for (FlowBlock y : b.successors())
					if (idoms.get(y) != b)
						dfx.add(y);
				for (FlowBlock y : b.exceptionSuccessors())
					if (idoms.get(y) != b)
						dfx.add(y);

				// Compute DF(up)
				for (FlowBlock z : dominatorTree.getNonNull(b))
					for (FlowBlock y : dominanceFrontiers.getNonNull(z))
						if (idoms.get(y) != b)
							dfx.add(y);
			}
		}

		return dominanceFrontiers;
	}

	public Iterable<FlowBlock> reverseTopologicalTraversal() {
		return new Iterable<FlowBlock>() {
			@Override
			public Iterator<FlowBlock> iterator() {
				return topologicalSort0().descendingIterator();
			}
		};
	}

	public List<FlowBlock> topologicalTraversal() {
		return Collections.unmodifiableList(topologicalSort0());
	}

	private LinkedList<FlowBlock> topologicalSort0() {
		if (topologicallySorted == null) {
			topologicallySorted = new LinkedList<FlowBlock>();
			for (FlowBlock node : blocks) {
				int idx = topologicallySorted.indexOf(idoms.get(node));
				if (idx != -1)
					topologicallySorted.add(idx + 1, node);
				else
					topologicallySorted.add(node);
			}
		}
		return topologicallySorted;
	}

	private void dfs(SuccessorTree tree, FlowBlock root) {
		Iterator<FlowBlock> it = new SuccessorTreeDFSIterator(tree, root);

		while (it.hasNext()) {
			FlowBlock node = it.next();

			if (!semiDominators.containsKey(node)) {
				blocks.add(node);

				semiDominators.put(node, semiDominators.size());
				mapped.put(node, node);

				for (FlowBlock child : node.successors()) {
					predecessors.getNonNull(child).add(node);
					if (!semiDominators.containsKey(child)) {
						parents.put(child, node);
					}
				}
			}
		}
	}

	private void computeDominators() {
		int lastSemiNumber = semiDominators.size() - 1;

		for (int i = lastSemiNumber; i > 0; i--) {
			FlowBlock b = blocks.get(i);
			FlowBlock parent = parents.get(b);

			int semidominator = semiDominators.get(b);
			for (FlowBlock v : predecessors.get(b))
				semidominator = Math.min(semidominator, semiDominators.get(lengtarj(v)));

			semiDominators.put(b, semidominator);
			linkde.getNonNull(blocks.get(semidominator)).add(b);

			ancestors.put(b, parent);

			for (FlowBlock v : linkde.getNonNull(parent)) {
				FlowBlock u = lengtarj(v);

				if (semiDominators.get(u) < semiDominators.get(v))
					idoms.put(v, u);
				else
					idoms.put(v, parent);
			}

			linkde.getNonNull(parent).clear();
		}

		for (int i = 1; i <= lastSemiNumber; i++) {
			FlowBlock w = blocks.get(i);

			if (idoms.get(w) != blocks.get((semiDominators.get(w))))
				idoms.put(w, idoms.get(idoms.get(w)));
		}
	}

	private FlowBlock lengtarj(FlowBlock v) {
		propogate(v);
		return mapped.get(v);
	}

	private void propogate(FlowBlock v) {
		Stack<FlowBlock> worklist = new Stack<FlowBlock>();
		worklist.add(v);

		FlowBlock a = ancestors.get(v);

		// Traverse back to the subtree root.
		while (ancestors.containsKey(a)) {
			worklist.push(a);
			a = ancestors.get(a);
		}

		// Propagate semidominator information forward.
		FlowBlock ancestor = worklist.pop();
		int leastSemi = semiDominators.get(mapped.get(ancestor));

		while (!worklist.empty()) {
			FlowBlock descendent = worklist.pop();
			int currentSemi = semiDominators.get(mapped.get(descendent));

			if (currentSemi > leastSemi)
				mapped.put(descendent, mapped.get(ancestor));
			else
				leastSemi = currentSemi;

			// Prepare to process the next iteration.
			ancestor = descendent;
		}
	}

	/*public static void main(String[] args) throws ControlFlowException {
		ClassNode cn = ClassStructure.create(ControlFlowFixer.class.getCanonicalName());
		for (MethodNode m : cn.methods) {
			if (m.name.equals("test")) {
				SaneControlFlowGraph graph = new SaneControlFlowGraph();
				graph.create(m);
				System.out.println(graph.toString(graph.blocks()));
				DominatorTree dt = new DominatorTree(graph);
				BlockMap domtree = dt.getDominanceFrontiers();

				for(FlowBlock b : dt.getToplogicalTraversalImplementation()) {
					System.out.println(b);
				}
				for (Entry<FlowBlock, Set<FlowBlock>> e : domtree.entrySet()) {
					System.out.println(e.getKey() + "  " + e.getValue());
				}
			}
		}
	}*/

	public static class BlockMap extends NullPermeableHashMap<FlowBlock, Set<FlowBlock>> {
		private static final long serialVersionUID = 8652178379903549240L;

		public BlockMap() {
			super(new SetCreator<FlowBlock>());
		}
	}
}