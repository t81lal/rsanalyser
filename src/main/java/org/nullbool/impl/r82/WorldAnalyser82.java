package org.nullbool.impl.r82;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.impl.analysers.world.WorldAnalyser;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.TypeNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jul 2015 18:44:53
 */
public class WorldAnalyser82 extends WorldAnalyser {

	@Override
	public String[] supportedFields() {
		return new Builder<String>(super.supportedFields())
				.addAll("mask&I", "host&Ljava/lang/String;", "activity&Ljava/lang/String;", "population&I", "index&I").asArray(new String[0]);
	}
	
	public class CompleteFinder implements IMultiAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<ObfuscatedData> findAny(ClassNode _cn) {
			
			List<ObfuscatedData> list = new ArrayList<ObfuscatedData>();
			
			TreeBuilder tb = new TreeBuilder();
			for(ClassNode cn : Context.current().getClassNodes().values()) {
				for(MethodNode m : cn.methods) {
					if(!Modifier.isStatic(m.access) || !m.desc.endsWith(")Z"))
						continue;
					
					NodeVisitor nv = new NodeVisitor() {
						@Override
						public void visitType(TypeNode tn) {
							if(tn.opcode() == ANEWARRAY) {
								if(tn.type().equals(_cn.name)) {
									loadMethod = m;
								}
							}
						}
//						@Override
//						public void visitField(FieldMemberNode fmn) {
//							if(fmn.opcode() == PUTSTATIC) {
//								TypeNode newarr = fmn.firstType();
//								if(newarr != null && newarr.type().equals(_cn.name)) {
//									for(AbstractNode an : fmn.traverse()) {
//										if(an != fmn) {
//											if(an instanceof FieldMemberNode) {
//												FieldMemberNode fmn2 = (FieldMemberNode) an;
//												if(fmn2.opcode() == GETSTATIC) {
//													loadMethod = m;
//													break;
//												}
//											}
//										}
//									}
//								}
//							}
//						}
					};
					
					tb.build(m).accept(nv);
				}
			}
			
			if(loadMethod != null) {
//	             new u
//	             dup
//	             invokespecial u <init>(()V);
//	             dup_x2
//	             aastore
				NodeVisitor nv = new NodeVisitor() {
					boolean started = false;
					boolean stopped = false;
					int i = 0;
					@Override
					public void visitMethod(MethodMemberNode mmn) {
						if(started || stopped)
							return;
						
						if(mmn.opcode() == INVOKESPECIAL && mmn.owner().equals(_cn.name) && mmn.name().equals("<init>")) {
							started = true;
						}
					}
					
					@Override
					public void visitField(FieldMemberNode fmn) {
						if(stopped)
							return;
						
						if(started) {
							if(fmn.owner().equals(_cn.name) && fmn.opcode() == PUTFIELD) {
								FieldInsnNode fin= fmn.fin();
								switch(i) {
									case 0:
										list.add(asFieldHook(fin, "mask"));
										break;
									case 1:
										// TODO: find name
										break;
									case 2:
										list.add(asFieldHook(fin, "host"));
										break;
									case 3:
										list.add(asFieldHook(fin, "activity"));
										break;
									case 4:
										list.add(asFieldHook(fin, "population"));
										break;
									case 5:
										// TODO: find name
										break;
									case 6:
										list.add(asFieldHook(fin, "index"));
										break;
								}
								if(i >= 6) {
									stopped = true;
								}
								i++;
							}
						}
					}
				};
				tb.build(loadMethod).accept(nv);
			}

			return list;
		}
	}
	


	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return new Builder<IMultiAnalyser>().add(new CompleteFinder());
	}
}