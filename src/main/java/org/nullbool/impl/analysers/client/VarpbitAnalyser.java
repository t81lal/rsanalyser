package org.nullbool.impl.analysers.client;

import java.lang.reflect.Modifier;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author Bibl (don't ban me pls)
 * @created 9 Jul 2015 23:52:12
 */
@SupportedHooks(fields = { }, methods = { })
public class VarpbitAnalyser extends ClassAnalyser {

	private final InstructionPattern pattern;
	private String cname;
	
	/**
	 * @param name
	 */
	public VarpbitAnalyser() {
		super("Varpbit");
		
//        getstatic rs/fv.z:int[]
//        iload3
//        iaload
//        iload4
//        ishr
//        iload6
//        iand
//        ireturn

		pattern = new InstructionPattern(new AbstractInsnNode[]{
				new FieldInsnNode(GETSTATIC, null, null, "[I"),
				new VarInsnNode(ILOAD, -1),
				new InsnNode(IALOAD),
				new VarInsnNode(ILOAD, -1),
				new InsnNode(ISHR),
				new VarInsnNode(ILOAD, -1),
				new InsnNode(IAND),
				new InsnNode(IRETURN)
		});
	}

	void findMethod() {
		for(ClassNode cn : Context.current().getClassNodes().values()) {
			for(MethodNode m : cn.methods) {
				if(Modifier.isStatic(m.access) && m.desc.startsWith("(I") && Type.getArgumentTypes(m.desc).length <= 2) {
					InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
					if(searcher.search()) {
						System.out.println("its: " + m.key());
						cname = "";
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(cname == null) {
			findMethod();
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
}