package org.nullbool.impl.analysers.client.widget;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Bibl
 */
@SupportedHooks(fields = {"quantities&[I", "itemIds&[I", "boundsIndex&I", "actions&String[]", "uid&I", "name&String",
        "text&String", "textColor&I", "textAlpha&I", "textureId&I", "borderThickness&I", "modelType&I", "modelId&I",
        "relativeX&I", "relativeY&I", "width&I", "height&I", "parentId&I", "index&I",
        "rotationX&I", "rotationY&I", "rotationZ&I", "hidden&Z", "insetX&I", "insetY&I", "viewportWidth&I",
        "viewportHeight&I", "modelZoom&I", "parent&Widget", "selectedAction&String", "mouseEnterListeners&Object[]",
        "mouseExitListeners&Object[]", "mouseHoverListeners&Object[]", "configListenerArgs&Object[]", "configTriggers&[I",
        "renderListeners&Object[]", "tableListenerArgs&Object[]", "tableModTriggers&[I", "skillListenerArgs&Object[]",
        "skillTriggers&[I", "scrollListeners&Object[]", "fontId&I", "textShadowed&Z", "shadowColour&I", "flippedVertically&Z",
        "flippedHorizontally&Z", "children&Widget[]", "itemId&I", "stackSize&I", "widgetType&I", "spriteId&I","loopCycle&I"}, methods = {})
public class WidgetAnalyser extends ClassAnalyser {

    public WidgetAnalyser() throws AnalysisException {
        super("Widget");
    }

    @Override
    public boolean matches(ClassNode cn) {
        String name = findObfClassName("Node");
        String[] pattern = {"new", "dup", "sipush"};
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
        public List<FieldHook> findFields(ClassNode cn) {
            String pattern = ";.*;.*;V";
            List<FieldHook> l = new ArrayList<FieldHook>();
            MethodNode[] m = findMethods(Context.current().getClassNodes(), pattern, true);
            MethodNode method = identifyMethod(m, false, "sipush 1601");

            String h = findField(method, "getfield .*String;", "sipush 1801");
            add(l, asFieldHook(h, "actions"));

            h = findField(method, true, true, 1, 'f', "dup_x1");
            add(l, asFieldHook(h, "uid"));

            h = findField(method, "getfield .*String;", "sipush 1802");
            add(l, asFieldHook(h, "name"));

            h = findField(method, "getfield .*String;", "sipush 1112");
            add(l, asFieldHook(h, "text"));

            h = findField(method, "putfield .*I", "sipush 1101");
            add(l, asFieldHook(h, "textColor"));

            h = findField(method, "putfield .*I", "sipush 1102");
            add(l, asFieldHook(h, "textAlpha"));

            h = findField(method, "putfield .*I", "sipush 1105");
            add(l, asFieldHook(h, "textureId"));

            h = findField(method, "putfield .*I", "sipush 1116");
            add(l, asFieldHook(h, "borderThickness"));

            h = findField(method, "putfield .*I", "sipush 1201");
            add(l, asFieldHook(h, "modelType"));

            // Added 03/07/15, Bibl ///////////////////////////////
            h = findField3(method, 2, "putfield .*I", "sipush 1108");
            add(l, asFieldHook(h, "modelId"));
            // ////////////////////////////////////////////////////

            // Scroll = Inset
            // h = findField2(method, "getfield .*I", "sipush 1100", "if_icmpne");
            // add(l, asFieldHook(h, "getScrollX"));
            // h = findField(method, "getfield .*I", "sipush 1601");
            // add(l, asFieldHook(h, "getScrollY"));

            h = findField(method, "getfield .*I", "sipush 1500");
            add(l, asFieldHook(h, "relativeX"));

            h = findField(method, "getfield .*I", "sipush 1501");
            add(l, asFieldHook(h, "relativeY"));

            h = findField(method, "getfield .*I", "sipush 1502");
            add(l, asFieldHook(h, "width"));

            h = findField(method, "getfield .*I", "sipush 1503");
            add(l, asFieldHook(h, "height"));

            h = findField(method, "getfield .*I", "sipush 1504");
            add(l, asFieldHook(h, "parentId"));

            h = findField(method, "getfield .*I", "sipush 1702");
            add(l, asFieldHook(h, "index"));

            h = findField(method, "getfield .*I", "sipush 2606");
            add(l, asFieldHook(h, "rotationX"));

            h = findField(method, "getfield .*I", "sipush 2607");
            add(l, asFieldHook(h, "rotationY"));

            h = findField(method, "getfield .*I", "sipush 2608");
            add(l, asFieldHook(h, "rotationZ"));

            // Added 03/07/15, Bibl.
            h = findField(method, "getfield .*Z", "sipush 1504");
            add(l, asFieldHook(h, "hidden"));

            h = findField2(method, "getfield .*I", "sipush 2600", "if_icmpne");
            add(l, asFieldHook(h, "insetX"));

            h = findField2(method, "getfield .*I", "sipush 2601", "if_icmpne");
            add(l, asFieldHook(h, "insetY"));

            h = findField(method, "getfield .*I", "sipush 2603");
            add(l, asFieldHook(h, "viewportWidth"));

            h = findField(method, "getfield .*I", "sipush 2604");
            add(l, asFieldHook(h, "viewportHeight"));

            h = findField(method, "getfield .*I", "sipush 2605");
            add(l, asFieldHook(h, "modelZoom"));

            h = findField(method, "putfield .*L" + cn.name + ";", "sipush 1301");
            add(l, asFieldHook(h, "parent"));

            h = findField(method, "putfield .*Ljava/lang/String;", "sipush 1306");
            add(l, asFieldHook(h, "selectedAction"));

            // WRONG
//			h = findField3(method, 4, "putfield .*I", "sipush 1001");
//			add(l, asFieldHook(h, "getButtonType"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1403");
            add(l, asFieldHook(h, "mouseEnterListeners"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1404");
            add(l, asFieldHook(h, "mouseExitListeners"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1412");
            add(l, asFieldHook(h, "mouseHoverListeners"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1407");
            add(l, asFieldHook(h, "configListenerArgs"));

            h = findField(method, "putfield .*I", "sipush 1407");
            add(l, asFieldHook(h, "configTriggers"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1408");
            add(l, asFieldHook(h, "renderListeners"));

            h = findField3(method, 1, "putfield .*\\[Ljava/lang/Object;", "sipush 1414");
            add(l, asFieldHook(h, "tableListenerArgs"));

            h = findField3(method, 1, "putfield .*\\[I", "sipush 1414");
            add(l, asFieldHook(h, "tableModTriggers"));

            h = findField3(method, 1, "putfield .*\\[Ljava/lang/Object;", "sipush 1415");
            add(l, asFieldHook(h, "skillListenerArgs"));

            h = findField3(method, 1, "putfield .*\\[I", "sipush 1415");
            add(l, asFieldHook(h, "skillTriggers"));

            h = findField(method, "putfield .*\\[Ljava/lang/Object;", "sipush 1417");
            add(l, asFieldHook(h, "scrollListeners"));

            h = findField(method, "putfield .*I", "sipush 1113");
            add(l, asFieldHook(h, "fontId"));

            h = findField(method, "putfield .*Z", "sipush 1115");
            add(l, asFieldHook(h, "textShadowed"));

            h = findField(method, "putfield .*I", "sipush 1117");
            add(l, asFieldHook(h, "shadowColour"));

            h = findField(method, "putfield .*Z", "sipush 1118");
            add(l, asFieldHook(h, "flippedVertically"));

            h = findField(method, "putfield .*Z", "sipush 1119");
            add(l, asFieldHook(h, "flippedHorizontally"));

            h = findField(method, "putfield .*I", "sipush 1122");
            add(l, asFieldHook(h, "spriteId"));

            /*
            MethodNode renderScreenMethod = identifyMethod(findMethods(Context.current().getClassNodes(), pattern, true), false, "ldc Fps:");
            h = findField(renderScreenMethod, true, true, 1, 'f', "getstatic .* I", "ldc .*", "imul", "putfield " + cn.name + ".* I");
            add(l, asFieldHook(h, "loopCycle"));
            */

            return l;
        }

        private void add(List<FieldHook> l, FieldHook f) {
            if (f != null) {
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
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            for (MethodNode m : cn.methods) {
                // aload0 // reference to self
                // iconst_5
                // anewarray java/lang/String
                // putfield fu.cy:java.lang.String[]
                String s = findField2(m, "putfield .*\\[Ljava/lang/String;", "iconst_5", "anewarray .*");
                if (s != null) {
                    list.add(asFieldHook(s, "tableActions"));
                    break;
                }
            }

            return list;
        }
    }

    public class ItemAndStackHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String f, regex = ";II.{0,1};V";
            List<FieldHook> l = new ArrayList<FieldHook>();
            String[] pattern = {"iload 2", "iaload", "istore 4"};
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);
            MethodNode m = identifyMethod(mn, false, pattern);

            AbstractInsnNode[] ins = followJump(m, 10);

            f = findField(ins, true, true, 1, 'f', "aload 0", "getfield .*");
            l.add(asFieldHook(f, "quantities"));

            f = findField(ins, true, true, 6, 'f', "aload 0", "getfield .*");
            l.add(asFieldHook(f, "itemIds"));

            return l;
        }
    }

    public class ChildrenHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> l = new ArrayList<FieldHook>();
            String f, pattern = ";II.{0,1};L" + cn.name + ";";
            String[] p = {"aload", "getfield", "arraylength", "if_icmplt"};

            MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
            MethodNode m = identifyMethod(mn, true, p);

            String regex = "getfield .*.* \\[L" + cn.name + ";";
            f = findField(m, true, true, 1, 'f', regex);
            l.add(asFieldHook(f, "children"));

            return l;
        }
    }

    public class BoundsIndexHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> l = new ArrayList<FieldHook>();
            String f, pattern = ";L" + cn.name + ";.{0,1};V";
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
            MethodNode m = identifyMethod(mn, true, "iconst_1", "bastore");

            f = findField(m, true, true, 1, 'f', "getstatic .* \\[Z");
            l.add(asFieldHook(f, "boundsIndex"));

            return l;
        }
    }

    public class TypeHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String regex, f, pattern = ";.{1,7};V";
            List<FieldHook> l = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), pattern, true);
            MethodNode m = identifyMethod(mn, false, "sipush 1107");

            regex = "invokespecial " + cn.name + "\\.<init> \\(\\)V";
            f = findField(m, true, true, 1, 'f', regex);
            l.add(asFieldHook(f, "widgetType"));

            f = findField(m, true, true, 1, 'f', "sipush 1701");
            l.add(asFieldHook(f, "itemId"));

            f = findField(m, true, true, 2, 'f', "sipush 1701");
            l.add(asFieldHook(f, "stackSize"));

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