package org.nullbool.impl.analysers.client.task;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.BoundedInstructionIdentifier.DataPoint;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jul 2015 23:10:47
 */
@SupportedHooks(
		fields = { "killed&Z", "thread&Ljava/lang/Thread;", "eventQueue&Ljava/awt/EventQueue;", "currentTask&Task", "nextTask&Task" }, 
		methods = { "schedule&(IIILjava/lang/Object;)Task", "scheduleSocketTask&(Ljava/lang/String;I)Task",
				"scheduleRunnableTask&(Ljava/lang/Runnable;I)V", "scheduleINetTask&(I)Task",
				"scheduleDataInputStreamTask&(Ljava/net/URL;)Task", "stop&()V"
		})
public class TaskHandlerAnalyser extends ClassAnalyser {

	public MethodNode scheduleMethod;

	public TaskHandlerAnalyser() {
		super("TaskHandler");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		if(!cn.interfaces.contains("java/lang/Runnable"))
			return false;

		for(MethodNode m : cn.methods) {
			if(m.name.equals("run") && m.desc.equals("()V")) {
				String[] calls = new String[]{"wait", "getByName", "setDaemon", "setPriority", "openStream", "getHostName"};
				if(!containsCalls(m, true, calls)) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean containsCalls(MethodNode m, boolean name, String[] calls) {
		boolean[] matches = new boolean[calls.length];

		for(AbstractInsnNode ain : m.instructions.toArray()) {
			if(ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				for(int i=0; i < calls.length; i++) {
					String c = calls[i];
					if(name) {
						if(min.name.contains(c)) {
							matches[i] = true;
						}
					} else {
						if(min.key().matches(c)) {
							matches[i] = true;
						}
					}

				}
			}
		}

		for(int i=0; i < matches.length; i++) {
			if(!matches[i])
				return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldsAnalysers());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new ScheduleMethodsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return new Builder<IMultiAnalyser>().add(new StopMethodAnalyser());
	}

	public class FieldsAnalysers implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(ClassNode)
		 */
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			for(FieldNode f : cn.fields) {
				if(!Modifier.isStatic(f.access)) {
					if(f.desc.equals("Ljava/awt/EventQueue;")) {
						list.add(asFieldHook(f, "eventQueue"));
					}
				}
			}

			// aload0 // reference to self
			// getfield df.n:ej
			// astore1
			String[] currentpattern = new String[]{"aload 0", "getfield.*" + cn.name + ".*", "astore 1"};
			
			// aload0 // reference to self
			// aconst_null
			// putfield df.t:ej

			String[] nextpattern = new String[]{"aload 0", "aconst_null", "putfield.*" + cn.name + ".*"};

			for(MethodNode m : cn.methods) {
				if(m.name.equals("run") && m.desc.equals("()V")) {
					DataPoint p = pointOf(m, currentpattern);
					if(p != null) {
						FieldInsnNode fin = (FieldInsnNode) p.instruction().getNext();
						list.add(asFieldHook(fin, "currentTask"));
					}
					
					DataPoint p2 = pointOf(m, nextpattern);
					if(p2 != null) {
						FieldInsnNode fin = (FieldInsnNode) p2.instruction().getNext().getNext();
						list.add(asFieldHook(fin, "nextTask"));
					}
				}
			}

			return list;
		}

	}

	public class StopMethodAnalyser implements IMultiAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<ObfuscatedData> findAny(ClassNode cn) {
			List<ObfuscatedData> list = new ArrayList<ObfuscatedData>();
			//            aload0 // reference to self
			//            iconst_1
			//            putfield rs/TaskHandler.q:boolean

			String[] pattern1 = new String[]{"aload 0", "iconst_1", "putfield.*" + cn.name + ".*Z"};

			//            aload0 // reference to self
			//            getfield rs/TaskHandler.e:java.lang.Thread
			//            invokevirtual java/lang/Thread join(()V);

			String[] pattern2 = new String[]{"aload 0", "getfield.*Ljava/lang/Thread;", "invokevirtual java/lang/Thread.*join.*()V"};

			for(MethodNode m : cn.methods) {
				DataPoint p1 = pointOf(m, pattern1);
				if(p1 != null) {
					DataPoint p2 = pointOf(m, pattern2);
					if(p2 != null) {
						list.add(asMethodHook(m, "stop"));

						FieldInsnNode fin = (FieldInsnNode) p1.instruction().getNext().getNext();
						list.add(asFieldHook(fin, "killed"));

						fin = (FieldInsnNode) p2.instruction().getNext();
						list.add(asFieldHook(fin, "thread"));
					}
				}
			}

			return list;
		}
	}

	public class ScheduleMethodsAnalyser implements IMethodAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IMethodAnalyser#find(ClassNode)
		 */
		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			// 1 = socket task
			// 2 = runnable task
			// 3 = inet task
			// 4 = datainputstream task

			// aload0 // reference to self
			// iconst_1
			// iload2
			// iconst_0
			// aload1 // reference to arg0
			// invokevirtual dd f((IIILjava/lang/Object;)Let;);
			// areturn

			// w(java.lang.String arg0, int arg1) { //(Ljava/lang/String;I)Let;
			String[] socketpattern = new String[]{ "aload 0", "iconst_1", "iload 2", "iconst_0", "aload 1", "invokevirtual.*", "areturn" };

			// aload0 // reference to self
			// iconst_2
			// iload2
			// iconst_0
			// aload1 // reference to arg0
			// invokevirtual rs/TaskHandler n((IIILjava/lang/Object;)Lrs/ej;);
			// areturn

			String[] runnablepattern = new String[]{"aload 0", "iconst_2", "iload 2", "iconst_0", "aload 1", "invokevirtual.*", "areturn"};

			// aload0 // reference to self
			// iconst_3
			// iload1
			// iconst_0
			// aconst_null
			// invokevirtual rs/TaskHandler n((IIILjava/lang/Object;)Lrs/ej;);
			// areturn

			String[] inetpattern = new String[]{"aload 0", "iconst_3", "iload 1", "iconst_0", "aconst_null", "invokevirtual.*", "areturn"};

			// aload0 // reference to self
			// iconst_4
			// iconst_0
			// iconst_0
			// aload1
			// invokevirtual rs/TaskHandler n((IIILjava/lang/Object;)Lrs/ej;);
			// areturn

			String[] dispattern = new String[]{"aload 0", "iconst_4", "iconst_0", "iconst_0", "aload 1", "invokevirtual.*", "areturn"};

			for(MethodNode m : cn.methods) {
				//int var1, int var2, int var3, Object var4
				if(!Modifier.isStatic(m.access)) {
					if(m.desc.contains(")L")) {
						if(m.desc.startsWith("(IIILjava/lang/Object;)")) {
							// TOOD: Validate
							list.add(asMethodHook(m, "schedule"));
							scheduleMethod = m;
						} else if(m.desc.startsWith("(Ljava/lang/String;I)")) {
							if(identifyMethod(m, socketpattern)) {
								list.add(asMethodHook(m, "scheduleSocketTask"));
							}
						} else if(m.desc.startsWith("(Ljava/lang/Runnable;I)")) {
							if(identifyMethod(m, runnablepattern)) {
								list.add(asMethodHook(m, "scheduleRunnableTask"));
							}
						} else if(m.desc.startsWith("(I)")) {
							if(identifyMethod(m, inetpattern)) {
								list.add(asMethodHook(m, "scheduleINetTask"));
							}
						} else if(m.desc.startsWith("(Ljava/net/URL;)")) {
							if(identifyMethod(m, dispattern)) {
								list.add(asMethodHook(m, "scheduleDataInputStreamTask"));
							}
						}
					}
				}
			}

			return list;
		}
	}
}