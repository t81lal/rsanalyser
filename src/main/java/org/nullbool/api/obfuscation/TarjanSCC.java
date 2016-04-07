package org.nullbool.api.obfuscation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.DummyExitBlock;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.InsaneControlFlowGraph;
import org.nullbool.api.util.ClassStructure;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 Jul 2015 00:31:30
 */
public class TarjanSCC {

	public static class SSC {
		public final Set<FlowBlock> blocks = new HashSet<FlowBlock>();

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("SSC:\n");
			Iterator<FlowBlock> it = blocks.iterator();
			while (it.hasNext()) {
				FlowBlock b = it.next();
				sb.append(b.id());
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
	}

	public static class TarjanBlock {
		public final FlowBlock block;
		public boolean marked;
		public int index;
		public int lowlink;
		public boolean onstack;

		public TarjanBlock(FlowBlock block) {
			this.block = block;
			marked = false;
			index = 0;
			onstack = false;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Block: ").append(block.id()).append(", marked=").append(marked);
			sb.append(", index=").append(index).append(", lowlink=").append(lowlink);
			sb.append(", onstack=").append(onstack);
			return sb.toString();
		}
	}

	private final Map<FlowBlock, TarjanBlock> blocks;
	private final Stack<TarjanBlock> stack;
	private final Set<SSC> sscs;
	private int index;

	public TarjanSCC() {
		blocks = new HashMap<FlowBlock, TarjanBlock>();
		stack = new Stack<TarjanBlock>();
		sscs = new HashSet<SSC>();
		index = 0;
	}

	public Set<SSC> tarjan(IControlFlowGraph graph) {
		List<FlowBlock> blocks = graph.blocks();
		if (blocks == null || blocks.isEmpty())
			return null;

		this.blocks.clear();
		stack.clear();
		sscs.clear();
		index = 0;

		for (FlowBlock b : blocks) {
			this.blocks.put(b, new TarjanBlock(b));
		}

		for (Entry<FlowBlock, TarjanBlock> e : this.blocks.entrySet()) {
			TarjanBlock b = e.getValue();
			if (!b.marked) {
				strongconnect(b);
			}
		}

		// for(TarjanBlock b : this.blocks.values()) {
		// System.out.println(b);
		// }
		// System.out.println(sscs.size());

		return sscs;
	}

	private void strongconnect(TarjanBlock b) {
		b.marked = true;
		b.index = index;
		b.lowlink = index;
		index++;
		stack.push(b);

		b.onstack = true;

		for (FlowBlock _s : b.block.successors()) {
			if (!(_s instanceof DummyExitBlock)) {
				TarjanBlock s = blocks.get(_s);
				if (!s.marked) {
					strongconnect(s);
					b.lowlink = Math.min(b.lowlink, s.lowlink);
				} else if (s.onstack) {
					b.lowlink = Math.min(b.lowlink, s.index);
				}
			}
		}

		for (FlowBlock _s : b.block.exceptionSuccessors()) {
			if (!(_s instanceof DummyExitBlock)) {
				TarjanBlock s = blocks.get(_s);
				if (!s.marked) {
					strongconnect(s);
					b.lowlink = Math.min(b.lowlink, s.lowlink);
				} else if (s.onstack) {
					b.lowlink = Math.min(b.lowlink, s.index);
				}
			}
		}

		if (b.lowlink == b.index) {
			SSC ssc = new SSC();
			TarjanBlock w;
			while ((w = stack.pop()) != b) {
				w.onstack = false;
				ssc.blocks.add(w.block);
			}
			sscs.add(ssc);
		}
	}

	public static void main(String[] args) throws ControlFlowException {
		ClassNode cn = ClassStructure.create(TarjanSCC.class.getCanonicalName());
		for (MethodNode m : cn.methods) {
			if (m.name.equals("strongconnect")) {
				IControlFlowGraph graph = new InsaneControlFlowGraph();
				graph.create(m);
				System.out.println(graph);

				TarjanSCC ssc = new TarjanSCC();
				for (SSC c : ssc.tarjan(graph)) {
					System.out.println(c);
				}
			}
		}
	}
}