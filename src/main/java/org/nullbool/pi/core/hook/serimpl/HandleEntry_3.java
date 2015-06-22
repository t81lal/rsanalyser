package org.nullbool.pi.core.hook.serimpl;

import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Handle;

public class HandleEntry_3 extends PoolEntry {

	private final Handle handle;
	private final int index1, index2, index3;
	
	public HandleEntry_3(Pool pool, Handle handle) {
		super(pool, 3);
		this.handle = handle;
		
		index1 = pool.allocateString(handle.getOwner());
		index2 = pool.allocateString(handle.getName());
		index3 = pool.allocateString(handle.getDesc());
	}
	
	public Handle getHandle(){
		return handle;
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException{
		dos.writeInt(handle.getTag());
		dos.writeInt(index1);
		dos.writeInt(index2);
		dos.writeInt(index3);
	}
}