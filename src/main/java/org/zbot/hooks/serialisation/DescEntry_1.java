package org.zbot.hooks.serialisation;

import java.io.DataOutputStream;
import java.io.IOException;

import org.zbot.hooks.DynamicDesc;

public class DescEntry_1 extends PoolEntry {

	private final DynamicDesc desc;
	private final int index;
	
	public DescEntry_1(Pool pool, DynamicDesc desc) {
		super(pool, 1);
		this.desc = desc;
		
		index = pool.allocateString(desc.getObfuscated());
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException{
		super.write(dos);
		/*write the type and then the obfuscated desc string entry index*/
		dos.writeBoolean(desc.isMethod());
		dos.writeShort  (index);
	}

	public DynamicDesc getDesc() {
		return desc;
	}
}