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
	private final List<String> types;
	
	public ExceptionData(FlowBlock handler, List<FlowBlock> range, List<String> types) {
		this.handler = handler;
		this.range = range;
		this.types = types;
	}
	
	public ExceptionData(FlowBlock handler) {
		this.handler = handler;
		this.range   = new ArrayList<FlowBlock>();
		this.types   = new ArrayList<String>();
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
	
	public List<String> types() {
		return types;
	}

	public boolean isCircular() {
		return range.contains(handler);
	}

	@Override
	public String toString() {
		return "ExceptionData [handler=" + handler + ", range=" + range.size() + ", types=" + types + "]";
	}
}