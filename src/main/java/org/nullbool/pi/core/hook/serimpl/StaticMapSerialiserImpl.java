package org.nullbool.pi.core.hook.serimpl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.nullbool.pi.core.hook.api.serialisation.IMapSerialiser;
import org.objectweb.custom_asm.Type;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.IincInsnNode;
import org.objectweb.custom_asm.tree.InsnList;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.JumpInsnNode;
import org.objectweb.custom_asm.tree.LabelNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.LineNumberNode;
import org.objectweb.custom_asm.tree.LookupSwitchInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MultiANewArrayInsnNode;
import org.objectweb.custom_asm.tree.TableSwitchInsnNode;
import org.objectweb.custom_asm.tree.TypeInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 19 Jun 2015 12:12:36
 */
public class StaticMapSerialiserImpl implements IMapSerialiser<HookMap> {

	// TODO: varint?
	
	/* (non-Javadoc)
	 * @see org.nullbool.zbot.pi.core.hook.serialisation.IMapSerialiser#serialise(org.nullbool.zbot.pi.core.hook.HookMap, java.io.DataInputStream)
	 */
	@Override
	public void serialise(HookMap map, OutputStream os) throws IOException {
		ByteArrayOutputStream contentBoas = new ByteArrayOutputStream();
		DataOutputStream contentDos = new DataOutputStream(contentBoas);
		
		Pool pool = new Pool();
		
		List<ClassHook> classes = map.classes();
		int c_count = classes.size();
		contentDos.writeShort(c_count);
		
		for(ClassHook c : classes) {
			// TODO: interfaces
			writeObfRefType(contentDos, pool, c);
			serialiseMap(contentDos, pool, c.variables());
			
			List<FieldHook> fields = c.fields();
			contentDos.writeShort(fields.size());
			
			for(FieldHook f : fields) {
				writeObfRefType(contentDos, pool, f);
				serialiseMap(contentDos, pool, f.variables());
			}
			
			List<MethodHook> methods = c.methods();
			contentDos.writeShort(methods.size());
			
			for(MethodHook m : methods) {
				writeObfRefType(contentDos, pool, m);
				serialiseMap(contentDos, pool, m.variables());
				writeCode(contentDos, m.insns(), pool);
			}
		}
		
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(map.version());
		pool.write(dos);
		dos.write(contentBoas.toByteArray());
	}
	
	private void serialiseMap(DataOutputStream dos, Pool pool, Map<String, String> map) throws IOException {
		dos.writeInt(map.size());
		for(Entry<String, String> e : map.entrySet()) {
			dos.writeInt(pool.allocateString(e.getKey()));
			dos.writeInt(pool.allocateString(e.getValue()));
		}
	}
	
	private void writeObfRefType(DataOutputStream dos, Pool pool, ObfuscatedData d) throws IOException {
		dos.writeInt(pool.allocateString(d.obfuscated()));	
		dos.writeInt(pool.allocateString(d.refactored()));
	}
	
	private Map<LabelNode, Integer> mapLabels(AbstractInsnNode[] insns) throws IOException {
		Map<LabelNode, Integer> map = new HashMap<LabelNode, Integer>();
		int i = 0;
		for(AbstractInsnNode ain : insns) {
			if(ain instanceof LabelNode) {
				LabelNode labelNode = (LabelNode) ain;
				map.put(labelNode, i++);
			}
		}
		
		return map;
	}
	
	private void writeCode(DataOutputStream dos, InsnList insns, Pool pool) throws IOException {
		if(insns == null || insns.size() == 0) {
			dos.writeInt(0);
		} else {
			AbstractInsnNode[] arr = insns.toArray();
			Map<LabelNode, Integer> labels = mapLabels(arr);
			
			dos.writeInt(arr.length);
			dos.writeInt(labels.size());
			
			for(AbstractInsnNode ain : arr) {
				writeInstruction(dos, pool, labels, ain);
			}
		}
	}
	
	private void writeInstruction(DataOutputStream dos, Pool pool, Map<LabelNode, Integer> labels, AbstractInsnNode insn) throws IOException{		
		/* Write the instruction type, opcode and then the instructions
		 * specific data.
		 * 
		 * {
		 * 	  u1   type;
		 * 	  u2   opcode;
		 * 
		 * 	  ux   extra_data;
		 * }
		 */
		dos.writeByte(insn.getType());
		dos.writeShort(insn.getOpcode());
		
		switch(insn.getType()){
			case AbstractInsnNode.INSN:{
				//nothing else
				break;
			}
			case AbstractInsnNode.INT_INSN:{
				int operand = ((IntInsnNode) insn).operand;
				dos.writeInt(operand);
				break;
			}
			case AbstractInsnNode.VAR_INSN:{
				int local = ((VarInsnNode) insn).var;
				dos.writeInt(local);
				break;
			}
			case AbstractInsnNode.TYPE_INSN:{
				//pool index
				int descIndex = pool.allocateString(((TypeInsnNode) insn).desc);
				dos.writeInt(descIndex);
				break;
			}
			case AbstractInsnNode.FIELD_INSN:{
				//pool indices
				FieldInsnNode fin = (FieldInsnNode) insn;
				int ownerIndex = pool.allocateString(fin.owner);
				int nameIndex  = pool.allocateString(fin.name);
				int descIndex  = pool.allocateString(fin.desc);
				dos.writeInt(ownerIndex);
				dos.writeInt(nameIndex);
				dos.writeInt(descIndex);
				break;
			}
			case AbstractInsnNode.METHOD_INSN:{
				//pool indices
				MethodInsnNode min = (MethodInsnNode) insn;
				int ownerIndex = pool.allocateString(min.owner);
				int nameIndex  = pool.allocateString(min.name);
				int descIndex  = pool.allocateString(min.desc);
				dos.writeInt(ownerIndex);
				dos.writeInt(nameIndex);
				dos.writeInt(descIndex);
				break;
			}
			case AbstractInsnNode.INVOKE_DYNAMIC_INSN:{
				throw new IOException("Cannot serialise invokedynamics at the moment.");
			}
			case AbstractInsnNode.JUMP_INSN:{
				JumpInsnNode jin = (JumpInsnNode) insn;
				int index = labels.get(jin.label);
				dos.writeShort(index);
				break;
			}
			case AbstractInsnNode.LABEL:{
				LabelNode label = (LabelNode) insn;
				int index = labels.get(label);
				dos.writeShort(index);
				break;
			}
			case AbstractInsnNode.LDC_INSN:{
				LdcInsnNode ldc = (LdcInsnNode) insn;
				Object cst      = ldc.cst;
				/* 0 = float
				 * 1 = double
				 * 2 = long
				 * 3 = String
				 * 4 = Type
				 */
				if(cst instanceof Float) {
					dos.writeByte(0);
					dos.writeFloat((float) cst);
				} else if (cst instanceof Double) {
					dos.writeByte(1);
					dos.writeDouble((double) cst);
				} else if(cst instanceof Long) {
					dos.writeByte(2);
					dos.writeLong((long) cst);
				} else if (cst instanceof String) {
					dos.writeByte(3);
					int stringIndex = pool.allocateString((String) cst);
					dos.writeInt(stringIndex);
				} else if(cst instanceof Integer) {
					dos.writeByte(4);
					dos.writeInt((int)cst);
				} else if (cst instanceof Type) {
					Type type = (Type) cst;
					/* Types have:
					 *   int    sort
					 *   char[] buf
					 *   int    off
					 *   int    len
					 */
					dos.writeByte(5);
					dos.writeInt(type.getSort());
					int charsIndex = pool.allocateChars(type.getBuf());
					dos.writeInt(charsIndex);
					dos.writeInt(type.getOff());
					dos.writeInt(type.getLen());
				} else {
					throw new RuntimeException(String.format("Illegal type %s (%s)", cst, cst == null ? "NULL" : cst.getClass().getCanonicalName()));
				}
				break;
			}
			case AbstractInsnNode.IINC_INSN:{
				IincInsnNode inc = (IincInsnNode) insn;
				dos.writeShort(inc.var);
				dos.writeShort(inc.incr);
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN:{
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
				/*
				 * {
				 *   u2 min;
				 *   u2 max;
				 *   u2 default_label_index;
				 *   u2 label_count;
				 *   labels [label_count;
				 * }
				 */
				dos.writeShort(tsin.min);
				dos.writeShort(tsin.max);
				dos.writeShort(labels.get(tsin.dflt));
				dos.writeShort(tsin.labels.size());
				for(LabelNode n : tsin.labels) {
					dos.writeShort(labels.get(n));
				}
				break;
			}
			case AbstractInsnNode.LOOKUPSWITCH_INSN:{
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
				if(lsin.keys.size() != lsin.labels.size())
					throw new RuntimeException(String.format("Different lengths: %s %s", lsin.keys, lsin.labels));
				
				/*
				 * {
				 *   u2           default_label_index;
				 *   u2           key_labels_count
				 *   key_labels   [key_label_count];
				 * }
				 */
				
				/*
				 * key_label
				 * {
				 * 	 s4   key
				 *   u2   label_index
				 * }
				 */
				
				dos.writeShort(labels.get(lsin.dflt));
				
				dos.writeShort(lsin.keys.size());
				for(int i = 0; i < lsin.keys.size(); i++) {
					int key         = (Integer) lsin.keys.get(i);
					LabelNode label = lsin.labels.get(i);
					
					dos.writeInt(key);
					dos.writeShort(labels.get(label));
				}
				
				break;
			}
			case AbstractInsnNode.MULTIANEWARRAY_INSN:{
				MultiANewArrayInsnNode mnin = (MultiANewArrayInsnNode) insn;
				dos.writeShort(mnin.dims);
				dos.writeInt(pool.allocateString(mnin.desc));
				break;
			}
			case AbstractInsnNode.FRAME: {
				break;
			}
			case AbstractInsnNode.LINE: {
				LineNumberNode lnn = (LineNumberNode) insn;
				dos.writeInt(lnn.line);
				dos.writeShort(labels.get(lnn.start));
				break;
			}
		}
	}
}