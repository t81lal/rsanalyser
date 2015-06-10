package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.DummyExitBlock;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.SuccessorType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 5 Jun 2015 16:51:11
 */
public class BlockReorderer implements Opcodes {

	public static boolean reorder2(MethodNode m, ControlFlowGraph graph) {
		graph.removeDeadBlocks();
		return false;
	}
	
	//TODO: fix
	
	private int count = 0;

	boolean debug = false;

	private SuccessorTree tree = new SuccessorTree();

	public boolean reorder(MethodNode m, ControlFlowGraph graph) {
		/* Remove the dummy exit (temporarily). */
		DummyExitBlock exit = (DummyExitBlock) graph.exit();
		exit.unlink();
		
		tree.map(graph);
		List<FlowBlock> dfs = traceDFS(graph, tree);
		tree.release();
		
		for(int i=0; i < dfs.size(); i++) {
			FlowBlock b = dfs.get(i);
			if(b.lastOpcode() == GOTO && i < (dfs.size() - 1)) {
				FlowBlock next = dfs.get(i + 1);
				FlowBlock targ = graph.findTarget(b);
				
				if(next.equals(targ)) {
					//b.removeLast();
				}
			}
		}
		
		m.instructions.removeAll(true);
		m.tryCatchBlocks.clear();
		
		for(FlowBlock b : dfs) {
			b.transfer(m.instructions);
		}
		
		exit.relink();
		
		return false;

//		if(m.key().equals("cb.az(Lci;Z)V")) {
//			System.out.println("BlockReorderer.reorder()");
//			//debug = true;
//		}
//		if(debug) {
//			System.out.println("   " + graph.blocks());
//			System.out.print("   ");
//		}
//		
//		List<FlowBlock> dfs = new ArrayList<FlowBlock>();
//
//		Set<FlowBlock> visited = new HashSet<FlowBlock>();
//		Stack<FlowBlock> stack = new Stack<FlowBlock>();
//		stack.push(graph.entry());
//		while(!stack.isEmpty()) {
//			FlowBlock v = stack.pop();
//			if(!visited.contains(v)) {
//				visited.add(v);
//				dfs.add(v);
//				if(debug)
//					System.out.print(v.id() + " ");
//				if(v.last() != null && InstructionUtil.isExit(v.last().opcode())) {
//					if(debug)
//						System.out.print("...");
//				}
//				List<Successor> succs = tree.get(v);
//
//				if(succs == null || succs.isEmpty())
//					continue;
//
//				/* Do the others. */
//				ListIterator<Successor> it = succs.listIterator(succs.size());
//				while(it.hasPrevious()) {
//					Successor s = it.previous();
//					if(!SuccessorType.IMMEDIATE.equals(s.type())) {
//						stack.push(s.block());
//					}
//				}
//
//				/* Favour successors. */
//				it = succs.listIterator(succs.size());
//				while(it.hasPrevious()) {
//					Successor s = it.previous();
//					if(SuccessorType.IMMEDIATE.equals(s.type())) {
//						stack.push(s.block());
//					}
//				}
//			}
//		}
//		System.out.println(dfs.size());
//		
//		
//
//		List<FlowBlock> handlers = new ArrayList<FlowBlock>();
//		List<ExceptionData> exceptions = graph.exceptions();
//		List<FlowBlock> realHandlers = new ArrayList<FlowBlock>();
//		
//		if(debug) {
//			System.out.println();
//			System.out.println("   " + exceptions.size() + " " + exceptions);
//		}
//		
//		for(ExceptionData d : exceptions) {
//			realHandlers.add(d.handler());
//		}
//		
//		boolean ub = false;
//		for(FlowBlock b : graph.blocks()) {
//			if(!dfs.contains(b)) {
//				if(realHandlers.contains(b)) {
//					handlers.add(b);
//				} else {
//					ub = true;
//					//if(debug)
//						System.err.printf("   %s: %s is unused.%n", m, b);
//				}
//			}
//		}
//		
//		if(debug) {
//			System.out.println("   Handlers : " + realHandlers);
//			System.out.println("   Handlers2: " + handlers);
//			System.out.println("   Pre return " + ub);
//		}
//		
//		if(ub)
//			return false;
//
//		int gotorem = 0;
//		int k = 0;
//		
//		if(debug && m.key().startsWith("v.s")) {
//			System.err.println("Got(1) at " + m);
//			System.err.println("Block: " + handlers.get(0).toVerboseString(graph.labels()));
//			System.err.println("Contains: " + handlers.get(0).equals(graph.blocks().get(graph.blocks().size() - 1)));
//		}
//		
//		if(m.tryCatchBlocks.size() != handlers.size()) {
//			if(debug)
//				System.out.println("   Diff " + m + " " + m.tryCatchBlocks.size() + " " + handlers.size() + " " + m.tryCatchBlocks);
//			 if(handlers.size() == 0)
//			 	m.tryCatchBlocks.clear();
//			
//			if(m.key().startsWith("v.s")) {
//				System.err.println("Got(1) at " + m);
//			}
//		} else if(handlers.size() == 0) {
//			
//			if(m.key().startsWith("v.s")) {
//				System.err.println("Got(2) at " + m);
//				System.err.println("   Diff " + m + " " + m.tryCatchBlocks.size() + " " + handlers.size() + " " + m.tryCatchBlocks + " " + handlers);
//			}
//			
//			ListIterator<FlowBlock> it = dfs.listIterator();
//			while(it.hasNext()) {
//				FlowBlock b = it.next();
//				if(b.last() != null && b.last().opcode() == GOTO) {
//					JumpInsnNode jin = (JumpInsnNode) b.last();
//					if(it.hasNext()) {
//						FlowBlock next = it.next();
//						it.previous();
//						FlowBlock targ = graph.findTarget(jin.label);
//						if(targ != null) {
//							if(next.equals(targ) && targ.predecessors().size() == 1) {
//								b.removeLast();
//								if(debug)
//									System.out.printf("   Letting %s flow into %s.%n", b, targ);
//								
//								count++;
//								gotorem++;
//							}
//						} else {
//							throw new RuntimeException();
//						}
//					}
//				}
//			}
//			
//			if(debug) {
//				System.out.printf("   Presize: %d.%n", m.instructions.size());
//			}
//
//			m.instructions.removeAll(true);
//			for(FlowBlock f : dfs) {
//				for(AbstractInsnNode ain : f.insns()) {
//					m.instructions.add(ain);
//				}
//			}
//			
//			//TODO: restructure exception ranges
//			for(FlowBlock f : handlers) {
//				for(AbstractInsnNode ain : f.insns()) {
//					m.instructions.add(ain);
//				}
//				k += f.size();
//			}
//		}
//
//		if(debug) {
//			System.out.printf("   New size: %d, removed %d gotos.%n", m.instructions.size(), gotorem);
//			System.out.printf("   %d handler insns added.%n", k);
//			System.out.printf("   Dfs: %s.%n", dfs);
//			System.out.printf("   Handlers: %s. %n", handlers);
//		}
//
//		tree.release();
//		exit.relink();
//		
//		if(debug)
//			debug = false;
//		
//		return gotorem > 0;
	}

	public static List<FlowBlock> traceDFS(ControlFlowGraph graph, SuccessorTree tree) {
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
		System.out.printf("Removed %d gotos.%n", count);
	}
	
	public static List<FlowBlock> visit(ControlFlowGraph graph) {
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