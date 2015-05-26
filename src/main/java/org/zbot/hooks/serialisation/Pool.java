package org.zbot.hooks.serialisation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Handle;
import org.zbot.hooks.DynamicDesc;

public class Pool {

	private Map<PoolEntry, Integer> pool;
	private Map<Integer, PoolEntry> reversePool;
	
	public Pool() {
		pool = new HashMap<PoolEntry, Integer>();
	}
	
	public Pool(DataInputStream dis) throws IOException {
		int size = dis.readShort();
		pool = new HashMap<PoolEntry, Integer>(size);

		for(int i=0; i < size; i++) {
			PoolEntry next = create(dis);
			System.out.println("putting " + next.tag());
			pool.put(next, i);
		}
	}
	
	public Map<Integer, PoolEntry> reversePool() {
		if(reversePool == null || reversePool.size() != pool.size()) {
			reversePool = new HashMap<Integer, PoolEntry>();
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
	
	public int allocateDesc(DynamicDesc desc) {
		int index = findDesc(desc.getObfuscated(), desc.isMethod());
		if(index == -1) {
			return allocateNext(new DescEntry_1(this, desc));
		} else {
			return index;
		}
	}
	
	public int findDesc(String desc, boolean method) {
		for(Entry<PoolEntry, Integer> e : pool.entrySet()) {
			PoolEntry en = e.getKey();
			if(en instanceof DescEntry_1) {
				DescEntry_1 d = (DescEntry_1) en;
				if(d.getDesc().isMethod() == method && d.getDesc().getObfuscated().equals(desc))
					return e.getValue();
			}
		}
		return -1;
	}
	
//	h.getTag(), h.getOwner(), h.getName(), h.getDesc()
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
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(pool.size());
		for(PoolEntry entry : pool.keySet()) {
			System.out.println("writing " + entry.tag());
			entry.write(dos);
		}
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
			case 1: {
				boolean method = dis.readBoolean();
				int index = dis.readUnsignedShort();
				return new DescEntry_1(this, new DynamicDesc(((StringEntry_0) reversePool().get(index)).value(), method));
			}
			default: {
				throw new IOException(String.format("Invalid tag: %d.", tag));
			}
		}
	}
}