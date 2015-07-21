/**
 * 
 */
package org.nullbool.api.obfuscation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jun 2015 00:09:37
 */
public class RedunantGotoTransformer implements Opcodes {

	private int gotos = 0;
	private int brem = 0;
	
	public void restructure(MethodNode m, IControlFlowGraph graph) {
		
		
		if(m.owner.name.equals("ck") && m.name.equals("c") && m.desc.startsWith("(Ljava/lang/CharSequence")) {

//			System.out.println(graph.toString());
		}
//		if(m.name.equals("<init>") && m.owner.name.equals("ao")) {
//			System.out.println("RedunantGotoTransformer.restructure()");
//			System.out.println(((SaneControlFlowGraph) graph).toString(graph.blocks()));
//		}
		
		
		Set<FlowBlock> removed = new HashSet<FlowBlock>();

		List<FlowBlock> blocks = graph.blocks();
		for(int i=0; i < blocks.size(); i++) {
			FlowBlock b = blocks.get(i);
			if(m.owner.name.equals("ck") && m.name.equals("c") && m.desc.startsWith("(Ljava/lang/CharSequence")) {
//				System.out.printf("size=%d(%d), last=%s, plen=%d, eplen=%d.%n", b.size(), b.cleansize(), b.lastOpcode() != -1 ? Printer.OPCODES[b.lastOpcode()] : b.last() != null ? b.last().getClass().getSimpleName() : "null", b.predecessors().size(), b.exceptionPredecessors().size());
			}
			if(((i + 1) < blocks.size()) && b.lastOpcode() == GOTO) {
				FlowBlock n = blocks.get(i + 1);
				if(n == null || b.target() == null) {
					throw new RuntimeException();
				}
				
				if(n == b.target()) {
					
					if(m.owner.name.equals("ck") && m.name.equals("c") && m.desc.startsWith("(Ljava/lang/CharSequence")) {
//						System.out.println("its " + n.predecessors().size());
					}
					
					if((n.predecessors().size()) <= 1) {
						// L1: goto L2
						// L2: ...

						AbstractInsnNode insn = b.last();
						b.insns().remove(insn);
						m.instructions.remove(insn);

						gotos++;
						
						if(m.owner.name.equals("ck") && m.name.equals("c") && m.desc.startsWith("(Ljava/lang/CharSequence")) {
//							System.out.println("removing");
						}
						
						if(b.size() == 0) {
							removed.add(b);
							brem++;
						}
					}
				}
			}
		}
	}

	public void output() {
		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.printf("Redundant GOTO remover.%n");
			System.out.printf("   Removed %d redundant gotos.%n", gotos);
			System.out.printf("   Removed %d empty blocks.%n", brem);
		}
	}
}