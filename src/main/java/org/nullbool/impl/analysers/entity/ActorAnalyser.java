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
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getLocalX&I", "getLocalY&I", "getAnimationId&I", "getInteractingId&I", "getHealth&I", "getMaxHealth&I", "getHitTypes&[I",
		"getMessage&Ljava/lang/String;", "getHitDamages&[I", "getHealthBarCycle&I" }, methods = {})
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
		return new Builder<IFieldAnalyser>().addAll(new XYHooks(), new AnimationHooks(), new InteractingHooks(), new HealthAndDamageHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class XYHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regexPat = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regexPat, true);
			MethodNode method = identifyMethod(m, false, "sipush 256");
			String fieldPattern[] = { "if_icmpeq|ifne", "\\w*", "\\w*", "\\w*", "imul", "istore" };
			AbstractInsnNode[] ins = followJump(method, 40);

			// TODO: broke on rev72, verified by: Bibl
			h = findField(ins, false, true, 1, 'f', fieldPattern);
			l.add(asFieldHook(h, "getLocalX"));

			h = findField(ins, false, true, 2, 'f', fieldPattern);
			l.add(asFieldHook(h, "getLocalY"));

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
										l.add(asFieldHook(k, "getHealthBarCycle"));
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
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "ldc 32768");

			h = findField(method, true, false, 1, 'f', "ldc 32768");
			list.add(asFieldHook(h, "getInteractingId"));

			return list;
		}
	}

	public class AnimationHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String regex = ";\\w*" + "L" + actorObj + ";" + "\\w*;V";
			MethodNode[] m = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(m, false, "sipush 1536");
			AbstractInsnNode[] ins = followJump(method, 40);
			h = findField(ins, true, true, 1, 'f', "sipush 128", "if_icmplt");
			list.add(asFieldHook(h, "getAnimationId"));

			return list;
		}
	}

	public class HealthAndDamageHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String h, actorObj = findObfClassName("Actor");
			String r = ";.*" + "L" + actorObj + ";III" + ".*;V";
			MethodNode[] ms = findMethods(Context.current().getClassNodes(), r, true);
			MethodNode m = identifyMethod(ms, false, "bipush 30");
			String[] pat = { "getfield .* .*String;", "aastore" };
			String reg = "invokestatic java/lang/Integer.toString .*String;";

			h = findField(m, true, false, 2, 'f', "idiv", "istore 8");
			list.add(asFieldHook(h, "getHealth"));
			// TODO:
			// System.out.println(m.owner.name + " " + m.name + " " + m.desc);
			h = findField(m, true, false, 1, 'f', "idiv", "istore 8");
			list.add(asFieldHook(h, "getMaxHealth"));

			h = findField(m, true, false, 1, 'f', "iload 7", "iaload", "aaload");
			list.add(asFieldHook(h, "getHitTypes"));

			h = findField(m, true, true, 1, 'f', pat);
			list.add(asFieldHook(h, "getMessage"));

			h = findField(m, true, false, 1, 'f', reg);
			list.add(asFieldHook(h, "getHitDamages"));

			// ms = findMethod(Context.current().getClassNodes(), "bipush 70", false);
			// m = identifyMethod(ms, false, "aload 0 getfield aload 4");
			// h = findField(m, true, false, 1, 'f', "iload 4");
			// System.out.println("found " + h);

			return list;
		}
	}
}