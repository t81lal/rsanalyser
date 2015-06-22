//package org.nullbool.pi.core.hook.serimpl.legacy;
//
//import java.io.DataOutputStream;
//import java.io.IOException;
//
//import org.nullbool.pi.core.hook.serimpl.Pool;
//import org.nullbool.pi.core.hook.serimpl.PoolEntry;
//import org.nullbool.zbot.pi.core.hooks.api.DynamicDesc;
//
//public class DescEntry_1 extends PoolEntry {
//
//	private final DynamicDesc desc;
//	private final int index;
//	
//	public DescEntry_1(Pool pool, DynamicDesc desc) {
//		super(pool, 1);
//		this.desc = desc;
//		
//		index = pool.allocateString(desc.getObfuscated());		
//	}
//	
//	@Override
//	public void write(DataOutputStream dos) throws IOException{
//		super.write(dos);
//		/*write the type and then the obfuscated desc string entry index*/
//		dos.writeBoolean(desc.isMethod());
//		dos.writeInt  (index);
//	}
//
//	public DynamicDesc getDesc() {
//		return desc;
//	}
//	
//	@Override
//	public String toString() {
//		return desc.toString();
//	}
//}