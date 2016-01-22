package org.nullbool.impl.analysers.client.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author Bibl (don't ban me pls)
 * @created 5 Jul 2015 00:47:22
 */
@SupportedHooks(fields = {"args&Object[]", "opbase&String"/*, "isDisposable&Z"*/, "mouseX&I", "mouseY&I"}, methods = { })
public class ScriptEventAnalyser extends ClassAnalyser {

	private MethodNode method;
	private String name;

	/**
	 */
	public ScriptEventAnalyser() {
		super("ScriptEvent");
	}

	void findMethod() {
		String pattern = ";.*;.*;V";
		Map<String, ClassNode> nodes = Context.current().getClassNodes();
		String node = getClassNodeByRefactoredName("Node").name;
		MethodNode[] methods = findMethods(nodes, pattern, true);
		method = identifyMethod(methods, false, "sipush 1601");
		name = Type.getArgumentTypes(method.desc)[0].getClassName();

		ClassNode cn = nodes.get(name);
		if(!cn.superName.equals(node)) {
			throw new RuntimeException("ScriptEvent super != Node.");
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(method == null) {
			findMethod();
		}

		return cn.name.equals(name);

		/*String dual = findObfClassName("DualNode");
		if(!cn.superName.equals(dual))
			return false;

		// opcodes and int operands
		int intArrs = (int) getFieldOfTypeCount(cn, "\\[I", false);
		// string operands
		int stringArrs = (int) getFieldOfTypeCount(cn, "\\[Ljava/lang/String;", false);
		// int_arg_count, int_stack_count, string_arg_count, string_stack_count
		int ints = (int) getFieldOfTypeCount(cn, "I", false);

		if(intArrs >= 2 && stringArrs >= 1 && ints >= 4) {
			System.out.println("At: " + cn.name);
		}
		return false;*/
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new ArgsAnalyser(), new OpbaseAnalyser(), new DisposableAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class DisposableAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode node) {
			List<FieldHook> l = new ArrayList<FieldHook>();

			// static final void cl(Widget[] var0, int var1, int var2, int var3,
			// int var4, int var5, int var6, int var7) {
			// ([Lfu;IIIIIIII)V

			//			String desc = ";[\\[L.*;IIIIIIII;V";
			//			for(ClassNode cn : Context.current().getClassNodes().values()) {
			//				for(MethodNode m : cn.methods) {
			//					if(m.desc.matches(desc)) {
			//						System.out.println("m1: " + m);
			//					}
			//				}
			//			}

			// String regex = ";\\[L.*;IIIIIII.{0,1};V";
			// MethodNode[] ms = findMethods(Context.current().getClassNodes(), regex, true);
			// String[] pattern = new String[]{"new.*", "dup"};
			// MethodNode m = identifyMethod(ms, false, pattern);
			
			// l.add(asFieldHook(s, "isDisposable"));
			// System.out.println(s);


			String s = findField(method, "getfield .*I", "ldc -2147483647");
			l.add(asFieldHook(s, "mouseX"));
			// System.out.println(s);
			
			s = findField(method, "getfield .*I", "ldc -2147483646");
			l.add(asFieldHook(s, "mouseY"));
			// System.out.println(s);
			


			// String s = findField(method, "putfield " + cn.name + ".*Z", "new " + cn.name);
			// l.add(asFieldHook(s, "opbase"));
			// System.out.println("opbase; " +s);

			return l;
		}
	}

	public class ArgsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();

			InstructionSearcher searcher = new InstructionSearcher(method.instructions, new AbstractInsnNode[]{
					new VarInsnNode(ALOAD, 0),
					new FieldInsnNode(GETFIELD, cn.name, null, "[Ljava/lang/Object;"),
					new VarInsnNode(ASTORE, -1)
			});
			if(searcher.search()) {
				AbstractInsnNode[] match = searcher.getMatches().get(0);
				l.add(asFieldHook((FieldInsnNode) match[1], "args"));
			}

			return l;
		}
	}

	public class OpbaseAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();

			String s = findField(method, "getfield .*Ljava/lang/String;", "ldc event_opbase");
			l.add(asFieldHook(s, "opbase"));

			return l;
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