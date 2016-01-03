package org.nullbool.impl.analysers.entity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.BoundedInstructionIdentifier.DataPoint;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 Jul 2015 19:07:09
 */
@SupportedHooks(
		fields = { "startX&I", "startY&I", "currentX&D", "currentY&D", "currentZ&D", "modelHeight&I", 
				"endCycle&I", "speedX&D", "speedY&D", "speedZ&D", "speed&D", "slope&I", "moving&Z",
				"heightStep&D", "radius&I", "rotationX&I", "rotationY&I",
				"id&I", "sceneId&I", "delay&I", "targetIndex&I", "endHeight&I",
				"animation&AnimationSequence", "frameCycle&I", "currentFrame&I" }, 
		methods = { "move&(I)V", "trackTarget&(IIII)V"}
		)
public class ProjectileAnalyser extends ClassAnalyser {

	public ProjectileAnalyser() {
		super("Projectile");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		ClassNode renderable = getClassNodeByRefactoredName("Renderable");
		if(renderable == null)
			return false;

		if(!cn.superName.equals(renderable.name))
			return false;

		boolean init = false;
		boolean track = false;
		boolean move = false;

		for(MethodNode m : cn.methods) {
			if(m.name.equals("<init>")) {
				if(m.desc.equals("(IIIIIIIIIII)V")) {
					init = true;
				}
			} else if(m.desc.equals("(IIII)V")) {
				track = true;
			} else if(m.desc.equals("(I)V")) {
				move = true;
			}
		}

		return init && track && move;
	}

	public class CompleteAnalyser implements IMultiAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMultiAnalyser#findAny(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<ObfuscatedData> findAny(ClassNode cn) {
			List<ObfuscatedData> list = new ArrayList<ObfuscatedData>();

			//            iload1 // reference to arg0
			//            aload0 // reference to self
			//            getfield rs/q.g:int
			//            ldc 1672657297 (java.lang.Integer)
			//            imul
			//            isub
			//            i2d
			//            dstore6
			String[] startpattern = new String[]{"iload.*", "aload 0", "getfield.*", "ldc.*", "imul", "isub", "i2d", "dstore.*"};

			//            aload0 // reference to self
			//            aload0 // reference to self
			//            getfield rs/q.b:int
			//            ldc 206883127 (java.lang.Integer)
			//            imul
			//            i2d
			//            dload6
			//            dmul
			//            dload10
			//            ddiv
			//            aload0 // reference to self
			//            getfield rs/q.g:int
			//            ldc 1672657297 (java.lang.Integer)
			//            imul
			//            i2d
			//            dadd
			//            putfield rs/q.w:double
			
//            aload0 // reference to self
//            dup
//            getfield rs/Projectile.d:int
//            iload1
//            ldc 1589266809 (java.lang.Integer)
//            imul
//            iadd
//            putfield rs/Projectile.d:int
			
			String[] framepattern = new String[]{"aload 0", "dup", "getfield.*", "iload 1", "ldc.*", "imul", "iadd", "putfield.*"};

//            aload0 // reference to self
//            iconst_0
//            putfield rs/Projectile.u:int

			String[] curframepattern = new String[]{"aload 0", "iconst_0", "putfield.*"};
			
			TreeBuilder tb = new TreeBuilder();
			
			for(MethodNode m : cn.methods) {
				if(!Modifier.isStatic(m.access)) {
					if(m.desc.equals("(IIII)V")) {
						List<DataPoint> ps = pointsOf(m, startpattern);
						if(ps != null && ps.size() == 2) {
							for(DataPoint p : ps) {
								VarInsnNode vin = (VarInsnNode) p.instruction();
								FieldInsnNode fin = (FieldInsnNode) vin.getNext().getNext();
								if(vin.var == 1) {
									list.add(asFieldHook(fin, "startX"));
								} else if(vin.var == 2) {
									list.add(asFieldHook(fin, "startY"));
								} else {
									System.out.println("WTF, VAR= " + vin.var);
								}
							}
						}

						AtomicBoolean radius = new AtomicBoolean();
						
						NodeVisitor nv = new NodeVisitor(){
							@Override
							public void visitField(FieldMemberNode fmn) {
								if(fmn.opcode() == PUTFIELD && fmn.desc().equals("D")) {
									VariableNode aload0 = fmn.firstVariable();
									ArithmeticNode add = (ArithmeticNode) fmn.first(DADD);
									if(aload0 != null && aload0.var() == 0 && add != null) {
										ArithmeticNode div = (ArithmeticNode) add.first(DDIV);
										if(div != null) {
											ArithmeticNode dmul = (ArithmeticNode) div.first(DMUL);
											if(dmul != null) {
												VariableNode dload = dmul.firstVariable();
												if(dload != null) {
													int index = dload.var();
													FieldInsnNode fin = fmn.fin();
													if(index == 6) {
														list.add(asFieldHook(fin, "currentX"));
													} else if(index == 8) {
														list.add(asFieldHook(fin, "currentY"));
													} else {
														System.out.println("WTF, DVAR= " + index);
													}
													
													if(!radius.get()) {
														for(AbstractNode an : dmul.traverse()) {
															if(an.opcode() == GETFIELD) {
																FieldMemberNode fmn2 = (FieldMemberNode) an;
																list.add(asFieldHook(fmn2.fin(), "radius"));
																radius.set(true);
															}
														}
													}
												}
											}
										}
									} else if(aload0 != null) {
										ConversionNode i2d = (ConversionNode) fmn.first(I2D);
										if(i2d != null) {
											ArithmeticNode mul = (ArithmeticNode) i2d.first(IMUL);
											if(mul != null) {
												FieldMemberNode curz = mul.firstField();
												if(curz != null && curz.opcode() == GETFIELD) {
													list.add(asFieldHook(fmn.fin(), "currentZ"));
													list.add(asFieldHook(curz.fin(), "modelHeight"));
												}
											}
										} else {
											ArithmeticNode div = (ArithmeticNode) fmn.first(DDIV);
											if(div != null) {
												ArithmeticNode sub = (ArithmeticNode) div.first(DSUB);
												if(sub != null) {
													i2d = (ConversionNode) sub.first(I2D);
													FieldMemberNode f2 = (FieldMemberNode) sub.first(GETFIELD);
													if(i2d != null && f2 != null) {
														VariableNode iload = (VariableNode) i2d.first(ILOAD);
														if(iload != null) {
															int index = iload.var();
															if(index == 1) {
																list.add(asFieldHook(fmn.fin(), "speedX"));
															} else if(index == 2) {
																list.add(asFieldHook(fmn.fin(), "speedY"));
															} else {
																System.out.println("WTF(2), DVAR= " + index);
															}
														}
													}
												} else {
													for(AbstractNode an : fmn.traverse()) {
														if(an instanceof NumberNode) {
															NumberNode nn = (NumberNode) an;
															if(nn.insn() instanceof LdcInsnNode) {
																LdcInsnNode ldc = (LdcInsnNode) nn.insn();
																if(ldc.cst instanceof Double) {
																	if(nn.number() == 2) {
																		list.add(asFieldHook(fmn.fin(), "heightStep"));
																	}
																}
															}
														}
													}
												}
											} else {
												boolean stat = false;
												boolean dneg = false;
												
												for(AbstractNode an : fmn.traverse()) {
													if(an.opcode() == DNEG) {
														dneg = true;
													} else if(an.opcode() == INVOKESTATIC) {
														MethodMemberNode mmn = (MethodMemberNode) an;
														if(mmn.owner().equals("java/lang/Math") && mmn.name().equals("tan")) {
															stat = true;
														}
													}
												}
												
												if(stat && dneg) {
													list.add(asFieldHook(fmn.fin(), "speedZ"));
												}
											}
										}
									}
								}
							}
							
							@Override
							public void visitVariable(VariableNode vn) {
								if(vn.opcode() == DSTORE) {
									ConversionNode i2d = (ConversionNode) vn.first(I2D);
									if(i2d != null) {
										ArithmeticNode sub = (ArithmeticNode) i2d.first(ISUB);
										if(sub != null) {
											ArithmeticNode add = (ArithmeticNode) sub.first(IADD);
											VariableNode cycleload = (VariableNode) sub.first(ILOAD);
											if(add != null && cycleload != null && cycleload.var() == 4) {
												AbstractNode iconst1 = add.first(ICONST_1);
												ArithmeticNode mul = (ArithmeticNode) add.first(IMUL);
												if(iconst1 != null && mul != null) { 
													FieldMemberNode endcycle = (FieldMemberNode) mul.first(GETFIELD);
													if(endcycle != null) {
														list.add(asFieldHook(endcycle.fin(), "endCycle"));
													}
												}
											}
										}
									}
								}
							}
							
							@Override
							public void visitMethod(MethodMemberNode mmn) {
//								invokestatic   java/lang/Math.sqrt (D)D
//								dadd           
//									dmul           
//										getfield       q.s D
//											aload          #0
//										getfield       q.s D
//											aload          #0
//									dmul           
//										getfield       q.r D
//											aload          #0
//										getfield       q.r D
//											aload          #0

								if(mmn.opcode() == INVOKESTATIC) {
									if(mmn.owner().equals("java/lang/Math")) {
										if(mmn.name().equals("sqrt")) {
											ArithmeticNode add = (ArithmeticNode) mmn.first(DADD);
											if(add != null) {
												List<ArithmeticNode> muls = add.t_findChildren(DMUL);
												if(muls != null && muls.size() == 2) {
													for(ArithmeticNode mul : muls) {
														List<FieldMemberNode> fmns = mul.t_findChildren(GETFIELD);
														if(fmns == null || fmns.size() != 2) {
															return;
														}
													}
													
													AbstractNode parent = mmn.parent();
													if(parent != null && parent.opcode() == PUTFIELD) {
														FieldMemberNode fmn = (FieldMemberNode) parent;
														list.add(asFieldHook(fmn.fin(), "speed"));
													}
												}
											}
										} else if(mmn.name().equals("tan")) {
											for(AbstractNode an : mmn.traverse()) {
												if(an instanceof FieldMemberNode) {
													FieldMemberNode fmn = (FieldMemberNode) an;
													list.add(asFieldHook(fmn.fin(), "slope"));
												}
											}
										}
									}
								}
							}
							
							boolean moving = false;
							
							@Override
							public void visitJump(JumpNode jn) {
								FieldMemberNode fmn = jn.firstField();
								if(fmn != null && fmn.opcode() == GETFIELD && fmn.desc().equals("Z") && !moving) {
									list.add(asFieldHook(fmn.fin(), "moving"));
									moving = true;
								}
							}
						};

						tb.build(m).accept(nv);

						list.add(asMethodHook(m, "trackTarget"));
					} else if(m.desc.equals("(I)V")) {
						NodeVisitor nv = new NodeVisitor() {
							@Override
							public void visitField(FieldMemberNode fmn) {
								if(fmn.opcode() == PUTFIELD) {
									VariableNode aload0 = fmn.firstVariable();
									if(aload0 != null && aload0.var() == 0 && aload0.opcode() == ALOAD) {
										for(AbstractNode an : fmn.traverse()) {
											if(an.opcode() == INVOKESTATIC) {
												MethodMemberNode mmn = (MethodMemberNode) an;
												if(mmn.owner().equals("java/lang/Math") && mmn.name().equals("atan2")) {
													boolean num = false;
													
													for(AbstractNode tan : fmn.traverse()) {
														if(tan instanceof NumberNode) {
															NumberNode nn = (NumberNode) tan;
															if(nn.number() == 1024) {
																num = true;
															}
														}
													}
													
													if(num) {
														list.add(asFieldHook(fmn.fin(), "rotationX"));
													} else {
														list.add(asFieldHook(fmn.fin(), "rotationY"));
													}
												}
											}
										}
									}
								}
							}
						};
						tb.build(m).accept(nv);
						
						DataPoint dp = pointOf(m, framepattern);
						if(dp != null) {
							FieldInsnNode fin = (FieldInsnNode) dp.instruction().getNext().getNext();
							list.add(asFieldHook(fin, "frameCycle"));
						}
						
						dp = pointOf(m, curframepattern);
						if(dp != null) {
							FieldInsnNode fin = (FieldInsnNode) dp.instruction().getNext().getNext();
							list.add(asFieldHook(fin, "currentFrame"));
						}

						
						list.add(asMethodHook(m, "move"));
					} else if(m.name.equals("<init>") && m.desc.equals("(IIIIIIIIIII)V")) {
						NodeVisitor nv = new NodeVisitor() {
							@Override
							public void visitField(FieldMemberNode fmn) {
								VariableNode aload0 = fmn.firstVariable();
								if(aload0 != null && aload0.opcode() == ALOAD && aload0.var() == 0) {
									FieldInsnNode fin = fmn.fin();
									for(AbstractNode an : fmn.traverse()) {
										if(an.opcode() == ILOAD) {
											VariableNode vn = (VariableNode) an;
											switch(vn.var()) {
												case 1:
													list.add(asFieldHook(fin, "id"));
													break;
												case 2:
													list.add(asFieldHook(fin, "sceneId"));
													break;
												case 6:
													list.add(asFieldHook(fin, "delay"));
													break;
												case 10:
													list.add(asFieldHook(fin, "targetIndex"));
													break;
												case 11:
													list.add(asFieldHook(fin, "endHeight"));
													break;
											}
											
											return;
										}
									}
									
									if(fmn.first(ACONST_NULL) != null) {
										list.add(asFieldHook(fin, "animation"));
									}
								}
							}
						};
						tb.build(m).accept(nv);
					}
				}
			}

			return list;
		}
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
		return new Builder<IMultiAnalyser>().add(new CompleteAnalyser());
	}
}