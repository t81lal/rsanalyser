package org.nullbool.pi.core.hook.serimpl;

import java.io.DataOutputStream;
import java.io.IOException;

public class PoolEntry {

	protected final Pool pool;
	private final int tag;
	
	public PoolEntry(Pool pool, int tag) {
		this.pool = pool;
		this.tag = tag; 
	}
	
	public void write(DataOutputStream dos) throws IOException{
		dos.writeByte(tag); 
	}
	
	public int tag(){
		return tag;
	}
}