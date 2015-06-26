package org.nullbool.api.rs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;
import org.topdank.banalysis.filter.InstructionFilter;

/**
 * @author Bibl (don't ban me pls)
 * @created 23 Jun 2015 20:01:43
 */
public class CaseAnalyser implements Opcodes {

	private final TreeBuilder tb;
	private final InstructionPattern pattern;
	private final NullPermeableHashMap<Integer, Set<FlowBlock>> found;
	private int count = 0;
	private MethodNode method;

	public CaseAnalyser() {
		tb = new TreeBuilder();

		// opcode loading mechanism
		InstructionFilter[] filters = InstructionPattern.translate(new AbstractInsnNode[]{
				new VarInsnNode(ALOAD, 9),
				new IincInsnNode(8, 1),
				new VarInsnNode(ILOAD, 8),
				new InsnNode(IALOAD),
				new VarInsnNode(ISTORE, 11)
		});
		pattern = new InstructionPattern(filters);
		found = new NullPermeableHashMap<Integer, Set<FlowBlock>>(new ValueCreator<Set<FlowBlock>>() {
			@Override
			public Set<FlowBlock> create() {
				return new HashSet<FlowBlock>();
			}
		});
	}

	public boolean analyse(MethodNode m, IControlFlowGraph graph) {
		InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
		if(searcher.search()) {
			method = m;
			Iterator<FlowBlock> it = graph.iterator();
			while(it.hasNext()) {
				FlowBlock block = it.next();
				NodeVisitor nv = new NodeVisitor(){
					@Override
					public void visitJump(JumpNode jn) {
						NumberNode nn = jn.firstNumber();
						VariableNode vn = jn.firstVariable();
						if((jn.opcode() == IF_ICMPEQ || jn.opcode() == IF_ICMPNE) && nn != null && vn != null) {
							if(vn.var() == 11) {
								count++;
								found.getNonNull(nn.number()).add(block);
							}
						}
					}
				};

				tb.build(m, block).accept(nv);
			}

//			if(Context.current().getFlags().getOrDefault("out", true)) {
				System.out.printf("Total of %d opcode checks.%n", count);
				System.out.printf("Collected %d different entries.%n", found.size());
//			}
			
			return true;
		}
		return false;
	}
	
	public MethodNode getMethod() {
		return method;
	}

	public Set<FlowBlock> findBlock(int opcode) {
		return found.get(opcode);
	}
}