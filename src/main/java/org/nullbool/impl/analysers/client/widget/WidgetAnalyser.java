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
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = { "getQuantities&[I", "getItemIds&[I", "getBoundsIndex&I", "getActions&String[]", "getUID&I", "getName&String",
		"getText&String", "getTextColor&I", "getTextAlpha&I", "getTextureId&I", "getBorderThickness&I", "getModelType&I", "getModelId&I",
		"getRelativeX&I", "getRelativeY&I", "getWidth&I", "getHeight&I", "getParentId&I", "getIndex&I",
		"getRotationX&I", "getRotationY&I", "getRotationZ&I", "isHidden&Z", "getInsetX&I", "getInsetY&I", "getViewportWidth&I",
		"getViewportHeight&I", "getModelZoom&I", "getParent&Widget", "getSelectedAction&String", "getMouseEnterListener&Object[]",
		"getMouseExitListener&Object[]", "getMouseHoverListener&Object[]", "getConfigListenerArgs&Object[]", "getConfigTriggers&[I",
		"getRenderListener&Object[]", "getTableListenerArgs&Object[]", "getTableModTriggers&[I", "getSkillListenerArgs&Object[]",
		"getSkillTriggers&[I", "getScrollListeners&Object[]", "getFontId&I", "isTextShadowed&Z", "getShadowColour&I", "isFlippedVertically&Z",
		"isFlippedHorizontally&Z", "getChildren&Widget[]", "getItemId&I", "getStackSize&I", "getWidgetType&I" }, methods = {})
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
		return new Builder<IFieldAnalyser>().addAll(new ItemAndStackHooks(), new BoundsIndexHooks(), new WidgetInfoHooks(), new ChildrenHooks(),
				new TypeHooks() /*, new TableActionsAnalyser()*/);
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
			add(l, asFieldHook(h, "getActions"));

			h = findField(method, true, true, 1, 'f', "dup_x1");
			add(l, asFieldHook(h, "getUID"));

			h = findField(method, "getfield .*String;", "sipush 1802");
			add(l, asFieldHook(h, "getName"));

			h = findField(method, "getfield .*String;", "sipush 1112");
			add(l, asFieldHook(h, "getText"));

			h = findField(method, "putfield .*I", "sipush 1101");
			add(l, asFieldHook(h, "getTextColor"));

			h = findField(method, "putfield .*I", "sipush 1102");
			add(l, asFieldHook(h, "getTextAlpha"));

			h = findField(method, "putfield .*I", "sipush 1105");
			add(l, asFieldHook(h, "getTextureId"));

			h = findField(method, "putfield .*I", "sipush 1116");
			add(l, asFieldHook(h, "getBorderThickness"));

			h = findField(method, "putfield .*I", "sipush 1201");
			add(l, asFieldHook(h, "getModelType"));

			// Added 03/07/15, Bibl ///////////////////////////////
			h = findField3(method, 2, "putfield .*I", "sipush 1108");
			add(l, asFieldHook(h, "getModelId"));
			// ////////////////////////////////////////////////////

			// Scroll = Inset
			// h = findField2(method, "getfield .*I", "sipush 1100", "if_icmpne");
			// add(l, asFieldHook(h, "getScrollX"));
			// h = findField(method, "getfield .*I", "sipush 1601");
			// add(l, asFieldHook(h, "getScrollY"));

			h = findField(method, "getfield .*I", "sipush 1500");
			add(l, asFieldHook(h, "getRelativeX"));

			h = findField(method, "getfield .*I", "sipush 1501");
			add(l, asFieldHook(h, "getRelativeY"));

			h = findField(method, "getfield .*I", "sipush 1502");
			add(l, asFieldHook(h, "getWidth"));

			h = findField(method, "getfield .*I", "sipush 1503");
			add(l, asFieldHook(h, "getHeight"));

			h = findField(method, "getfield .*I", "sipush 1504");
			add(l, asFieldHook(h, "getParentId"));

			h = findField(method, "getfield .*I", "sipush 1702");
			add(l, asFieldHook(h, "getIndex"));

			h = findField(method, "getfield .*I", "sipush 2606");
			add(l, asFieldHook(h, "getRotationX"));

			h = findField(method, "getfield .*I", "sipush 2607");
			add(l, asFieldHook(h, "getRotationY"));

			h = findField(method, "getfield .*I", "sipush 2608");
			add(l, asFieldHook(h, "getRotationZ"));

			// Added 03/07/15, Bibl.
			h = findField(method, "getfield .*Z", "sipush 1504");
			add(l, asFieldHook(h, "isHidden"));

			h = findField2(method, "getfield .*I", "sipush 2600", "if_icmpne");
			add(l, asFieldHook(h, "getInsetX"));

			h = findField2(method, "getfield .*I", "sipush 2601", "if_icmpne");
			add(l, asFieldHook(h, "getInsetY"));

			h = findField(method, "getfield .*I", "sipush 2603");
			add(l, asFieldHook(h, "getViewportWidth"));

			h = findField(method, "getfield .*I", "sipush 2604");
			add(l, asFieldHook(h, "getViewportHeight"));

			h = findField(method, "getfield .*I", "sipush 2605");
			add(l, asFieldHook(h, "getModelZoom"));

			h = findField(method, "putfield .*L" + cn.name + ";", "sipush 1301");
			add(l, asFieldHook(h, "getParent"));

			h = findField(method, "putfield .*Ljava/lang/String;", "sipush 1306");
			add(l, asFieldHook(h, "getSelectedAction"));

			// WRONG
//			h = findField3(method, 4, "putfield .*I", "sipush 1001");
//			add(l, asFieldHook(h, "getButtonType"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1403");
			add(l, asFieldHook(h, "getMouseEnterListener"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1404");
			add(l, asFieldHook(h, "getMouseExitListener"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1412");
			add(l, asFieldHook(h, "getMouseHoverListener"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1407");
			add(l, asFieldHook(h, "getConfigListenerArgs"));

			h = findField(method, "putfield .*I", "sipush 1407");
			add(l, asFieldHook(h, "getConfigTriggers"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1408");
			add(l, asFieldHook(h, "getRenderListener"));

			h = findField3(method, 1, "putfield .*\\[Ljava/lang/Object;", "sipush 1414");
			add(l, asFieldHook(h, "getTableListenerArgs"));

			h = findField3(method, 1, "putfield .*\\[I", "sipush 1414");
			add(l, asFieldHook(h, "getTableModTriggers"));

			h = findField3(method, 1, "putfield .*\\[Ljava/lang/Object;", "sipush 1415");
			add(l, asFieldHook(h, "getSkillListenerArgs"));

			h = findField3(method, 1, "putfield .*\\[I", "sipush 1415");
			add(l, asFieldHook(h, "getSkillTriggers"));

			h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1417");
			add(l, asFieldHook(h, "getScrollListeners"));

			h = findField(method, "putfield .*I", "sipush 1113");
			add(l, asFieldHook(h, "getFontId"));

			h = findField(method, "putfield .*Z", "sipush 1115");
			add(l, asFieldHook(h, "isTextShadowed"));

			h = findField(method, "putfield .*I", "sipush 1117");
			add(l, asFieldHook(h, "getShadowColour"));

			h = findField(method, "putfield .*Z", "sipush 1118");
			add(l, asFieldHook(h, "isFlippedVertically"));

			h = findField(method, "putfield .*Z", "sipush 1119");
			add(l, asFieldHook(h, "isFlippedHorizontally"));
			
			return l;
		}

		private void add(List<FieldHook> l, FieldHook f) {
			if(f != null) {
				l.add(f);
			}
		}
	}
	
	public class TableActionsAnalyser implements IFieldAnalyser {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree
		 * .ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			for (MethodNode m : cn.methods) {
				// aload0 // reference to self
				// iconst_5
				// anewarray java/lang/String
				// putfield fu.cy:java.lang.String[]
				String s = findField2(m, "putfield .*\\[Ljava/lang/String;", "iconst_5", "anewarray");
				if (s != null) {
					list.add(asFieldHook(s, "getTableActions"));
					break;
				}
			}
			
			return list;
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
			l.add(asFieldHook(f, "getBoundsIndex"));

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
			 l.add(asFieldHook(f, "getWidgetType"));
			 
			f = findField(m, true, true, 1, 'f', "sipush 1701");
			l.add(asFieldHook(f, "getItemId"));

			f = findField(m, true, true, 2, 'f', "sipush 1701");
			l.add(asFieldHook(f, "getStackSize"));

			return l;
		}
	}
}