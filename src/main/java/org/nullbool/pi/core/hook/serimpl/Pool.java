package org.nullbool.pi.core.hook.serimpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Handle;

public class Pool {

	private Map<PoolEntry, Integer> pool;
	private Map<Integer, PoolEntry> reversePool;
	
	public Pool() {
		pool = new LinkedHashMap<PoolEntry, Integer>();
	}
	
	public Pool(DataInputStream dis) throws IOException {
		int size = dis.readInt();
		pool = new LinkedHashMap<PoolEntry, Integer>(size);

		for(int i=0; i < size; i++) {
			PoolEntry next = create(dis);
			pool.put(next, i);
		}
		
		//System.out.println("size: " + pool.size());
	}
	
	public Map<Integer, PoolEntry> reversePool() {
		if(reversePool == null || reversePool.size() != pool.size()) {
			reversePool = new LinkedHashMap<Integer, PoolEntry>();
			for(Entry<PoolEntry, Integer> e : pool.entrySet()) {
				reversePool.put(e.getValue(), e.getKey());
			}
		}
		
		return reversePool;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PoolEntry> T get(int index) {
		return (T) reversePool().get(index);
	}
	
	public int allocateNext(PoolEntry e) {
		int index = pool.size();
		pool.put(e, index);
		return index;
	}
	
	public int findChars(char[] chars) {
		for(Entry<PoolEntry, Integer> e : pool.entrySet()) {
			PoolEntry en = e.getKey();
			if(en instanceof CharsEntry_2) {
				CharsEntry_2 c = (CharsEntry_2) en;
				if(c.getChars().length == chars.length) {
					char[] chars2 = c.getChars();
					if(Arrays.equals(chars, chars2))
						return e.getValue();
				}
			}
		}
		return -1;
	}
	
	public int allocateChars(char[] chars) {
		int index = findChars(chars);
		if(index == -1) {
			return allocateNext(new CharsEntry_2(this, chars));
		} else {
			return index;
		}
	}
	
	public int findString(String s) {
		for(Entry<PoolEntry, Integer> e : pool.entrySet()) {
			PoolEntry en = e.getKey();
			if(en instanceof StringEntry_0) {
				StringEntry_0 se = (StringEntry_0) en;
				if(s.equals(se.value()))
					return e.getValue();
			}
		}
		return -1;
	}
	
	public int allocateString(String s) {
		int index = findString(s);
		if(index == -1) {
			return allocateNext(new StringEntry_0(this, s));
		} else {
			return index;
		}
	}
	
	public int findHandle(int tag, String owner, String name, String desc) {
		for(Entry<PoolEntry, Integer> e : pool.entrySet()) {
			PoolEntry en = e.getKey();
			if(en instanceof HandleEntry_3) {
				HandleEntry_3 se = (HandleEntry_3) en;
				Handle h = se.getHandle();
				if(h.getTag() == tag && h.getOwner().equals(owner) && h.getName().equals(name) && h.getDesc().equals(desc))
					return e.getValue();
		
			}
		}
		return -1;
	}
	
	public int write(DataOutputStream dos) throws IOException {
		dos.writeInt(pool.size());
		for(PoolEntry entry : pool.keySet()) {
			entry.write(dos);
		}
		return pool.size();
	}
	
	public PoolEntry create(DataInputStream dis) throws IOException {
		int tag = dis.readUnsignedByte();
		switch(tag) {
			case 0: {
				int length = dis.readInt();
				byte[] bytes = new byte[length];
				dis.read(bytes);
				String name = new String(bytes);
				return new StringEntry_0(this, name);
			}
			//case 1: {
			//	boolean method = dis.readBoolean();
			//	int index = dis.readInt();
			//	String desc = ((StringEntry_0) reversePool().get(index)).value();
			//	return new DescEntry_1(this, new DynamicDesc(desc, method));
			//}
			default: {
				throw new IOException(String.format("Invalid tag: %d.", tag));
			}
		}
	}
}