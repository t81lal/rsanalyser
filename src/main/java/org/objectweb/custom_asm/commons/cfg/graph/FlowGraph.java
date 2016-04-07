package org.objectweb.custom_asm.commons.cfg.graph;

import org.objectweb.custom_asm.commons.cfg.Block;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Tyler Sedlar
 */
public class FlowGraph extends Digraph<Block, Block> {

    private final MethodNode mn;

    public FlowGraph(MethodNode mn) {
        this.mn = mn;
    }

    public MethodNode method() {
        return mn;
    }
}
