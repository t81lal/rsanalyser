package org.nullbool.impl.analysers.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

@SupportedHooks(fields = { "getReason_&Ljava/lang/String;", "getThrowable&Ljava/lang/Throwable;" }, methods = {})
public class WrappedExceptionAnalyser extends ClassAnalyser {

	public WrappedExceptionAnalyser() {
		super("WrappedException");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		// RuntimeException
		return cn.superName.equals("java/lang/RuntimeException");
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new FieldHooksAnalyser());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class FieldHooksAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> hooks = new ArrayList<FieldHook>();

			for (MethodNode m : cn.methods) {
				if (m.name.equals("<init>") && m.desc.equals("(Ljava/lang/Throwable;Ljava/lang/String;)V")) {
					for (AbstractInsnNode ain : m.instructions.toArray()) {
						if (ain.opcode() == PUTFIELD) {
							FieldInsnNode fin = (FieldInsnNode) ain;
							if (fin.desc.equals("Ljava/lang/String;")) {
								hooks.add(asFieldHook(fin.owner + "." + fin.name, "getReason_"));
							} else if (fin.desc.equals("Ljava/lang/Throwable;")) {
								hooks.add(asFieldHook(fin.owner + "." + fin.name, "getThrowable"));
							}
						}
					}
				}
			}

			return hooks;
		}
	}
}