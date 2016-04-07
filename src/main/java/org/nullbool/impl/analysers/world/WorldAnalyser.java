package org.nullbool.impl.analysers.world;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.StaticDescFilter;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @OVERRIDE NO LONGER THE CASE
 *   NOTE: THIS ONLY WORKS AFTER REV 81
 * 
 * worldCount and worlds list is done in ClientAnalyser82. (static)
 *  so is worldDownloader
 * same with the load method.
 * 
 * @author Bibl (don't ban me pls)
 * @created 17 Jul 2015 20:24:02
 */
@SupportedHooks(
		fields = { }, 
		methods = { })
public class WorldAnalyser extends ClassAnalyser {

	public MethodNode loadMethod;
	

	public WorldAnalyser() {
		super("World");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
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
		return null;
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
		return null;
	}
}