package org.zbot.hooks.serialisation.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.DynamicDesc;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.ObfuscatedData;
import org.zbot.hooks.MethodHook.MethodType;
import org.zbot.hooks.serialisation.CharsEntry_2;
import org.zbot.hooks.serialisation.DescEntry_1;
import org.zbot.hooks.serialisation.IMapDeserialiser;
import org.zbot.hooks.serialisation.Pool;
import org.zbot.hooks.serialisation.StringEntry_0;

/**
 * @author Bibl (don't ban me pls)
 * @created 16 Jun 2015 12:45:55
 */
public class MapDeserialiserImpl implements IMapDeserialiser {

	/* (non-Javadoc)
	 * @see org.nullbool.zbot.pi.core.hook.serialisation.IMapDeserialiser#deserialise(java.io.InputStream)
	 */
	@Override
	public HookMap deserialise(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		int ver = dis.readInt();
		Pool pool = new Pool(dis);
		HookMap map = new HookMap(ver);
		
		int c_count = dis.readUnsignedShort();
		
		for(int i=0; i < c_count; i++) {
			
			String[] cn = readObfRefType(dis, pool);
			ClassHook ch = new ClassHook(cn[0], cn[1]);
			
			int f_count = dis.readUnsignedShort();
			
			for(int j=0; j < f_count; j++) {
				String[] fn = readObfRefType(dis, pool);
				DynamicDesc desc = pool.<DescEntry_1>get(dis.readInt()).getDesc();
				boolean stat = dis.readBoolean();
				long multi = dis.readLong();
				
				ch.getFields().add(new FieldHook(ch, new ObfuscatedData(fn[0], fn[1]), desc, stat, multi));
			}
			
			int m_count = dis.readUnsignedShort();
			
			for(int j=0; j < m_count; j++) {
				MethodType type = MethodType.values()[dis.readByte()];
				String[] mn = readObfRefType(dis, pool);
				DynamicDesc desc = pool.<DescEntry_1>get(dis.readInt()).getDesc();
				boolean stat = dis.readBoolean();
				int maxs = dis.readInt();
				int maxl = dis.readInt();
				
				InsnList insns = readCode(dis, pool);
				
				MethodHook mh = new MethodHook(type, ch, new ObfuscatedData(mn[0], mn[1]), desc, stat, insns);
				mh.setMaxStack(maxs);
				mh.setMaxLocals(maxl);
				
				ch.getMethods().add(mh);
			}
			
			map.getClasses().add(ch);
		}
		
		return map;
	}
	
	private InsnList readCode(DataInputStream dis, Pool pool) throws IOException {
		InsnList list = new InsnList();
		
		int size = dis.readInt();
		if(size == 0)
			return list;
		
		int lcount = dis.readInt();
		Map<Integer, LabelNode> labels = new HashMap<Integer, LabelNode>();
		for(int i=0; i < lcount; i++) {
			labels.put(i, new LabelNode());
		}
		
		for(int i=0; i < size; i++) {
			AbstractInsnNode ain = readInstruction(dis, pool, labels);
			if(ain != null)
				list.add(ain);
		}
		
		return list;
	}
	
	private AbstractInsnNode readInstruction(DataInputStream dis, Pool pool, Map<Integer, LabelNode> labels) throws IOException {
		byte type = dis.readByte();
		int opcode = dis.readUnsignedShort();
		
		switch(type) {
			case AbstractInsnNode.INSN: {
				return new InsnNode(opcode);
			}
			case AbstractInsnNode.INT_INSN: {
				int operand = dis.readInt();
				return new IntInsnNode(opcode, operand);
			}
			case AbstractInsnNode.VAR_INSN: {
				int local = dis.readInt();
				return new VarInsnNode(opcode, local);
			}
			case AbstractInsnNode.TYPE_INSN: {
				int index = dis.readInt();
				String desc = pool.<StringEntry_0>get(index).value();
				return new TypeInsnNode(opcode, desc);
			}
			case AbstractInsnNode.FIELD_INSN: {
				int oIndex = dis.readInt();
				int nIndex = dis.readInt();
				int dIndex = dis.readInt();
				String o = pool.<StringEntry_0>get(oIndex).value();
				String n = pool.<StringEntry_0>get(nIndex).value();
				String d = pool.<StringEntry_0>get(dIndex).value();
				return new FieldInsnNode(opcode, o, n, d);
			}
			case AbstractInsnNode.METHOD_INSN: {
				int oIndex = dis.readInt();
				int nIndex = dis.readInt();
				int dIndex = dis.readInt();
				if(pool.get(oIndex) == null)
					System.out.println(oIndex);
				String o = pool.<StringEntry_0>get(oIndex).value();
				String n = pool.<StringEntry_0>get(nIndex).value();
				String d = pool.<StringEntry_0>get(dIndex).value();
				return new MethodInsnNode(opcode, o, n, d, opcode == Opcodes.INVOKEINTERFACE);
			}
			case AbstractInsnNode.JUMP_INSN: {
				int index = dis.readUnsignedShort();
				LabelNode label = labels.get(index);
				return new JumpInsnNode(opcode, label);
			}
			case AbstractInsnNode.LABEL: {
				int index = dis.readUnsignedShort();
				LabelNode label = labels.get(index);
				return label;
			}
			case AbstractInsnNode.LDC_INSN: {
				byte t = dis.readByte();
				Object cst = null;
				switch(t) {
					case 0: {
						cst = dis.readFloat();
						break;
					}
					case 1: {
						cst = dis.readDouble();
						break;
					}
					case 2: {
						cst = dis.readLong();
						break;
					}
					case 3: {
						cst = pool.<StringEntry_0>get(dis.readInt());
						break;
					}
					case 4: {
						cst = dis.readInt();
						break;
					}
					case 5: {
						int sort = dis.readShort();
						char[] chars = pool.<CharsEntry_2>get(dis.readInt()).getChars();
						int off = dis.readInt();
						int len = dis.readInt();
						cst = new Type(sort, chars, off, len);
					}
					default: {
						throw new IOException("Unknown ldc type: " + t);
					}
				}
				
				return new LdcInsnNode(cst);
			}
			case AbstractInsnNode.IINC_INSN: {
				int var = dis.readShort();
				int inc = dis.readShort();
				return new IincInsnNode(var, inc);
			}
			case AbstractInsnNode.TABLESWITCH_INSN: {
				int min = dis.readShort();
				int max = dis.readShort();
				LabelNode dflt = labels.get(dis.readShort());
				TableSwitchInsnNode tsin = new TableSwitchInsnNode(min, max, dflt, new LabelNode[0]);
				for(int i=0; i < dis.readShort(); i++) {
					tsin.labels.add(labels.get(dis.readShort()));
				}
				return tsin;
			}
			case AbstractInsnNode.LOOKUPSWITCH_INSN: {
				LabelNode dflt = labels.get(dis.readShort());
				LookupSwitchInsnNode lsin = new LookupSwitchInsnNode(dflt, new int[0], new LabelNode[0]);
				for(int i=0; i < dis.readShort(); i++) {
					int key = dis.readInt();
					LabelNode l = labels.get(dis.readShort());
					lsin.keys.add(key);
					lsin.labels.add(l);
				}
				return lsin;
			}
			case AbstractInsnNode.MULTIANEWARRAY_INSN: {
				int dims = dis.readShort();
				String desc = pool.<StringEntry_0>get(dis.readInt()).value();
				MultiANewArrayInsnNode in = new MultiANewArrayInsnNode(desc, dims);
				return in;
			}
			case AbstractInsnNode.FRAME: {
				// ignore
				//FIXME;
				return null;
			}
			case AbstractInsnNode.LINE: {
				int line = dis.readInt();
				LabelNode l = labels.get(dis.readShort());
				return new LineNumberNode(line, l);
			}
			default: {
				throw new IOException("Invalid type: " + type);
			}
		}
	}
	
	private String[] readObfRefType(DataInputStream dis, Pool pool) throws IOException {
		int oIndex = dis.readInt();
		int rIndex = dis.readInt();	
		String o   = pool.<StringEntry_0> get(oIndex).value();
		String r   = pool.<StringEntry_0> get(rIndex).value();
		return new String[]{o, r};
	}
}