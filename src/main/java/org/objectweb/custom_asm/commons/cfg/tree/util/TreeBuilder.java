package org.objectweb.custom_asm.commons.cfg.tree.util;

import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.objectweb.custom_asm.Type;
import org.objectweb.custom_asm.commons.cfg.Block;
import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.commons.cfg.tree.node.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.custom_asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 */
public class TreeBuilder {

    public static final int[] CDS, PDS;

    static {
        CDS = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4, 3, 4, 3, 3, 3, 3, 1, 2, 1, 2, 3, 2, 3, 4, 2, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 2, 4, 1, 2, 1, 2, 2, 3, 2, 3, 2, 3, 2, 4, 2, 4, 2, 4, 0, 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 4, 2, 2, 4, 4, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0};
        PDS = new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 1, 1, 0, 0, 1, 2, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 4, 5, 6, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0, 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    public static TreeSize getTreeSize(org.objectweb.custom_asm.tree.AbstractInsnNode ain) {
        int c = 0, p = 0;
        if (ain instanceof org.objectweb.custom_asm.tree.InsnNode || ain instanceof org.objectweb.custom_asm.tree.IntInsnNode || ain instanceof org.objectweb.custom_asm.tree.VarInsnNode ||
                ain instanceof org.objectweb.custom_asm.tree.JumpInsnNode || ain instanceof org.objectweb.custom_asm.tree.TableSwitchInsnNode ||
                ain instanceof org.objectweb.custom_asm.tree.LookupSwitchInsnNode) {
            c = CDS[ain.getOpcode()];
            p = PDS[ain.getOpcode()];
        } else if (ain instanceof org.objectweb.custom_asm.tree.FieldInsnNode) {
            org.objectweb.custom_asm.tree.FieldInsnNode fin = (org.objectweb.custom_asm.tree.FieldInsnNode) ain;
            char d = fin.desc.charAt(0);
            switch (fin.getOpcode()) {
                case GETFIELD: {
                    c = 1;
                    p = d == 'D' || d == 'J' ? 2 : 1;
                    break;
                }
                case GETSTATIC: {
                    c = 0;
                    p = d == 'D' || d == 'J' ? 2 : 1;
                    break;
                }
                case PUTFIELD: {
                    c = d == 'D' || d == 'J' ? 3 : 2;
                    p = 0;
                    break;
                }
                case PUTSTATIC: {
                    c = d == 'D' || d == 'J' ? 2 : 1;
                    p = 0;
                    break;
                }
                default: {
                    c = 0;
                    p = 0;
                    break;
                }
            }
        } else if (ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
            org.objectweb.custom_asm.tree.MethodInsnNode min = (org.objectweb.custom_asm.tree.MethodInsnNode) ain;
            int as = Type.getArgumentsAndReturnSizes(min.desc);
            c = (as >> 2) - (min.getOpcode() == INVOKEDYNAMIC || min.getOpcode() == INVOKESTATIC ? 1 : 0);
            p = as & 0x03;
        } else if (ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode) {
            Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) ain).cst;
            p = cst instanceof Double || cst instanceof Long ? 2 : 1;
        } else if (ain instanceof org.objectweb.custom_asm.tree.MultiANewArrayInsnNode) {
            c = ((org.objectweb.custom_asm.tree.MultiANewArrayInsnNode) ain).dims;
            p = 1;
        }
        return new TreeSize(c, p);
    }

    private static AbstractNode createNode(org.objectweb.custom_asm.tree.AbstractInsnNode ain, NodeTree tree, TreeSize size) {
        int opcode = ain.getOpcode();
        if (ain instanceof org.objectweb.custom_asm.tree.IntInsnNode) {
            return new NumberNode(tree, ain, size.collapsing, size.producing);
        } else if (ain instanceof org.objectweb.custom_asm.tree.VarInsnNode) {
            return new VariableNode(tree, ain, size.collapsing, size.producing);
        } else if (ain instanceof org.objectweb.custom_asm.tree.JumpInsnNode) {
            JumpNode jumpNode = new JumpNode(tree, (org.objectweb.custom_asm.tree.JumpInsnNode) ain, size.collapsing, size.producing);
            jumpNode.setTarget(new TargetNode(tree,((org.objectweb.custom_asm.tree.JumpInsnNode) ain).label,size.collapsing,size.producing));
            return jumpNode;
        } else if (ain instanceof org.objectweb.custom_asm.tree.FieldInsnNode) {
            return new FieldMemberNode(tree, ain, size.collapsing, size.producing);
        } else if (ain instanceof org.objectweb.custom_asm.tree.MethodInsnNode) {
            return new MethodMemberNode(tree, ain, size.collapsing, size.producing);
        } else if (ain instanceof org.objectweb.custom_asm.tree.LdcInsnNode) {
            Object cst = ((org.objectweb.custom_asm.tree.LdcInsnNode) ain).cst;
            if (cst instanceof Number) {
                return new NumberNode(tree, ain, size.collapsing, size.producing);
            } else {
                return new ConstantNode(tree, ain, size.collapsing, size.producing);
            }
        } else if (ain instanceof org.objectweb.custom_asm.tree.IincInsnNode) {
            return new IincNode(tree, ain, size.collapsing, size.producing);
        } else if (ain instanceof org.objectweb.custom_asm.tree.TypeInsnNode) {
            return new TypeNode(tree, ain, size.collapsing, size.producing);
        } else {
            if (opcode >= ICONST_M1 && opcode <= DCONST_1) {
                return new NumberNode(tree, ain, size.collapsing, size.producing);
            } else if (opcode >= I2L && opcode <= I2S) {
                return new ConversionNode(tree, ain, size.collapsing, size.producing);
            } else if (opcode >= IADD && opcode <= LXOR) {
                return new ArithmeticNode(tree, ain, size.collapsing, size.producing);
            } else {
                return new AbstractNode(tree, ain, size.collapsing, size.producing);
            }
        }
    }

    private int treeIndex = -1;

    private AbstractNode iterate(List<AbstractNode> nodes) {
        if (treeIndex < 0) {
            return null;
        }
        AbstractNode node = nodes.get(treeIndex--);
        if (node.collapsed == 0) {
            return node;
        }
        int c = node.collapsed;
        while (c != 0) {
            AbstractNode n = iterate(nodes);
            if (n == null) {
                break;
            }
            int op = n.opcode();
            if (op == MONITOREXIT && node.opcode() == ATHROW)
                n.producing = 1;
            node.addFirst(n);
            int cr = c - n.producing;
            if (cr < 0) {
                node.producing += -cr;
                n.producing = 0;
                break;
            }
            c -= n.producing;
            n.producing = 0;
        }
        return node;
    }

    public long create = 0;
    public long iterate = 0;

    public NodeTree build(org.objectweb.custom_asm.tree.MethodNode mn) {
        NodeTree tree = new NodeTree(mn);
        List<AbstractNode> nodes = new ArrayList<>();
        long start = System.nanoTime();
        for (org.objectweb.custom_asm.tree.AbstractInsnNode ain : mn.instructions.toArray()) {
//        	if(ain == null) {
//        		System.out.println("NULL INSN NIGGER AT " + mn);
//        		System.exit(10);
//        	}
            if(ain.getOpcode() != -1) {
            	nodes.add(createNode(ain, tree, getTreeSize(ain)));
            }
        }
        long end = System.nanoTime();
        create += (end - start);
        treeIndex = nodes.size() - 1;
        AbstractNode node;
        start = System.nanoTime();
        while ((node = iterate(nodes)) != null)
            tree.addFirst(node);
        end = System.nanoTime();
        iterate += (end - start);
        return tree;
    }

    public NodeTree build(Block block) {
        NodeTree tree = new NodeTree(block);
        List<AbstractNode> nodes = new ArrayList<>();
        long start = System.nanoTime();
        for (org.objectweb.custom_asm.tree.AbstractInsnNode ain : block.instructions)
            if(ain.getOpcode() != -1) {
            	nodes.add(createNode(ain, tree, getTreeSize(ain)));
            }
        long end = System.nanoTime();
        create += (end - start);
        treeIndex = nodes.size() - 1;
        AbstractNode node;
        start = System.nanoTime();
        while ((node = iterate(nodes)) != null)
            tree.addFirst(node);
        end = System.nanoTime();
        iterate += (end - start);
        return tree;
    }
    
    public NodeTree build(org.objectweb.custom_asm.tree.MethodNode method, FlowBlock block) {
        NodeTree tree = new NodeTree(method);
        List<AbstractNode> nodes = new ArrayList<>();
        long start = System.nanoTime();
        for (org.objectweb.custom_asm.tree.AbstractInsnNode ain : block.insns())
            if(ain.getOpcode() != -1) {
            	nodes.add(createNode(ain, tree, getTreeSize(ain)));
            }
        long end = System.nanoTime();
        create += (end - start);
        treeIndex = nodes.size() - 1;
        AbstractNode node;
        start = System.nanoTime();
        while ((node = iterate(nodes)) != null)
            tree.addFirst(node);
        end = System.nanoTime();
        iterate += (end - start);
        return tree;
    }
}