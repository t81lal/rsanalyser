package org.nullbool.api.obfuscation.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bibl (don't ban me pls)
 * @created 26 May 2015
 */
public class ExceptionData {

	private final FlowBlock handler;
	private final List<FlowBlock> range;
	
	public ExceptionData(FlowBlock handler, List<FlowBlock> range) {
		this.handler = handler;
		this.range = range;
	}
	
	public ExceptionData(FlowBlock handler) {
		this.handler = handler;
		this.range   = new ArrayList<FlowBlock>();
	}
	
	public FlowBlock handler(){
		return handler;
	}
	
	public void addToRange(FlowBlock block) {
		if(!range.contains(block)) {
			range.add(block);
		}
	}
	
	public List<FlowBlock> range() {
		return range;
	}
}