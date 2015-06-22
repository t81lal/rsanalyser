package org.nullbool.pi.core.hook.serimpl;

import java.io.DataOutputStream;
import java.io.IOException;

public class StringEntry_0 extends PoolEntry {

	private final String string;
	
	public StringEntry_0(Pool pool, String string) {
		super(pool, 0);
		this.string = string;
	}

	@Override
	public void write(DataOutputStream dos) throws IOException {
		super.write(dos); 
		byte[] bytes = string.getBytes();
		dos.writeInt(bytes.length);
		dos.write(bytes);
	}
	
	public String value(){
		return string;
	}
	
	@Override
	public String toString() {
		return value();
	}
}