package org.nullbool.impl.analysers.client;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SupportedHooks(fields = { "reason_&Ljava/lang/String;", "throwable&Ljava/lang/Throwable;" }, methods = {})
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
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldHooksAnalyser());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class FieldHooksAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> hooks = new ArrayList<FieldHook>();

			for (MethodNode m : cn.methods) {
				if (m.name.equals("<init>") && m.desc.equals("(Ljava/lang/Throwable;Ljava/lang/String;)V")) {
					for (AbstractInsnNode ain : m.instructions.toArray()) {
						if (ain.opcode() == PUTFIELD) {
							FieldInsnNode fin = (FieldInsnNode) ain;
							if (fin.desc.equals("Ljava/lang/String;")) {
								hooks.add(asFieldHook(fin.owner + "." + fin.name, "reason_"));
							} else if (fin.desc.equals("Ljava/lang/Throwable;")) {
								hooks.add(asFieldHook(fin.owner + "." + fin.name, "throwable"));
							}
						}
					}
				}
			}

			return hooks;
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