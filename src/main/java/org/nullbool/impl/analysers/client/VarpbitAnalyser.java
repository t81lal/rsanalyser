package org.nullbool.impl.analysers.client;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * THANKS FOR THIS GREG, NEVER FORGET.
 * 
 * @author Bibl (don't ban me pls)
 * @created 9 Jul 2015 23:52:12
 */
@SupportedHooks(fields = { "configId&I", "initialBit&I", "endBit&I"}, methods = { })
public class VarpbitAnalyser extends ClassAnalyser {

//	private final InstructionPattern pattern;
//	private String cname;
	
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

//		pattern = new InstructionPattern(new AbstractInsnNode[]{
//				new FieldInsnNode(GETSTATIC, null, null, "[I"),
//				new VarInsnNode(ILOAD, -1),
//				new InsnNode(IALOAD),
//				new VarInsnNode(ILOAD, -1),
//				new InsnNode(ISHR),
//				new VarInsnNode(ILOAD, -1),
//				new InsnNode(IAND),
//				new InsnNode(IRETURN)
//		});
	}

//	void findMethod() {
//		for(ClassNode cn : Context.current().getClassNodes().values()) {
//			for(MethodNode m : cn.methods) {
//				if(Modifier.isStatic(m.access) && m.desc.startsWith("(I") && Type.getArgumentTypes(m.desc).length <= 2) {
//					InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
//					if(searcher.search()) {
//						System.out.println("its: " + m.key());
//						cname = "";
//					}
//				}
//			}
//		}
//	}
	
	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
//		if(cname == null) {
//			findMethod();
//		}
		
		ClassNode dual = getClassNodeByRefactoredName("DualNode");
		if(!cn.superName.equals(dual.name))
			return false;
		int ints = (int) getFieldOfTypeCount(cn, "I", false);
		if(ints != 3)
			return false;
		int ns = 0;
		for(FieldNode f : cn.fields) {
			if(!Modifier.isStatic(f.access))
				ns++;
		}
		
		return ns == 3;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
	
	public class FieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			ClassNode buffer = getClassNodeByRefactoredName("Buffer");
			String desc = "(L" + buffer.name + ";I)V";
			for(MethodNode m : cn.methods) {
				if(!Modifier.isStatic(m.access) && m.desc.equals(desc)) {
					TreeBuilder tb = new TreeBuilder();
					AtomicInteger c = new AtomicInteger();
					NodeVisitor nv = new NodeVisitor(){
						@Override
						public void visitField(FieldMemberNode fmn) {
							if(fmn.opcode() == PUTFIELD) {
								FieldInsnNode fin = fmn.fin();
								int _c = c.incrementAndGet();
								switch(_c) {
									case 1:
										list.add(asFieldHook(fin, "configId"));
										break;
									case 2:
										list.add(asFieldHook(fin, "initialBit"));
										break;
									case 3:
										list.add(asFieldHook(fin, "endBit"));
										break;
								}
								
							}
						}
					};
					
					tb.build(m).accept(nv);
				}
			}
			
			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}