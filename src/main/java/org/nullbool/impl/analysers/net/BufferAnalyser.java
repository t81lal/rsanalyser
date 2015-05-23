package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.DescFilter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
				methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V", })
public class BufferAnalyser extends ClassAnalyser {

	private static final Filter<FieldNode> BYTE_ARRAY_FILTER = new DescFilter("[B");
	private static final Filter<FieldNode> INT_FILTER        = new DescFilter("I");

	public BufferAnalyser() {
		super("Buffer");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		ClassHook nodeHook = getAnalyser("Node").getFoundHook();
		if(cn.superName.equals(nodeHook.getObfuscated())) {
			int bytesFieldCount = getFieldCount(cn, BYTE_ARRAY_FILTER);
			int intFieldsCount  = getFieldCount(cn, INT_FILTER);
			return bytesFieldCount == 1 && intFieldsCount > 0;
		}
		return false;
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
				if(m.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "enableEncryption"));
				}
			}
			
			return list;
		}
	}
	
	private class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
					
			for(MethodNode m : cn.methods) {
				if(m.name.equals("<init>") && m.desc.startsWith("(I")) {
					for(AbstractInsnNode ain : m.instructions.toArray()) {
						if(ain.opcode() == PUTFIELD) {
							FieldInsnNode fin = (FieldInsnNode) ain;
							String source = String.format("%s.%s", fin.owner, fin.name);
							if(ain.getPrevious().opcode() == ICONST_0) {
								list.add(asFieldHook(fin, "getCaret", false, findMultiplier(source, false)));
							} else {
								list.add(asFieldHook(fin, "getPayload", false, 1));
							}
						}
					}
				}
			}
			
			return list;
		}
	}
}