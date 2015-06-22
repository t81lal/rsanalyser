package org.nullbool.impl.analysers.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.nullbool.pi.core.hook.api.FieldHook;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = { "getQuantities&[I", "getItemIds&[I", "getBoundsIndex&I", "getActions&[Ljava/lang/String;", "getUID&I", "getName&Ljava/lang/String;",
		"getText&Ljava/lang/String;", "getTextColor&I", "getTextAlpha&I", "getTextureId&I", "getBorderThickness&I", "getModelType&I", "getScrollX&I",
		"getScrollY&I", "getRelativeX&I", "getRelativeY&I", "getWidth&I", "getHeight&I", "getParentId&I", "getIndex&I", "getRotationX&I", "getRotationY&I",
		"getRotationZ&I", "getChildren&[Widget", "getWidgetType&I", "getItemId&I", "getStackSize&I", }, methods = {})
public class WidgetAnalyser extends ClassAnalyser {

	public WidgetAnalyser() throws AnalysisException {
		super("Widget");
	}

	@Override
	public boolean matches(ClassNode cn) {
		String name = findObfClassName("Node");
		String[] pattern = { "new", "dup", "sipush" };
		boolean rightSuperClass = cn.superName.equals(name);
		boolean goodPattern = findMethod(cn, "<clinit>", pattern);
		return rightSuperClass && goodPattern;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new ItemAndStackHooks(), new BoundsIndexHooks(), new WidgetInfoHooks(), new ChildrenHooks(), new TypeHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class WidgetInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String pattern = ";.*;.*;V";
			List<FieldHook> l = new ArrayList<FieldHook>();
			MethodNode[] m = findMethods(Context.current().getClassNodes(), pattern, true);
			MethodNode method = identifyMethod(m, false, "sipush 1601");

			String h = findField(method, "getfield .*String;", "sipush 1801");
			l.add(asFieldHook(h, "getActions"));

			h = findField(method, true, true, 1, 'f', "dup_x1");
			l.add(asFieldHook(h, "getUID", findMultiplier(h, false)));

			h = findField(method, "getfield .*String;", "sipush 1802");
			l.add(asFieldHook(h, "getName"));

			h = findField(method, "getfield .*String;", "sipush 1112");
			l.add(asFieldHook(h, "getText"));

			h = findField(method, "putfield .*I", "sipush 1101");
			l.add(asFieldHook(h, "getTextColor", findMultiplier(h, false)));

			h = findField(method, "putfield .*I", "sipush 1102");
			l.add(asFieldHook(h, "getTextAlpha", findMultiplier(h, false)));

			h = findField(method, "putfield .*I", "sipush 1105");
			l.add(asFieldHook(h, "getTextureId", findMultiplier(h, false)));

			h = findField(method, "putfield .*I", "sipush 1116");
			l.add(asFieldHook(h, "getBorderThickness", findMultiplier(h, false)));

			h = findField(method, "putfield .*I", "sipush 1201");
			l.add(asFieldHook(h, "getModelType", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1100");
			l.add(asFieldHook(h, "getScrollX", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1601");
			l.add(asFieldHook(h, "getScrollY", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1500");
			l.add(asFieldHook(h, "getRelativeX", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1501");
			l.add(asFieldHook(h, "getRelativeY", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1502");
			l.add(asFieldHook(h, "getWidth", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1503");
			l.add(asFieldHook(h, "getHeight", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1504");
			l.add(asFieldHook(h, "getParentId", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 1702");
			l.add(asFieldHook(h, "getIndex", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 2606");
			l.add(asFieldHook(h, "getRotationX", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 2607");
			l.add(asFieldHook(h, "getRotationY", findMultiplier(h, false)));

			h = findField(method, "getfield .*I", "sipush 2608");
			l.add(asFieldHook(h, "getRotationZ", findMultiplier(h, false)));

			return l;
		}
	}

	public class ItemAndStackHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String f, regex = ";II.{0,1};V";
			List<FieldHook> l = new ArrayList<FieldHook>();
			String[] pattern = { "iload 2", "iaload", "istore 4" };
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);
			MethodNode m = identifyMethod(mn, false, pattern);

			AbstractInsnNode[] ins = followJump(m, 10);

			f = findField(ins, true, true, 1, 'f', "aload 0", "getfield .*");
			l.add(asFieldHook(f, "getQuantities"));

			f = findField(ins, true, true, 6, 'f', "aload 0", "getfield .*");
			l.add(asFieldHook(f, "getItemIds"));

			return l;
		}
	}

	public class ChildrenHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();
			String f, pattern = ";II.{0,1};L" + cn.name + ";";
			String[] p = { "aload", "getfield", "arraylength", "if_icmplt" };

			MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
			MethodNode m = identifyMethod(mn, true, p);

			String regex = "getfield .*.* \\[L" + cn.name + ";";
			f = findField(m, true, true, 1, 'f', regex);
			l.add(asFieldHook(f, "getChildren"));

			return l;
		}
	}

	public class BoundsIndexHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> l = new ArrayList<FieldHook>();
			String f, pattern = ";L" + cn.name + ";.{0,1};V";
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
			MethodNode m = identifyMethod(mn, true, "iconst_1", "bastore");

			f = findField(m, true, true, 1, 'f', "getstatic .* \\[Z");
			l.add(asFieldHook(f, "getBoundsIndex", findMultiplier(f, false)));

			return l;
		}
	}

	public class TypeHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String regex, f, pattern = ";.{1,7};V";
			List<FieldHook> l = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
			MethodNode m = identifyMethod(mn, false, "sipush 1107");

			regex = "invokespecial " + cn.name + "\\.<init> \\(\\)V";

			f = findField(m, true, true, 1, 'f', regex);
			l.add(asFieldHook(f, "getWidgetType", findMultiplier(f, false)));

			f = findField(m, true, true, 1, 'f', "sipush 1701");
			l.add(asFieldHook(f, "getItemId", findMultiplier(f, false)));

			f = findField(m, true, true, 2, 'f', "sipush 1701");
			l.add(asFieldHook(f, "getStackSize", findMultiplier(f, false)));

			return l;
		}
	}
}