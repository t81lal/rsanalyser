package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.zbot.pi.core.hooks.api.FieldHook;
import org.nullbool.zbot.pi.core.hooks.api.MethodHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SupportedHooks(fields = { "getCipher&IsaacCipher", "getBitCaret&I"}, methods = { "initCipher&([I)V", "initBitAccess&()V", "finishBitAccess&()V", "readableBytes&(I)I", "readBits&(I)I"})
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
	private static final int[] READ_BITS_PATTERN = new int[]{ICONST_3, ISHR, ISTORE};
	
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
		return new Builder<IMethodAnalyser>().add(new MethodAnalyser());
	}
	
	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("([I") && m.desc.endsWith("V")) {
					MethodInsnNode min = (MethodInsnNode) findOpcodePattern(m, INIT_CIPHER_PATTERN);
					if(min != null) {
						list.add(asMethodHook(m, "initCipher").var(MethodHook.TYPE, MethodHook.CALLBACK));
					}
				} else if(m.desc.endsWith("V")) {
					FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, INIT_BIT_ACCESS_PATTERN1);
					if(fin != null) {
						list.add(asMethodHook(m, "initBitAccess").var(MethodHook.TYPE, MethodHook.CALLBACK));
					} else {
						fin = (FieldInsnNode) findOpcodePattern(m, INIT_BIT_ACCESS_PATTERN2);
						if(fin != null) {
							list.add(asMethodHook(m, "initBitAccess").var(MethodHook.TYPE, MethodHook.CALLBACK));
						} else {
							IntInsnNode iin = (IntInsnNode) findOpcodePattern(m, FINISH_BIT_ACCESS_PATTERN);
							if(iin != null) {
								if(InstructionUtil.resolve(iin) == 8) {
									list.add(asMethodHook(m, "finishBitAccess").var(MethodHook.TYPE, MethodHook.CALLBACK));
								}
							}
						}
					}
				} else if(m.desc.endsWith(")I")) {
					FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, READABLE_BEATS_PATTERN1);
					if(fin == null) 
						fin = (FieldInsnNode) findOpcodePattern(m, READABLE_BEATS_PATTERN2);
					
					if(fin != null) {
						list.add(asMethodHook(m, "readableBytes").var(MethodHook.TYPE, MethodHook.CALLBACK));
					} else {
						AbstractInsnNode a1 = findOpcodePattern(m, READ_BITS_PATTERN);
						if(a1 != null) {
							list.add(asMethodHook(m, "readBits").var(MethodHook.TYPE, MethodHook.CALLBACK));
						}
					}
				}
			}

//            aload0 // reference to self
//            aload0 // reference to self
//            getfield Packet.getCaret:int
//            ldc -619618920 (java.lang.Integer)
//            imul
//            putfield Packet.getBitCaret:int

            
//            aload0 // reference to self
//            aload0 // reference to self
//            getfield Packet.getCaret:int
//            ldc -960061224 (java.lang.Integer)
//            imul
//            putfield Packet.n:int

			
			return list;
		}
	}
	
	private class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
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
						list.add(asFieldHook(fin, "getCipher", findMultiplier(source(fin), false)));
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
									list.add(asFieldHook(fin, "getBitCaret", findMultiplier(source(fin), false)));
								}
							}
						}
					}
				}
			}
			
			
//			String h = findField(caretMethod, true, true, 1, 'f', "iload 1");
//			list.add(asFieldHook(h, "getBitCaret", findMultiplier(h, false)));
			
			return list;
		}
	}
}