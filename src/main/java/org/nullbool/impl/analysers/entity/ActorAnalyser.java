package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "localX&I", "localY&I", "animationId&I", "interactingId&I", "health&I", "maxHealth&I", "hitTypes&[I",
		"message&Ljava/lang/String;", "hitDamages&[I", "hitCycle&I", /*"healthBarCycle&I" */
		"queueX&[I", "queueY&[I", "queueLength&I", "queueRun&[Z"}, 
				methods = { "queuePosition&(IIZ)V", "move&(IZ)V"})
public class ActorAnalyser extends ClassAnalyser {

	public ActorAnalyser() throws AnalysisException {
		super("Actor");

	}

	@Override
	protected boolean matches(ClassNode cn) {
		// FIXME: BREAKS ON REV 70 AND 75
		Set<ClassNode> supers = Context.current().getClassTree().getSupers(cn);
		ClassNode actorClass = getClassNodeByRefactoredName("Renderable");
		if(!supers.contains(actorClass))
			return false;
		
		if(getFieldOfTypeCount(cn, "\\[Z") != 1)
			return false;
		
		if(getFieldOfTypeCount(cn, "\\[I") < 3)
			return false;
		
		return true;
		
		/*String[] pattern = Context.current().getPattern("Actor");
		String superClassName = findObfClassName("Renderable");
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean rightFields = getFieldOfTypeCount(cn, "\\[Z") == 1;
		boolean goodPattern = findMethod(cn, "init", pattern);
		return goodPattern && rightSuperClass && rightFields;*/
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new XYHooks(), new AnimationHooks(), new InteractingHooks(), new HealthAndDamageHooks(), new QueueFieldsAnalyser(), new CycleHookAnalyser());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new QueuePositionMethodAnalyser(), new MoveMethodAnalyser());
	}
	
	public class MoveMethodAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			TreeBuilder tb = new TreeBuilder();
			
			for(MethodNode m : cn.methods) {
				if(m.desc.equals("(IZ)V")) {
					List<Integer> ints = new ArrayList<Integer>();
					NodeVisitor nv = new NodeVisitor(){
						@Override
						public void visitJump(JumpNode jn) {
							NumberNode nn = jn.firstNumber();
							VariableNode vn = jn.firstVariable();
							if(nn != null && vn != null && vn.var() == 1) {
								ints.add(nn.number());
							} else if(vn != null && vn.var() == 1) {
								if(jn.opcode() == IFNE || jn.opcode() == IFGE) {
									ints.add(0);
								}
							}
						}
					};
					
					tb.build(m).accept(nv);
					
					if(contains(ints, new int[]{0, 1, 2, 3, 4, 5, 6, 7})) {
						list.add(asMethodHook(m, "move"));
					}
				}
			}
			
			return list;
		}
	}
	
	private MethodNode moveMethod;
	
	public MethodNode findMoveMethod(ClassNode cn) {
		if(moveMethod != null) {
			return moveMethod;
		}
        // aload0 // reference to self
		// getfield rs/Actor.be:int[]
		// iload7
		// aload0 // reference to self
		// getfield rs/Actor.be:int[]
		// iload7
		// iconst_1
		// isub
		// iaload
		// iastore
		
		InstructionPattern ipattern = new InstructionPattern(new AbstractInsnNode[]{
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, cn.name, null, "[I"),
				new VarInsnNode(ILOAD, -1),
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, cn.name, null, "[I"),
				new VarInsnNode(ILOAD, -1),
				new InsnNode(ICONST_1),
				new InsnNode(ISUB),
				new InsnNode(IALOAD),
				new InsnNode(IASTORE)
		});
		
		InstructionPattern bpattern = new InstructionPattern(new AbstractInsnNode[]{
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, cn.name, null, "[Z"),
				new VarInsnNode(ILOAD, -1),
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, cn.name, null, "[Z"),
				new VarInsnNode(ILOAD, -1),
				new InsnNode(ICONST_1),
				new InsnNode(ISUB),
				new InsnNode(BALOAD),
				new InsnNode(BASTORE)
		});

		for(MethodNode m : cn.methods) {
			if(m.desc.equals("(IIZ)V")) {
				InstructionSearcher isearcher = new InstructionSearcher(m.instructions, ipattern);
				InstructionSearcher bsearcher = new InstructionSearcher(m.instructions, bpattern);
				if(isearcher.search() && bsearcher.search()) {
					if(isearcher.size() == 2 && bsearcher.size() == 1) {
						return (moveMethod = m);
					}
				}
			}
		}
		
		return null;
	}
	
	public class QueuePositionMethodAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			MethodNode m = findMoveMethod(cn);
			if(m != null) {
				list.add(asMethodHook(m, "queuePosition"));
			}
			
			return list;
		}
	}
	
	public class QueueFieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			MethodNode m = findMoveMethod(cn);
			if(m != null) {
	            // aload0 // reference to self
	            // getfield rs/Actor.bb:int[]
	            // iconst_0
	            // iload2 // reference to arg1
	            // iastore


				InstructionPattern ipattern = new InstructionPattern(new AbstractInsnNode[]{
						new VarInsnNode(ALOAD, 0),
						new FieldInsnNode(GETFIELD, cn.name, null, "[I"),
						new InsnNode(ICONST_0),
						new VarInsnNode(ILOAD, -1),
						new InsnNode(IASTORE)
				});
				
				InstructionSearcher isearcher = new InstructionSearcher(m.instructions, ipattern);
				if(isearcher.search() && isearcher.size() >= 2) {
					List<Integer> found = new ArrayList<Integer>();
					for(AbstractInsnNode[] ains : isearcher.getMatches()) {
						FieldInsnNode fin = (FieldInsnNode) ains[1];
						VarInsnNode vin = (VarInsnNode) ains[3];
						int var = vin.var;
						if(!found.contains(var)) {
							found.add(var);
							if(var == 1) {
								list.add(asFieldHook(fin, "queueX"));
							} else if(var == 2) {
								list.add(asFieldHook(fin, "queueY"));
							}
						}
					}
				}
				
				InstructionPattern bpattern = new InstructionPattern(new AbstractInsnNode[]{
						new VarInsnNode(ALOAD, 0),
						new FieldInsnNode(GETFIELD, cn.name, null, "[Z"),
						new InsnNode(ICONST_0),
						new InsnNode(ICONST_0),
						new InsnNode(BASTORE)
				});
				
				InstructionSearcher bsearcher = new InstructionSearcher(m.instructions, bpattern);
				if(bsearcher.search()) {
					FieldInsnNode fin = (FieldInsnNode) bsearcher.getMatches().get(0)[1];
					list.add(asFieldHook(fin, "queueRun"));
				}
				
				
	            // aload0 // reference to self
				// getfield rs/Actor.bc:int
				// ldc 1937318741 (java.lang.Integer)
				// imul
				// bipush 9
				// if_icmpge L13
				//  goto L12

	             
				InstructionPattern lenPattern = new InstructionPattern(new AbstractInsnNode[]{
						new VarInsnNode(ALOAD, 0),
						new FieldInsnNode(GETFIELD, cn.name, null, "I"),
						new LdcInsnNode(null),
						new InsnNode(IMUL),
						new IntInsnNode(BIPUSH, 9),
						new JumpInsnNode(-1, null)
				});
				
				InstructionSearcher lenSearcher = new InstructionSearcher(m.instructions, lenPattern);
				if(lenSearcher.search()) {
					FieldInsnNode fin = (FieldInsnNode) lenSearcher.getMatches().get(0)[1];
					list.add(asFieldHook(fin, "queueLength"));
				}
			}
			
			return list;
		}
	}
	
	public class CycleHookAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			//aload0 // reference to self
            //getfield rs/Actor.ae:int[]
            //iload5
            //bipush 70
            //iload3
            //iadd
            //iastore

//			InstructionPattern pattern = new InstructionPattern(new AbstractInsnNode[]{
//					new VarInsnNode(ALOAD, 0),
//					new FieldInsnNode(GETFIELD, cn.name, null, "[I"),
//					new VarInsnNode(ILOAD, 5),
//					new IntInsnNode(BIPUSH, 70),
//					new VarInsnNode(ILOAD, 3),
//					new InsnNode(IADD),
//					new InsnNode(IASTORE)
//			});
			
			TreeBuilder tb = new TreeBuilder();
			for(MethodNode m : cn.methods) {
				if(m.desc.equals("(III)V")) {
					NodeVisitor nv = new NodeVisitor() {
						@Override
						public void visit(AbstractNode an) {
							if(an.opcode() == IASTORE) {
								ArithmeticNode arn = an.firstOperation();
								if(arn != null && arn.opcode() == IADD) {
									NumberNode nn = arn.firstNumber();
									if(nn != null && nn.number() == 70) {
										FieldMemberNode fmn = an.firstField();
										if(fmn != null && fmn.opcode() == GETFIELD && fmn.desc().equals("[I")) {
											list.add(asFieldHook(fmn.fin(), "hitCycle"));
										}
									}
								}
							}
						}
					};
					tb.build(m).accept(nv);
//					InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
//					if(searcher.search()) {
//						FieldInsnNode fin = (FieldInsnNode) searcher.getMatches().get(0)[1];
//					}
				}
			}
			
			return list;
		}
	}

	public class XYHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regexPat = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regexPat, true);
			MethodNode method = identifyMethod(m, false, "sipush 256");
			
			String fieldPattern[] = { "if_icmpeq|ifne", "\\w*", "\\w*", "\\w*", "imul", "istore" };
			AbstractInsnNode[] ins = followJump(method, 40);

			// TODO: broke on rev72, verified by: Bibl
			h = findField(ins, false, true, 1, 'f', fieldPattern);
			l.add(asFieldHook(h, "localX"));

			h = findField(ins, false, true, 2, 'f', fieldPattern);
			l.add(asFieldHook(h, "localY"));

			healthbarcycle: for (MethodNode mn : cn.methods) {
				if (mn.name.equals("<init>")) {
					for (AbstractInsnNode ain : mn.instructions.toArray()) {
						if (ain.opcode() == PUTFIELD) {
							AbstractInsnNode prev = ain.getPrevious();
							if (prev.opcode() == LDC) {
								LdcInsnNode lin = (LdcInsnNode) prev;
								if (lin.cst instanceof Number) {
									FieldInsnNode fin = (FieldInsnNode) ain;
									int l1 = 0;
									if (lin.cst instanceof Long) {
										l1 = (int) (long) lin.cst;
									} else {
										l1 = (int) lin.cst;
									}
									String k = fin.owner + "." + fin.name;
									long mul = Context.current().getMultiplierHandler().getDecoder(k) * l1;
									if (mul == -1000) {
										l.add(asFieldHook(k, "healthBarCycle"));
										break healthbarcycle;
									}
								}
							}
						}
					}
				}
			}

			return l;
		}
	}

	public class InteractingHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "ldc 32768");

			h = findField(method, true, false, 1, 'f', "ldc 32768");
			list.add(asFieldHook(h, "interactingId"));

			return list;
		}
	}

	public class AnimationHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 1536");
			AbstractInsnNode[] ins = followJump(method, 40);
			h = findField(ins, true, true, 1, 'f', "sipush 128", "if_icmplt");
			list.add(asFieldHook(h, "animationId"));

			return list;
		}
	}

	public class HealthAndDamageHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String r = ";.*" + "L" + actorObj + ";III" + ".*;V";
			MethodNode[] ms = findMethods(Context.current().getClassNodes(), r, true);
			MethodNode m = identifyMethod(ms, false, "bipush 30");
			String[] pat = { "getfield .* .*String;", "aastore" };
			String reg = "invokestatic java/lang/Integer.toString .*String;";

			h = findField(m, true, false, 2, 'f', "idiv", "istore 8");
			list.add(asFieldHook(h, "health"));
			
			// TODO:
			// System.out.println(m.owner.name + " " + m.name + " " + m.desc);
			h = findField(m, true, false, 1, 'f', "idiv", "istore 8");
			list.add(asFieldHook(h, "maxHealth"));

			h = findField(m, true, false, 1, 'f', "iload 7", "iaload", "aaload");
			list.add(asFieldHook(h, "hitTypes"));

			h = findField(m, true, true, 1, 'f', pat);
			list.add(asFieldHook(h, "message"));

			h = findField(m, true, false, 1, 'f', reg);
			list.add(asFieldHook(h, "hitDamages"));

			System.out.println("dank  " + m);
			
			// ms = findMethod(Context.current().getClassNodes(), "bipush 70", false);
			// m = identifyMethod(ms, false, "aload 0 getfield aload 4");
			// h = findField(m, true, false, 1, 'f', "iload 4");
			// System.out.println("found " + h);

			return list;
		}
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