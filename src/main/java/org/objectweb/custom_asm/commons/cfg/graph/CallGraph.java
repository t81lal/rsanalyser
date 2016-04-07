package org.objectweb.custom_asm.commons.cfg.graph;

import org.objectweb.custom_asm.Handle;

/**
 * @author Tyler Sedlar
 */
public class CallGraph extends Digraph<Handle, Handle> {

    public void addMethodCall(Handle source, Handle target) {
        addVertex(target);
        addEdge(source, target);
    }
}
