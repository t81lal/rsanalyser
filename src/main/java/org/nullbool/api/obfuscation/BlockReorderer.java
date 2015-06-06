package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.DummyExitBlock;
import org.nullbool.api.obfuscation.cfg.ExceptionData;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.SuccessorTree;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.obfuscation.cfg.SuccessorTree.SuccessorType;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 5 Jun 2015 16:51:11
 */
public class BlockReorderer implements Opcodes {

	boolean debug = false;

	private SuccessorTree tree = new SuccessorTree();

	public void reorder(MethodNode m, ControlFlowGraph graph) {
		/* Remove the dummy exit (temporarily). */
		DummyExitBlock exit = (DummyExitBlock) graph.exit();
		exit.unlink();

		if(debug)
			System.out.println(graph);

		tree.map(graph);

		List<FlowBlock> dfs = new ArrayList<FlowBlock>();

		Set<FlowBlock> visited = new HashSet<FlowBlock>();
		Stack<FlowBlock> stack = new Stack<FlowBlock>();
		stack.push(graph.entry());
		while(!stack.isEmpty()) {
			FlowBlock v = stack.pop();
			if(!visited.contains(v)) {
				visited.add(v);
				dfs.add(v);
				if(debug)
					System.out.print(v.id() + " ");
				if(v.last() != null && InstructionUtil.isExit(v.last().opcode())) {
					if(debug)
						System.out.print("...");
				}
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

		List<FlowBlock> handlers = new ArrayList<FlowBlock>();
		List<ExceptionData> exceptions = graph.exceptions();
		List<FlowBlock> realHandlers = new ArrayList<FlowBlock>();
		for(ExceptionData d : exceptions) {
			realHandlers.add(d.handler());
		}

		for(FlowBlock b : graph.blocks()) {
			if(!dfs.contains(b)) {
				if(realHandlers.contains(b)) {
					handlers.add(b);
				} else {
					if(debug)
						System.err.printf("%s is unused.%n", b);
				}
			}
		}
		if(debug)
			System.out.println();

		int gotorem = 0;

		ListIterator<FlowBlock> it = dfs.listIterator();
		while(it.hasNext()) {
			FlowBlock b = it.next();
			if(b.last() != null && b.last().opcode() == GOTO) {
				JumpInsnNode jin = (JumpInsnNode) b.last();
				if(it.hasNext()) {
					FlowBlock next = it.next();
					it.previous();
					FlowBlock targ = graph.findTarget(jin.label);
					if(targ != null) {
						if(next.equals(targ) && targ.predecessors().size() == 1) {
							b.removeLast();
							if(debug)
								System.out.printf("letting %s flow into %s.%n", b, targ);
							gotorem++;
						}
					} else {
						//if(debug)
						System.out.println("BlockReorderer.reorder()");
					}
				}
			}
		}
		if(debug)
			System.out.println("size " + m.instructions.size());

		m.instructions.removeAll(true);
		for(FlowBlock f : dfs) {
			for(AbstractInsnNode ain : f.insns()) {
				m.instructions.add(ain);
			}
		}

		if(debug)
			System.out.println("size " + m.instructions.size() + "  - " + gotorem);

		int k = 0;
		//TODO: restructure exception ranges
		for(FlowBlock f : handlers) {
			for(AbstractInsnNode ain : f.insns()) {
				m.instructions.add(ain);
			}
			k += f.size();
		}

		if(debug)
			System.out.println("k " + k);

		if(debug) {
			System.err.println(graph.toString(dfs));
			System.err.println(graph.toString(handlers));
		}

		tree.release();
		exit.relink();
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

		System.out.println();
		
		System.out.println(graph.toString(dfs));
		return dfs;
	}
}