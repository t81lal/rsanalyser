package org.zbot.hooks;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.zbot.hooks.serialisation.Pool;
import org.zbot.hooks.serialisation.StringEntry_0;

public class HookMap implements Serializable {

	private static final long serialVersionUID = -2199521127019909810L;

	private static final int CURRENT_VERSION = 2;
	
	private final int version;
	private List<ClassHook> classes;

	public HookMap() {
		this(CURRENT_VERSION);
	}
	
	public HookMap(int ver) {
		this.version = ver;
		classes = new ArrayList<ClassHook>();
	}
	
	public HookMap(List<ClassHook> classes) {
		this(CURRENT_VERSION, classes);
	}

	public HookMap(int ver, List<ClassHook> classes) {
		this.version = ver;
		this.classes = classes;
	}

	public List<ClassHook> getClasses() {
		return classes;
	}

	public void setClasses(List<ClassHook> classes) {
		this.classes = classes;
	}
	
	public int getVersion(){
		return version;
	}

	public static HookMap read(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		int ver = dis.readInt();
		if(ver != CURRENT_VERSION) 
			throw new IOException(String.format("Invalid HookMap version: %d.", ver));
		
		Pool pool = new Pool(dis);
		
		HookMap map = new HookMap();
		
		int class_count = dis.readUnsignedShort();
		for(int i=0; i < class_count; i++) {
			ClassHook c = new ClassHook();
			String[] name = readObfRefType(dis, pool);
			c.setObfuscated(name[0]);
			c.setRefactored(name[1]);
			
			int field_count = dis.readUnsignedShort();
			for(int j=0; j < field_count; j++) {
				
			}
			
			int method_count = dis.readUnsignedShort();
			
			map.classes.add(c);
		}
		
//		writeObfRefType(dos, pool, c);
//		List<FieldHook> fields = c.getFields();
//		dos.writeShort(fields.size());
//		for(FieldHook f : fields) {
//			writeObfRefType(dos, pool, f.getName()); //obf and ref name
//			dos.writeShort(pool.allocateDesc(f.getDesc())); //desc
//			dos.writeBoolean(f.isStatic()); //static boolean
//			dos.writeLong(f.getMultiplier()); //multiplier
//		}
//		List<MethodHook> methods = c.getMethods();
//		dos.writeShort(methods.size());
//		for(MethodHook m : methods) {
//			dos.writeByte(m.getType().ordinal());
//			writeObfRefType(dos, pool, m.getName());
//			dos.writeShort(pool.allocateDesc(m.getDesc()));
//			dos.writeBoolean(m.isStatic());
//			dos.writeShort(m.getMaxStack());
//			dos.writeShort(m.getMaxLocals());
//			writeInsns(dos, pool, m.getInstructions());
//		}
		
		return map;
	}
	
	/**
	 * Converts this HookMap's contents into an internal format and writes it to the 
	 * {@link OutputStream} that is provided. <br>
	 * The current specification for the internal formatting is based loosely on the 
	 * JVM class file format. <br>
	 * <p>
	 * Data types are split into the following categories: <br>
	 * <p>
	 * 
	 * <style>
	 *    table, th, td {
	 * 	     border: 1px solid black;
	 * 	     border-collapse: collapse;
	 *    }
	 * 
	 *    th, td {
	 * 	     padding: 5px;
	 *    }
	 * </style>
	 *
	 * <table>
	 *  <tr>
	 *  	<th>Name</th>
	 *  	<th>Type</th>
	 *  	<th>Size (bytes)</th>
	 *  </tr>
	 * 
	 * 	<tr>
	 * 		<td>u1</td>
	 * 		<td>unsigned byte</td>
	 * 		<td>1</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>u2</td>
	 * 		<td>unsigned short</td>
	 * 		<td>2</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>u4</td>
	 * 		<td>unsigned int</td>
	 * 		<td>4</td>
	 * 	</tr>
	 * </table>
	 * 
	 * <p>
	 * The internal definition is structured as follows: <br>
	 * 
	 * <pre>
	 *{
	 *   u1      version;
	 *   u2      pool_size;
	 *   pool    [pool_size];
	 *   u2      class_size;
	 *   classes [class_size];
	 *}
	 * </pre>
	 * 
	 * The pool_size is positive whole number representing the size of the  array which 
	 * can be indexed from 0 as the first element to pool_size - 1 as the last element.
	 * <p>
	 * 
	 * The pool entry struct: <br>
	 * <pre>
	 *{
	 *   u1   tag;
	 *   u2   length;
	 *   u1   [length];
	 *}
	 * </pre>
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void write(OutputStream os) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		/*
		 * {
		 * 	  u1      version;
		 *    u2      class_count
		 *    classes [class_count];
		 * }
		 */
		
		/*
		 * class structure
		 *   {
		 *  	u2       obf_name_index;
		 *  	u2       ref_name_index;
		 *  	u2       field_size;
		 *  	fields   [field_size];
	 	 *  	u2       method_size;
	  	 *  	methods  [method_size];
		 *   }
		 *   
		 * field structure
		 *   {
		 *   	u2   obf_name_index;
		 *   	u2   ref_name_index;
		 *   	u2   desc_index;
		 *   	u1   is_static;
		 *   	u8   multiplier;
		 *   }
		 *   
		 * method structure
		 *   {
		 *   	u1   method_type_ordinal
		 *   	u2   obf_name_index;
		 *   	u2   ref_name_index;
		 *   	u2 	 desc_index; 
		 *   	u1   is_static;
		 *   	u2   max_stack;
		 *   	u2 	 max_locals;
		 *   	u2	 code_length;
		 *   	code [code_length];
		 *   }
		 */

		Pool pool = new Pool();
		dos.writeShort(classes.size());
		
		for(ClassHook c : classes) {
			writeObfRefType(dos, pool, c);
			
			List<FieldHook> fields = c.getFields();
			dos.writeShort(fields.size());
			
			for(FieldHook f : fields) {
				writeObfRefType(dos, pool, f.getName()); //obf and ref name
				dos.writeShort(pool.allocateDesc(f.getDesc())); //desc
				dos.writeBoolean(f.isStatic()); //static boolean
				dos.writeLong(f.getMultiplier()); //multiplier
			}
			
			List<MethodHook> methods = c.getMethods();
			dos.writeShort(methods.size());
			for(MethodHook m : methods) {
				dos.writeByte(m.getType().ordinal());
				writeObfRefType(dos, pool, m.getName());
				dos.writeShort(pool.allocateDesc(m.getDesc()));
				dos.writeBoolean(m.isStatic());
				dos.writeShort(m.getMaxStack());
				dos.writeShort(m.getMaxLocals());
				writeInsns(dos, pool, m.getInstructions());
			}
		}
		
		DataOutputStream dos1 = new DataOutputStream(os);
		dos1.writeInt(version);
		pool.write(dos1);
		dos1.write(bos.toByteArray());
	}
	
	private static void writeInsns(DataOutputStream dos, Pool pool, InsnList insns) throws IOException {
		/*
		 * {
		 * 	  u2   code_length
		 * 	  
		 *   if(code_length > 0)
		 *    u2   label_count
		 * }
		 */
		if(insns == null || insns.size() == 0){
			dos.writeShort(0);
		} else {
			dos.writeShort(insns.size());
			Map<LabelNode, Integer> labels = getLabels(insns.toArray());
			dos.writeShort(labels.size());
		}
	}
	
	private static Map<LabelNode, Integer> getLabels(AbstractInsnNode[] insns) throws IOException {
		Map<LabelNode, Integer> map = new HashMap<LabelNode, Integer>();
		int i = 0;
		for(AbstractInsnNode ain : insns) {
			if(ain instanceof LabelNode) {
				LabelNode labelNode = (LabelNode) ain;
//				Label         label = labelNode.getLabel();
//				if(label == null){
//					throw new RuntimeException("What the fuck, null label.");
//				}
				
				map.put(labelNode, i++);
			}
		}
		
		return map;
	}

	public static void writeInstruction(DataOutputStream dos, Pool pool, Map<LabelNode, Integer> labels, AbstractInsnNode insn) throws IOException{		
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
		dos.writeByte(insn.type());
		dos.writeShort(insn.opcode());
		
		switch(insn.type()){
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
				int descIndex = pool.findString(((TypeInsnNode) insn).desc);
				dos.writeShort(descIndex);
				break;
			}
			case AbstractInsnNode.FIELD_INSN:{
				//pool indices
				FieldInsnNode fin = (FieldInsnNode) insn;
				int ownerIndex = pool.findString(fin.owner);
				int nameIndex  = pool.findString(fin.name);
				int descIndex  = pool.findString(fin.desc);
				dos.writeShort(ownerIndex);
				dos.writeShort(nameIndex);
				dos.writeShort(descIndex);
				break;
			}
			case AbstractInsnNode.METHOD_INSN:{
				//pool indices
				MethodInsnNode min = (MethodInsnNode) insn;
				int ownerIndex = pool.findString(min.owner);
				int nameIndex  = pool.findString(min.name);
				int descIndex  = pool.findString(min.desc);
				dos.writeShort(ownerIndex);
				dos.writeShort(nameIndex);
				dos.writeShort(descIndex);
				break;
			}
			case AbstractInsnNode.INVOKE_DYNAMIC_INSN:{
				throw new IOException("Cannot serialise invokedynamics at the moment.");
//				InvokeDynamicInsnNode din = (InvokeDynamicInsnNode) insn;
//				int nameIndex   = pool.findString(din.name);
//				int descIndex   = pool.findString(din.desc);
//				int handleIndex = pool.findHandle(din.bsm);
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
					dos.writeShort(stringIndex);
				} else if (cst instanceof org.objectweb.asm.Type) {
					Type type = (Type) cst;
					/* Types have:
					 *   int    sort
					 *   char[] buf
					 *   int    off
					 *   int    len
					 */
					dos.writeByte(4);
					dos.writeInt(type.getSort());
					int charsIndex = pool.allocateChars(type.getBuf());
					dos.writeShort(charsIndex);
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
				dos.writeShort(pool.allocateString(mnin.desc));
				break;
			}
		}
	}
	
	private static void writeObfRefType(DataOutputStream dos, Pool pool, ObfuscatedData d) throws IOException {
		dos.writeShort(pool.allocateString(d.getObfuscated()));	
		dos.writeShort(pool.allocateString(d.getRefactored()));
	}
	
	private static String[] readObfRefType(DataInputStream dis, Pool pool) throws IOException {
		int oIndex = dis.readUnsignedShort();
		int rIndex = dis.readUnsignedShort();
		String o   = pool.<StringEntry_0> get(oIndex).value();
		String r   = pool.<StringEntry_0> get(rIndex).value();
		return new String[]{o, r};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ClassHook c : classes) {
			sb.append(c).append("\n");
			for (FieldHook h : c.getFields()) {
				sb.append("\t").append(h).append("\n");
			}
			for (MethodHook h : c.getMethods()) {
				sb.append("\t").append(h).append("\n");
			}
		}
		return sb.toString();
	}
}