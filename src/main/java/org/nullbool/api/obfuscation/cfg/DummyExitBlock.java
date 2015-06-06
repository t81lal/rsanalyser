package org.nullbool.api.obfuscation.cfg;

/**
 * @author Bibl (don't ban me pls)
 * @created 5 Jun 2015 20:03:48
 */
public class DummyExitBlock extends FlowBlock {

	public DummyExitBlock(String id) {
		super(id);
	}
	
	public void unlink() {
		//TODO: Do we really need to check the list?
		for(FlowBlock pred : predecessors()) {
			if(pred.successors().contains(this))
				pred.successors().remove(this);
		}
		
		for(FlowBlock pred : exceptionPredecessors()) {
			if(pred.successors().contains(this))
				pred.successors().remove(this);
		}
	}
	
	public void relink() {
		//TOOD: Exception successors?
		
		for(FlowBlock pred : predecessors()) {
			if(!pred.successors().contains(this))
				pred.successors().add(this);
		}
		
		for(FlowBlock pred : exceptionPredecessors()) {
			if(!pred.successors().contains(this))
				pred.successors().add(this);
		}
	}
}