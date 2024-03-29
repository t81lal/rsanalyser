package org.nullbool.impl.analysers.net;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.*;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.custom_asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedHooks(
		fields = { "cipher&IsaacCipher", "bitCaret&I"}, 
		methods = { "initCipher&([I)V", "initBitAccess&()V", "finishBitAccess&()V", "readableBytes&(I)I", "readBits&(I)I",
					"writePacketHeader&(I)V", "readPacketHeader&()I" })
/**
 * @author Bibl (don't ban me pls)
 * @created 23 May 2015
 */
public class PacketAnalyser extends ClassAnalyser {

	private static final int[] INIT_CIPHER_PATTERN = new int[]{INVOKESPECIAL, PUTFIELD};
	private static final int[] INIT_BIT_ACCESS_PATTERN1 = new int[]{GETFIELD, IMUL, PUTFIELD};
	private static final int[] INIT_BIT_ACCESS_PATTERN2 = new int[]{GETFIELD, LDC, IMUL, PUTFIELD};
	private static final int[] FINISH_BIT_ACCESS_PATTERN = new int[]{BIPUSH, IDIV};
	private static final int[] READABLE_BEATS_PATTERN1 = new int[]{GETFIELD, IMUL, ISUB, IRETURN};
	private static final int[] READABLE_BEATS_PATTERN2 = new int[]{GETFIELD, LDC, IMUL, ISUB, IRETURN};
	private static final int[] READ_BITS_PATTERN = new int[]{ICONST_3, ISHR, ISTORE};			//		      iload1

	private static final int[] WRITE_HEADER_PATTERN = new int[]{ALOAD, GETFIELD, INVOKEVIRTUAL, IADD, I2B, BASTORE};
	private static final int[] READ_HEADER_PATTERN = new int[]{BALOAD, ALOAD, GETFIELD, INVOKEVIRTUAL, ISUB, SIPUSH, IAND, IRETURN};

	private static final int[] GETFIELD_PATTERN = new int[]{GETFIELD};

	public PacketAnalyser() {
		super("Packet");
	}

	@Override
	protected boolean matches(ClassNode cn) { 
		ClassNode bufferNode     = getAnalyser("Buffer").getFoundClass();
		Set<ClassNode> delegates = Context.current().getClassTree().getDelegates(bufferNode);
		return delegates.contains(cn);
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldAnalyser());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new MethodAnalyser(), new HeaderMethodAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return null;
	}

	public class HeaderMethodAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#findMethods(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			//			  write
			//		      iload1
			//            aload0 // reference to self
			//            getfield rs/Packet.cipher:rs.IsaacCipher
			//            invokevirtual rs/IsaacCipher y(()I);
			//            iadd
			//            i2b
			//            bastore

			// read
			//            baload
			//            aload0 // reference to self
			//            getfield rs/Packet.cipher:rs.IsaacCipher
			//            invokevirtual rs/IsaacCipher y(()I);
			//            isub
			//            sipush 255
			//            iand
			//            ireturn


			for(MethodNode m : cn.methods) {
				if(!Modifier.isStatic(m.access)) {
					if(m.desc.equals("(I)V")) {
						if(findOpcodePattern(m, WRITE_HEADER_PATTERN) != null) {
							list.add(asMethodHook(m, "writePacketHeader"));
						}
					} else if(m.desc.equals("()I")) {
						if(findOpcodePattern(m, READ_HEADER_PATTERN) != null) {
							list.add(asMethodHook(m, "readPacketHeader"));
						}
					}
				}
			}
			return list;
		}
	}

	public class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			for(MethodNode m : cn.methods) {
				if(!((m.access & ACC_STATIC) != 0)){
					if (m.desc.startsWith("([I") && m.desc.endsWith("V")) {
						MethodInsnNode min = (MethodInsnNode) findOpcodePattern(m, INIT_CIPHER_PATTERN);
						if (min != null) {
							list.add(asMethodHook(m, "initCipher").var(Constants.METHOD_TYPE, Constants.CALLBACK));
						}
					} else if (m.desc.endsWith("V")) {
						FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, INIT_BIT_ACCESS_PATTERN1);
						if (fin != null) {
							list.add(asMethodHook(m, "initBitAccess").var(Constants.METHOD_TYPE, Constants.CALLBACK));
						} else {
							fin = (FieldInsnNode) findOpcodePattern(m, INIT_BIT_ACCESS_PATTERN2);
							if (fin != null) {
								list.add(asMethodHook(m, "initBitAccess").var(Constants.METHOD_TYPE, Constants.CALLBACK));
							} else {
								IntInsnNode iin = (IntInsnNode) findOpcodePattern(m, FINISH_BIT_ACCESS_PATTERN);
								if (iin != null) {
									if (InstructionUtil.resolve(iin) == 8) {
										list.add(asMethodHook(m, "finishBitAccess").var(Constants.METHOD_TYPE, Constants.CALLBACK));
									}
								}
							}
						}
					} else if (m.desc.endsWith(")I")) {
						FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, READABLE_BEATS_PATTERN1);
						if (fin == null)
							fin = (FieldInsnNode) findOpcodePattern(m, READABLE_BEATS_PATTERN2);

						if (fin != null) {
							list.add(asMethodHook(m, "readableBytes").var(Constants.METHOD_TYPE, Constants.CALLBACK));
						} else {
							AbstractInsnNode a1 = findOpcodePattern(m, READ_BITS_PATTERN);
							if (a1 != null) {
								list.add(asMethodHook(m, "readBits").var(Constants.METHOD_TYPE, Constants.CALLBACK));
							}
						}
					}
				}
			}

			//            aload0 // reference to self
			//            aload0 // reference to self
			//            getfield Packet.getCaret:int
			//            ldc -619618920 (java.lang.Integer)
			//            imul
			//            putfield Packet.bitCaret:int


			//            aload0 // reference to self
			//            aload0 // reference to self
			//            getfield Packet.getCaret:int
			//            ldc -960061224 (java.lang.Integer)
			//            imul
			//            putfield Packet.n:int


			return list;
		}
	}

	public class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			//			MethodNode[] classMethods = getMethodNodes(cn.methods.toArray());
			//			final MethodNode caretMethod = startWithBc(new String[] { "bipush","iload" }, classMethods)[0];
			//			final MethodNode cipherMethod = startWithBc(new String[] { "aload","getfield", "aload" }, classMethods)[0];

			//			String h = findField(cipherMethod, true, true, 1, 'f', "isub");
			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("([I") && m.desc.endsWith(")V")) {
					AbstractInsnNode ain = findOpcodePattern(m, INIT_CIPHER_PATTERN);
					if(ain != null) {
						FieldInsnNode fin = (FieldInsnNode) ain.getNext();
						list.add(asFieldHook(fin, "cipher"));
					}
				} else if(m.desc.endsWith("V")) {
					FieldInsnNode f1 = (FieldInsnNode) findOpcodePattern(m, INIT_BIT_ACCESS_PATTERN1);
					if(f1 == null) {
						IntInsnNode iin = (IntInsnNode) findOpcodePattern(m, FINISH_BIT_ACCESS_PATTERN);
						if(iin != null) {
							if(InstructionUtil.resolve(iin) == 8) {
								//finishBitAccess
								FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, GETFIELD_PATTERN);
								if(fin != null) {
									list.add(asFieldHook(fin, "bitCaret"));
								}
							}
						}
					}
				}
			}


			//			String h = findField(caretMethod, true, true, 1, 'f', "iload 1");
			//			list.add(asFieldHook(h, "bitCaret"));

			return list;
		}
	}
}