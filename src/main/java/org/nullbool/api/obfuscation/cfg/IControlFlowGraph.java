package org.nullbool.api.obfuscation.cfg;

import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 17 Jun 2015 01:07:27
 */
public abstract interface IControlFlowGraph extends Iterable<FlowBlock> {

	public abstract List<FlowBlock> blocks();
	
	public abstract Map<LabelNode, FlowBlock> labels();
	
	public abstract Map<String, FlowBlock> blockNames();
	
	public abstract List<ExceptionData> exceptions();
	
	public abstract FlowBlock entry();
	
	public abstract FlowBlock exit();
	
	public abstract FlowBlock findTarget(FlowBlock b);
	
	public abstract FlowBlock findTarget(LabelNode l);
	
	public abstract IControlFlowGraph create(MethodNode m) throws ControlFlowException;
	
	public abstract void destroy();

	public abstract boolean hasLoop();
}