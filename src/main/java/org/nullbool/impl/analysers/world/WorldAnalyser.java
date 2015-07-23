package org.nullbool.impl.analysers.world;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.TypeNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @OVERRIDE NO LONGER THE CASE
 *   NOTE: THIS ONLY WORKS AFTER REV 81
 * 
 * worldCount and worlds list is done in ClientAnalyser82. (static)
 * same with the load method.
 * 
 * @author Bibl (don't ban me pls)
 * @created 17 Jul 2015 20:24:02
 */
@SupportedHooks(fields = { }, methods = { })
public class WorldAnalyser extends ClassAnalyser {

	public MethodNode loadMethod;
	
	/**
	 * @param name
	 */
	public WorldAnalyser() {
		super("World");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		int ints = getFieldCount(cn, new StaticDescFilter("I"));
		int strings = getFieldCount(cn, new StaticDescFilter("Ljava/lang/String;"));

		if(ints < 5 || strings < 2)
			return false;

		List<Integer> masks = new ArrayList<Integer>();
		NodeVisitor nv = new NodeVisitor(){
			@Override
			public void visitJump(JumpNode jn) {
				if(InstructionUtil.isConditional(jn.opcode())) {
					ArithmeticNode an = jn.firstOperation();
					if(an != null && an.opcode() == IAND) {
						NumberNode nn = an.firstNumber();
						if(nn != null) {
							masks.add(nn.number());
						}
					}
				}
			}
		};

		TreeBuilder tb = new TreeBuilder();

		for(MethodNode m : cn.methods) {
			if(!Modifier.isStatic(m.access) && m.desc.equals("()Z")) {
				tb.build(m).accept(nv);
			}
		}
		
		return contains(masks, new int[]{0x01, 0x02, 0x04, 0x08}) || contains(masks, new int[]{0x01, 0x04});
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {		
		return new Builder<IFieldAnalyser>().add(new LoadMethodFinder());
	}
	
	public class WorldFieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			return list;
		}
	}
	
	public class LoadMethodFinder implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode _cn) {
			TreeBuilder tb = new TreeBuilder();
			for(ClassNode cn : Context.current().getClassNodes().values()) {
				for(MethodNode m : cn.methods) {
//					if(!Modifier.isStatic(m.access) || !m.desc.equals("()Z"))
//						continue;
					
					NodeVisitor nv = new NodeVisitor() {
						@Override
						public void visitField(FieldMemberNode fmn) {
							if(fmn.opcode() == PUTSTATIC) {
								TypeNode newarr = fmn.firstType();
								if(newarr != null && newarr.type().equals(_cn.name)) {
									for(AbstractNode an : fmn.traverse()) {
										if(an != fmn) {
											if(an instanceof FieldMemberNode) {
												FieldMemberNode fmn2 = (FieldMemberNode) an;
												if(fmn2.opcode() == GETSTATIC) {
													loadMethod = m;
													break;
												}
											}
										}
									}
								}
							}
						}
					};
					
					tb.build(m).accept(nv);
				}
			}
			
			return new ArrayList<FieldHook>();
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		// TODO Auto-generated method stub
		return null;
	}
}