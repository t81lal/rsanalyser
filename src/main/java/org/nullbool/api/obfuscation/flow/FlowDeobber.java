package org.nullbool.api.obfuscation.flow;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.Block;
import org.objectweb.asm.commons.cfg.graph.Digraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
public class FlowDeobber implements Opcodes {
	
	private MethodNode method;
    private Block current = new Block(new Label());
    public final List<Block> blocks = new ArrayList<>();
    public final Digraph<Block, Block> graph = new Digraph<>();
    
	public void accept(MethodNode m) {
        current = new Block(new Label());
        blocks.clear();
        blocks.add(current);
        graph.flush();
		this.method = m;
		
		analyse();
	}
	
	private void analyse() {
		for(AbstractInsnNode ain : method.instructions.toArray()) {
			switch(ain.type()) {
				case INSN:
					visitInsn((InsnNode) ain);
					break;
				case INT_INSN:
					visitIntInsn((IntInsnNode) ain);
					break;
				case VAR_INSN:
					visitVarInsn((VarInsnNode) ain);
					break;
				case TYPE_INSN:
					visitTypeInsn((TypeInsnNode) ain);
					break;
				case FIELD_INSN:
					visitFieldInsn((FieldInsnNode) ain);
					break;
				case METHOD_INSN:
					visitMethodInsn((MethodInsnNode) ain);
					break;
				case INVOKE_DYNAMIC_INSN:
					visitInvokeDynamicInsn((InvokeDynamicInsnNode) ain);
					break;
				case JUMP_INSN:
					visitJumpInsn((JumpInsnNode) ain);
					break;
				case LABEL:
					visitLabel(((LabelNode) ain).getLabel());
					break;
				case LDC_INSN:
					visitLdcInsn((LdcInsnNode) ain);
					break;
				case IINC_INSN:
					visitIincInsn((IincInsnNode) ain);
					break;
				case TABLESWITCH_INSN:
					visitTableSwitchInsn((TableSwitchInsnNode) ain);
					break;
				case LOOKUPSWITCH_INSN:
					visitLookupSwitchInsn((LookupSwitchInsnNode) ain);
					break;
				case MULTIANEWARRAY_INSN:
					visitMultiANewArrayInsn((MultiANewArrayInsnNode) ain);
					break;
			}
		}
		
		visitEnd();
	}
	
    public void visitInsn(InsnNode in) {
        current.instructions.add(in);
        switch (in.opcode()) {
            case RETURN:
            case IRETURN:
            case ARETURN:
            case FRETURN:
            case DRETURN:
            case LRETURN:
            case ATHROW: {
                current = construct(new LabelNode(new Label()), false);
                break;
            }
        }
    }

    public void visitIntInsn(IntInsnNode iin) {
        current.instructions.add(iin);
    }

    public void visitVarInsn(VarInsnNode vin) {
        current.instructions.add(vin);
    }

    public void visitTypeInsn(TypeInsnNode tin) {
        current.instructions.add(tin);
    }

    public void visitFieldInsn(FieldInsnNode fin) {
        current.instructions.add(fin);
    }

    public void visitMethodInsn(MethodInsnNode min) {
        current.instructions.add(min);
    }

    public void visitInvokeDynamicInsn(InvokeDynamicInsnNode idin) {
        current.instructions.add(idin);
    }

    public void visitJumpInsn(JumpInsnNode jin) {
        int opcode = jin.opcode();
        current.target = construct(jin.label);
        current.target.preds.add(current.target);
        if (opcode != GOTO)
            current.instructions.add(jin);
        Stack<AbstractInsnNode> stack = current.stack;
        current = construct(new LabelNode(new Label()), opcode != GOTO);
        current.stack = stack;
    }

    public void visitLabel(Label label) {
        if (label == null || label.info == null) return;
        Stack<AbstractInsnNode> stack = current == null ? new Stack<AbstractInsnNode>() : current.stack;
        current = construct(new LabelNode(label));
        current.stack = stack;
    }

    public void visitLdcInsn(LdcInsnNode ldc) {
        current.instructions.add(ldc);
    }

    public void visitIincInsn(IincInsnNode iin) {
        current.instructions.add(iin);
    }

    public void visitTableSwitchInsn(TableSwitchInsnNode tsin) {
        construct(tsin.dflt);
        constructAll(tsin.labels);
        current.instructions.add(tsin);
    }

    public void visitLookupSwitchInsn(LookupSwitchInsnNode lsin) {
        construct(lsin.dflt);
        constructAll(lsin.labels);
        current.instructions.add(lsin);
    }

    public void visitMultiANewArrayInsn(MultiANewArrayInsnNode manain) {
        current.instructions.add(manain);
    }
    
    public void visitEnd() {
        List<Block> empty = new ArrayList<>();
        for (Block block : blocks) {
            block.owner = method;
            if (block.isEmpty())
                empty.add(block);
        }
        blocks.removeAll(empty);
        Collections.sort(blocks, new Comparator<Block>() {
            @Override
			public int compare(Block b1, Block b2) {
                return method.instructions.indexOf(new LabelNode(b1.label)) - method.instructions.indexOf(new LabelNode(b2.label));
            }
        });
        for (Block block : blocks) {
            block.setIndex(blocks.indexOf(block));
            if (!graph.containsVertex(block))
                graph.addVertex(block);
            if (block.target != null && block.target != block) {
                if (!graph.containsVertex(block.target))
                    graph.addVertex(block.target);
                graph.addEdge(block, block.target);
            }
            if (block.next != null) {
                if (!graph.containsVertex(block.next))
                    graph.addVertex(block.next);
                graph.addEdge(block, block.next);
            }
        }
    }
	
    /**
     * Constructs blocks for all given labels.
     *
     * @param labels The labels in which to construct blocks for.
     * @return The constructed blocks.
     */
    private Block[] constructAll(List<LabelNode> labels) {
        Block[] blocks = new Block[labels.size()];
        for (int i = 0; i < blocks.length; i++)
            blocks[i] = construct(labels.get(i));
        return blocks;
    }

    /**
     * Constructs a block for the given label.
     *
     * @param ln The label to get a block from.
     * @return The given label's block.
     */
    private Block construct(LabelNode ln) {
        return construct(ln, true);
    }

    /**
     * Constructs a block for the given label.
     *
     * @param ln The label to get a block from.
     * @param add <t>true</t> to add the block to the next preds, otherwise <t>false.</t>
     * @return A block for the given label.
     */
    private Block construct(LabelNode ln, boolean add) {
        Label label = ln.getLabel();
        if (!(label.info instanceof Block)) {
            label.info = new Block(label);
            if (add) {
                current.next = ((Block) label.info);
                current.next.preds.add(current.next);
            }
            blocks.add((Block) label.info);
        }
        return (Block) label.info;
    }
}