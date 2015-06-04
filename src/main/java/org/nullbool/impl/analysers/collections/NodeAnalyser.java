package org.nullbool.impl.analysers.collections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.ClassStructure;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getKey&J", "getPrevious&Node", "getNext&Node", }, methods = {"isLinked&()Z", "unlink&()V"})
public class NodeAnalyser extends ClassAnalyser {

	public NodeAnalyser() throws AnalysisException {
		super("Node");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		// boolean rightField = containFieldOfType(cn, "J");
		// boolean rightStructure = (cn.methods.size() == 3) && (cn.fields.size() == 3);
		// if (cn.name.equals("gm")) {
		// System.out.println(cn.methods.size());
		// System.out.println(rightField);
		// System.out.println(rightStructure);
		// }
		// return rightStructure && rightField;
		return cn.superName.equals("java/lang/Object") && ((ClassStructure) cn).delegates.size() > 25;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new NodeInfoHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().add(new MethodsAnalyser());
	}
	
	private class MethodsAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			if(hasPreviousMethod != null) {
				list.add(asMethodHook(MethodType.CALLBACK, hasPreviousMethod, "isLinked"));
			}
			
			MethodNode[] ms  = getMethodNodes(cn.methods.stream().filter(m -> !Modifier.isStatic(m.access) && m.desc.endsWith(")V") && !m.name.equals("<init>")).collect(Collectors.toList()).toArray());
			MethodNode[] ms2 = startWithBc(new String[] { "aload", "getfield", "ifnonnull" }, ms);
			if(ms2.length == 1) {
				MethodNode m = ms2[0];
				list.add(asMethodHook(MethodType.CALLBACK, m, "unlink"));
			}
						
			return list;
		}
	}
	
	private MethodNode hasPreviousMethod;

	private class NodeInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] ms  = getMethodNodes(cn.methods.stream().filter(element -> element.desc.endsWith("Z")).collect(Collectors.toList()).toArray());
			MethodNode[] ms2 = startWithBc(new String[] { "aload", "getfield", "ifnonnull" }, ms);
			
			if(ms2.length == 1) {
				MethodNode m = ms2[0];
				hasPreviousMethod = m;
				
				FieldInsnNode prevFin = (FieldInsnNode) Arrays.asList(m.instructions.toArray()).stream().filter(ain -> ain.opcode() == GETFIELD).findFirst()
						.get();
				list.add(asFieldHook(cn.name + "." + prevFin.name, "getPrevious"));

				FieldNode nextFn = cn.fields.stream().filter(f -> f.desc.equals(prevFin.desc) && !f.name.equals(prevFin.name)).findFirst().get();
				list.add(asFieldHook(nextFn.owner.name + "." + nextFn.name, "getNext"));
			}

			String hook = getFieldOfType(cn, "J", false);
			list.add(asFieldHook(hook, "getKey"));

			return list;
		}
	}
}