package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.SuccessorTree.Successor;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.map.NullPermeableLinkedHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.util.Printer;

/**
 * <p>
 * A utility class for evaluating and storing relationships
 * between basic blocks in a control flow diagram.
 * </p>
 * 
 * <p>
 * Note that after creating a tree tree, one must call the
 * {@link #release()} method to clear the previously mapped
 * data from the cache.
 * </p>
 * 
 * @see FlowBlock
 * @see ControlFlowGraph
 * 
 * @author Bibl (don't ban me pls)
 * @created 2 Jun 2015 18:15:49
 */
public class SuccessorTree implements Iterable<Successor> {

	private final NullPermeableLinkedHashMap<FlowBlock, List<Successor>> tree;

	public SuccessorTree() {
		tree = new NullPermeableLinkedHashMap<FlowBlock, List<Successor>>(new ValueCreator<List<Successor>>() {
			@Override
			public List<Successor> create() {
				return new ArrayList<Successor>();
			}
		});
	}

	public void map(IControlFlowGraph graph) {
		for(FlowBlock block : graph.blocks()) {
			mapSuccessors(graph, block, block.successors());
			mapSuccessors(graph, block, block.exceptionSuccessors());
		}
	}

	public void mapSuccessors(IControlFlowGraph graph, FlowBlock parent, List<FlowBlock> blockSuccs){
		for(FlowBlock succ : blockSuccs) {
			if(!(succ instanceof DummyExitBlock)) {
				Successor successor = new Successor(parent, succ, typeOf(graph, parent, succ));
				tree.getNotNull(parent).add(successor);
			}
		}
	}

	/**
	 * Evaluates the relationship between two blocks that are linked.
	 * 
	 * @param graph
	 * @param b A block.
	 * @param target A successor of that block.
	 * @return
	 * @throws RuntimeException If no relationship was found.
	 */
	public SuccessorType typeOf(IControlFlowGraph graph, FlowBlock b, FlowBlock target) {
		if(b.last() == null)
			return SuccessorType.DEAD;

		if(b.exceptionSuccessors().contains(target))
			return SuccessorType.EXCEPTION;

		List<FlowBlock> blocks = graph.blocks();
		int index = blocks.indexOf(b);
		int targetIndex = blocks.indexOf(target);
		if(index == -1 || targetIndex == -1) 
			throw new RuntimeException(String.format("%s:%d, %s:%d.", b.id(), index, target.id(), targetIndex));
		boolean immediate = (index + 1) == targetIndex;

		int lastOpcode = b.lastOpcode();

		if(b.last() instanceof JumpInsnNode) {
			if(b.cleansize() == 1 && lastOpcode == Opcodes.GOTO && immediate) {
				return SuccessorType.EMPTY_GOTO;
			} else if(!immediate){
				return SuccessorType.FAR_JUMP;
			}
		} else if(lastOpcode == Opcodes.LOOKUPSWITCH) {
			LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) b.last();
			
			Set<LabelNode> labels = new HashSet<LabelNode>(lsin.labels);
			labels.add(lsin.dflt);
			
			/* Validate to make sure the target is in the table. */
			for(LabelNode l : labels) {
				FlowBlock caseBlock = graph.findTarget(l);
				if(caseBlock.equals(target)) {
					if(immediate) {
						return SuccessorType.IMMEDIATE;
					} else {
						return SuccessorType.FAR_JUMP;
					}
				}
			}
		} else if(lastOpcode == Opcodes.TABLESWITCH) {
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) b.last();
			
			Set<LabelNode> labels = new HashSet<LabelNode>(tsin.labels);
			labels.add(tsin.dflt);
			
			/* Validate to make sure the target is in the table. */
			for(LabelNode l : labels) {
				FlowBlock caseBlock = graph.findTarget(l);
				if(caseBlock.equals(target)) {
					if(immediate) {
						return SuccessorType.IMMEDIATE;
					} else {
						return SuccessorType.FAR_JUMP;
					}
				}
			}
		} else if(InstructionUtil.isExit(lastOpcode)) {
			return SuccessorType.EXIT;
		}

		if(immediate) {
			return SuccessorType.IMMEDIATE;
		}

		System.err.println(b.toVerboseString(graph.labels()));
		System.err.println(target.toVerboseString(graph.labels()));

		throw new RuntimeException(index + " " + targetIndex);
	}

	/**
	 * Returns the Successor relationship that links the block and the
	 * successor FlowBlock.
	 * 
	 * @param block
	 * @param successor
	 * @return
	 */
	public Successor findRelationship(FlowBlock block, FlowBlock successor) {
		List<Successor> successors = tree.get(block);
		if(successors == null || successors.isEmpty())
			return null;

		for(Successor s : successors) {
			if(s.block().equals(successor))
				return s;
		}

		System.out.println("No succ for " + block);
		return null;
	}

	public List<Successor> get(FlowBlock b) {
		return tree.get(b);
	}

	public void release() {
		tree.clear();
	}

	public Map<FlowBlock, List<Successor>> tree() {
		return tree;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Entry<FlowBlock, List<Successor>> e : tree.entrySet()) {
			for(Successor s : e.getValue()) {
				sb.append(s).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public List<Successor> collapsedList() {
		List<Successor> list = new ArrayList<Successor>();
		for(List<Successor> succs : tree.values()) {
			list.addAll(succs);
		}
		return list;
	}

	@Override
	public Iterator<Successor> iterator() {
		//TODO: Make DFS iterable.
		List<Successor> list = new ArrayList<Successor>();
		for(List<Successor> succs : tree.values()) {
			list.addAll(succs);
		}
		return list.iterator();
	}

	public static enum SuccessorType {
		DEAD(), EXCEPTION(), IMMEDIATE(), EXIT(), EMPTY_GOTO(), FAR_JUMP();
	}

	public static class Successor {
		private final FlowBlock parent;
		private final FlowBlock block;
		private final SuccessorType type;

		public Successor(FlowBlock parent, FlowBlock block, SuccessorType type) {
			this.parent = parent;
			this.block = block;
			this.type = type;
		}

		public FlowBlock parent() {
			return parent;
		}

		public FlowBlock block() {
			return block;
		}

		public SuccessorType type() {
			return type;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(parent.id());

			sb.append(" -> ").append(block.id());
			sb.append(" (").append(type == null ? "null" : type.name().toLowerCase());
			if(type != SuccessorType.EXCEPTION)
				sb.append(", ").append(parent.lastOpcode() == -1 ? "-1" : Printer.OPCODES[parent.lastOpcode()]);
			sb.append(")");

			return sb.toString();
		}
	}

	/*public static class Pair<T> {
		private final T item1;
		private final T item2;

		public Pair(T item1, T item2) {
			this.item1 = item1;
			this.item2 = item2;
		}

		public T item1() {
			return item1;
		}

		public T item2() {
			return item2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((item1 == null) ? 0 : item1.hashCode());
			result = prime * result + ((item2 == null) ? 0 : item2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair<?> other = (Pair<?>) obj;
			if (item1 == null) {
				if (other.item1 != null)
					return false;
			} else if (!item1.equals(other.item1))
				return false;
			if (item2 == null) {
				if (other.item2 != null)
					return false;
			} else if (!item2.equals(other.item2))
				return false;
			return true;
		}
	}*/
}