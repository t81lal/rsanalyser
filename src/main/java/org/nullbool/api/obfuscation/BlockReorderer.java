package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.cfg.DummyExitBlock;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.SuccessorType;
import org.nullbool.api.util.InsnListPrinter;
import org.nullbool.api.util.LabelHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 5 Jun 2015 16:51:11
 */
public class BlockReorderer implements Opcodes {
	
	private int count = 0;
	private SuccessorTree tree = new SuccessorTree();

	public boolean reorder(MethodNode m, IControlFlowGraph graph) {
		/* Remove the dummy exit (temporarily). */
		DummyExitBlock exit = (DummyExitBlock) graph.exit();
		exit.unlink();
		tree.map(graph);
		
		for(FlowBlock b : graph.blocks()) {
			for(FlowBlock s : b.successors()) {
				if(s.lastOpcode() == GOTO) {
					Successor succ = tree.findRelationship(b, s);
					if(succ.type() == SuccessorType.EMPTY_GOTO) {
						m.instructions.remove(b.last());
						count++;
					}
				}
			}
		}
		
		if(m.key().startsWith("bf.j")) {
			for(String s : new InsnListPrinter(m.instructions).createPrint()) {
				System.out.println(s);
			}
		}
		
//		System.out.println(tree);
//		if(true)
//			System.exit(1);

//		List<FlowBlock> dfs = new ArrayList<FlowBlock>();
//		for(FlowBlock b : traceDFS(graph, tree)) {
//			dfs.add(b);
//		}

//		m.instructions.removeAll(true);
//
//		int rem = 0;
//		
//		for(int i=0; i < dfs.size(); i++) {
//			FlowBlock b = dfs.get(i);
//			if(b.lastOpcode() == GOTO && i < (dfs.size() - 1)) {
//				FlowBlock next = dfs.get(i + 1);
//				FlowBlock targ = graph.findTarget(b);
//
//				if(next.equals(targ)) {
//					b.removeLast();
//					rem++;
//				}
//			}
//
//			b.transfer(m.instructions);
//		}

//		if(rem > 0) {
//			System.out.println(m + " removed " + rem);
//		}
		exit.relink();
		tree.release();

		return false;
	}
	
	public static List<FlowBlock> dfs(SuccessorTree tree, IControlFlowGraph graph) {
		FlowBlock entry = graph.entry();
		return dfs(tree, entry, new boolean[LabelHelper.numeric(entry.id())]);
	}
	
	public static List<FlowBlock> dfs(SuccessorTree tree, FlowBlock entry, boolean[] visited) {
		List<FlowBlock> blocks = new ArrayList<FlowBlock>();
		for(Successor succ : tree.get(entry)) {
			int index = LabelHelper.numeric(succ.block().id());
			if(!visited[index]) {
				visited[index] = true;
				blocks.add(succ.block());
				blocks.addAll(dfs(tree, succ.block(), visited));
			}
		}
		return blocks;
	}
	
	public static List<Structure> identifyStructures(IControlFlowGraph graph, SuccessorTree tree) {
		
		return null;
	}
	
	public static class Structure {
		
	}

	public static List<FlowBlock> traceDFS(IControlFlowGraph graph, SuccessorTree tree) {
		List<FlowBlock> dfs = new ArrayList<FlowBlock>();

		Set<FlowBlock> visited = new HashSet<FlowBlock>();
		Stack<FlowBlock> stack = new Stack<FlowBlock>();
		stack.push(graph.entry());
		while(!stack.isEmpty()) {
			FlowBlock v = stack.pop();
			if(!visited.contains(v)) {
				visited.add(v);
				dfs.add(v);
				//System.out.print(v.id() + " ");
				//if(v.last() != null && InstructionUtil.isExit(v.last().opcode())) {
				//	System.out.print("...");
				//}
				List<Successor> succs = tree.get(v);

				if(succs == null || succs.isEmpty())
					continue;

				/* Do the others. */
				ListIterator<Successor> it = succs.listIterator(succs.size());
				while(it.hasPrevious()) {
					Successor s = it.previous();
					if(!SuccessorType.IMMEDIATE.equals(s.type())) {
						stack.push(s.block());
					}
				}

				/* Favour successors. */
				it = succs.listIterator(succs.size());
				while(it.hasPrevious()) {
					Successor s = it.previous();
					if(SuccessorType.IMMEDIATE.equals(s.type())) {
						stack.push(s.block());
					}
				}
			}
		}

		//System.out.println();

		//System.out.println(graph.toString(dfs));
		return dfs;
	}

	public void output() {
		System.err.println("Running Flow Fixer");
		System.out.printf("   Removed %d gotos.%n", count);
	}

	public static List<FlowBlock> visit(IControlFlowGraph graph) {
		LinkedList<FlowBlock> stack = new LinkedList<FlowBlock>();
		List<FlowBlock> ordered = new ArrayList<FlowBlock>();

		FlowBlock entry = graph.entry();
		stack.add(entry);
		ordered.add(entry);

		while (!stack.isEmpty()) {
			FlowBlock block = stack.removeFirst();

			List<FlowBlock> successors = new ArrayList<FlowBlock>();
			successors.addAll(block.successors());
			//successors.addAll(block.exceptionSuccessors());

			for (FlowBlock succ : successors) {
				if (!ordered.contains(succ)) {
					stack.add(succ);
					ordered.add(succ);
				}
			}
		}

		return ordered;
	}
}