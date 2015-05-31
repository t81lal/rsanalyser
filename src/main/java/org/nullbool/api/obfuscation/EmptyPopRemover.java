package org.nullbool.api.obfuscation;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class EmptyPopRemover extends NodeVisitor {

	private int removed, unremovable, chosenUnremovable;
	
    @Override
	public void visit(AbstractNode n) {
    	if(n.opcode() == POP) {
    		if(n.children() > 0) {
    			AbstractNode child = n.child(0);
    			if(child instanceof NumberNode) {
    				child.delete();
    				n.delete();
    				removed++;
    			} else {
    				chosenUnremovable++;
    			}
    			//if(child instanceof ConstantNode || child || )
    		} else {
    			unremovable++;
    		}
    	}
    }
    
    public void output() {
    	System.err.println("Removing empty pop remover.");
    	System.out.printf("   Collapsed %d pops.%n", removed);
    	System.out.printf("   Was unable to remove %d pops.%n", unremovable);
    	System.out.printf("   Chose not to remove %d pops.%n", chosenUnremovable);
    }
}