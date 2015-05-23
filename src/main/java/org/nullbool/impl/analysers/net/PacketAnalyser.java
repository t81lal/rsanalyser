package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields = { "getCipher&IsaacCipher", "getBitCaret&I"}, methods = { "initCipher&([I)V", "initBitAccess&()V", "finishBitAccess&()V", "readableBytes&(I)I", "readBits&(I)I"})
public class PacketAnalyser extends ClassAnalyser {

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
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new FieldAnalyser());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return Arrays.asList(new MethodAnalyser());
	}
	
	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("([I") && m.desc.endsWith("V")) {
					MethodInsnNode min = (MethodInsnNode) findOpcodePattern(m, new int[]{INVOKESPECIAL, PUTFIELD});
					if(min != null) {
						list.add(asMethodHook(MethodType.CALLBACK, m, "initCipher"));
					}
				} else if(m.desc.endsWith("V")) {
					FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, new int[]{GETFIELD, IMUL, PUTFIELD});
					if(fin != null) {
						list.add(asMethodHook(MethodType.CALLBACK, m, "initBitAccess"));
					} else {
						IntInsnNode iin = (IntInsnNode) findOpcodePattern(m, new int[]{BIPUSH, IDIV});
						if(iin != null) {
							if(InstructionUtil.resolve(iin) == 8) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "finishBitAccess"));
							}
						}
					}
				} else if(m.desc.endsWith(")I")) {
					FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, new int[]{GETFIELD, IMUL, ISUB, IRETURN});
					if(fin != null) {
						list.add(asMethodHook(MethodType.CALLBACK, m, "readableBytes"));
					} else {
						AbstractInsnNode a1 = findOpcodePattern(m, new int[]{ICONST_3, ISHR, ISTORE});
						if(a1 != null) {
							list.add(asMethodHook(MethodType.CALLBACK, m, "readBits"));
						}
					}
				}
			}
			
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
					AbstractInsnNode ain = findOpcodePattern(m, new int[]{INVOKESPECIAL, PUTFIELD});
					if(ain != null) {
						FieldInsnNode fin = (FieldInsnNode) ain.getNext();
						list.add(asFieldHook(fin, "getCipher", findMultiplier(source(fin), false)));
					}
				} else if(m.desc.endsWith("V")) {
					FieldInsnNode f1 = (FieldInsnNode) findOpcodePattern(m, new int[]{GETFIELD, IMUL, PUTFIELD});
					if(f1 == null) {
						IntInsnNode iin = (IntInsnNode) findOpcodePattern(m, new int[]{BIPUSH, IDIV});
						if(iin != null) {
							if(InstructionUtil.resolve(iin) == 8) {
								//finishBitAccess
								FieldInsnNode fin = (FieldInsnNode) findOpcodePattern(m, new int[]{GETFIELD});
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