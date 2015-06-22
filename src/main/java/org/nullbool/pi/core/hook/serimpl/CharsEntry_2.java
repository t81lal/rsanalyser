package org.nullbool.pi.core.hook.serimpl;

import java.io.DataOutputStream;
import java.io.IOException;

public class CharsEntry_2 extends PoolEntry {

	private final char[] chars;
	
	public CharsEntry_2(Pool pool, char[] chars) {
		super(pool, 2);
		this.chars = chars;
	}

	public char[] getChars() {
		return chars;
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException {
		super.write(dos);
		dos.writeInt(chars.length);
		for(char c : chars) {
			dos.writeChar(c);
		}
	}
}