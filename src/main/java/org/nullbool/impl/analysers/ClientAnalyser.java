package org.nullbool.impl.analysers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.EventCallGenerator;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author MalikDz
 */
@SupportedHooks(
        fields = {"npcs&[NPC", "players&[Player", "region&Region", /*"getWidgetPositionsX&[I", "getWidgetPositionsY&[I",*/
                "canvas&Ljava/awt/Canvas;", "localPlayer&Player", "localPlayerIndex&I" , "widgetNodes&HashTable", "menuVariable&[I", "menuActions&[Ljava/lang/String;", "spellSelected&Z",
                "selectionState&I", "menuOptions&[Ljava/lang/String;", "loopCycle&I", "currentWorld&I", "gameState&I", "currentLevels&[I",
                "realLevels&[I", "skillsExp&[I", "selectedSpellName&Ljava/lang/String;","selectedItemIndex&I", "selectedItemId&I", "selectedItemName&Ljava/lang/String;" ,"menuOpen&Z", "menuX&I", "menuY&I", "menuWidth&I", "menuHeight&I",
                "menuSize&I", "groundItems&[[[Deque", "tileSettings&[[[B", "tileHeights&[[[I", "mapScale&I", "mapOffset&I", "mapAngle&I",
                "plane&I", "cameraX&I", "cameraY&I", "cameraZ&I", "cameraYaw&I", "cameraPitch&I", "baseX&I", "baseY&I", "widgets&[[Widget",
                "clientSettings&[I", "widgetsSettings&[I", "hoveredRegionTileX&I", "hoveredRegionTileY&I", "itemTables&HashTable", "username&String", "password&String",
                "widgetPositionsX&I", "widgetPositionsY&I", "hintArrowType&I", "hintArrowX&I", "hintArrowY&I" ,"hintArrowHeight&I", "hintArrowNpcId&I",
                "hintArrowPlayerId&I","hintArrowIconX&I","hintArrowIconY&I","destinationX&I","destinationY&I"
        },
        methods = {"loadObjDefinition&(I)LObjectDefinition;", "loadItemDefinition&(I)LItemDefinition;",
                /*"getPlayerModel&()LModel;",*/ "reportException&(Ljava/lang/Throwable;Ljava/lang/String;)WrappedException", "processAction&(IIIILjava/lang/String;Ljava/lang/String;II)V"})
public class ClientAnalyser extends ClassAnalyser {

    public ClientAnalyser() throws AnalysisException {
        super("Client");
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return new Builder<IFieldAnalyser>().addAll(new ActorArrayHook(), new CurrentRegionHook(), new WidgetPositionXY(), new CanvasPlayerHook(), new ClientArrayHooks(),
                new MenuScreenHooks(), new GroundItemsHook(), new TileInfoHooks(), new MinimapHooks(), new CameraHooks(), new BaseXYHooks(), new WidgetsHook(),
                new SettingsHook(), new CredentialAnalyser(), new RegionWalkingHooks(), new ItemTableHook(), new CredentialHooks(), new HintArrowHook(),new LocalPlayerIdHook(),
                new SelectedItemHook());
    }

    @Override
    protected Builder<IMethodAnalyser> registerMethodAnalysers() {
        return new Builder<IMethodAnalyser>().addAll(new LoadDefinitionHook(), new ReportMethodHookAnalyser(), new ProccessActionMethodHookAnalyser());
    }

    /* (non-Javadoc)
     * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
     */
    @Override
    public Builder<IMultiAnalyser> registerMultiAnalysers() {
        return null;
    }

    @Override
    public boolean matches(ClassNode c) {
        return c.name.equalsIgnoreCase("client");
    }

    public class CredentialHooks implements IFieldAnalyser {

        /* (non-Javadoc)
         * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
         */
        @Override
        public List<FieldHook> findFields(ClassNode _cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();

            // getstatic aq.ad:java.lang.String
            // invokestatic da v((Ljava/lang/CharSequence;)I);
            // invokestatic java/lang/Integer valueOf((I)Ljava/lang/Integer;);
            // invokevirtual java/util/LinkedHashMap containsKey((Ljava/lang/Object;)Z);

            InstructionPattern pattern = new InstructionPattern(new AbstractInsnNode[]{
                    new FieldInsnNode(GETSTATIC, null, null, "Ljava/lang/String;"),
                    new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false),
                    new JumpInsnNode(IFNE, null)
                    //	getstatic aq.ad:java.lang.String
                    //	invokevirtual java/lang/String length(()I);
                    //	ifle L24

                    //	new FieldInsnNode(GETSTATIC, null, null, "Ljava/lang/String;"),
                    //	new MethodInsnNode(INVOKESTATIC, null, null, "(Ljava/lang/CharSequence;)I", false),
                    //	new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false),
                    //	new MethodInsnNode(INVOKEVIRTUAL, "java/util/LinkedHashMap", "containsKey", "(Ljava/lang/Object;)Z", false)
            });

            boolean username = false;
            boolean password = false;

            MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";L.*;V", true);

            f:
            for (MethodNode m : mn) {
                InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
                if (searcher.search()) {

                    for (AbstractInsnNode[] ains : searcher.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ains[0];
                        AbstractInsnNode ain = ains[2].getNext();
                        w:
                        while (ain != null) {
                            if (ain instanceof JumpInsnNode) {
                                JumpInsnNode jin = (JumpInsnNode) ain;
                                ain = jin.label.getNext();
                                continue;
                            }
                            ain = ain.getNext();

                            if (ain instanceof LdcInsnNode) {
                                while (ain != null) {
                                    if (!(ain instanceof LdcInsnNode)) {
                                        break w;
                                    }

                                    LdcInsnNode ldc = (LdcInsnNode) ain;
                                    if (ldc.cst instanceof String) {
                                        String s = (String) ldc.cst;
                                        if (s.contains("username") && !username) {
                                            list.add(asFieldHook(fin, "username"));
                                            username = true;
                                            break w;
                                        } else if (s.contains("password") && !password) {
                                            list.add(asFieldHook(fin, "password"));
                                            password = true;
                                            break w;
                                        }
                                    }

                                    ain = ain.getNext();
                                }
                            }

                            if (ain.getOpcode() != -1)
                                break;
                        }

                        if (username && password)
                            break f;
                    }
                }
            }

            return list;
        }
    }

    public class ItemTableHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> hooks = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";IIII.{0,2};V", false);
            final MethodNode[] m = startWithBc(new String[]{"getstatic", "iload", "i2l"}, mn);

            String field = findField(m[0], false, true, 1, 's', "getstatic");
            hooks.add(asFieldHook(getNew(field.split("\\.")[0]), field, "itemTables"));
            return hooks;
        }
    }

    public class RegionWalkingHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";L.{0,3};IIIIII.{0,2};V", false);
            final MethodNode m = identifyMethod(mn, false, "iload 7", "bipush 7", "ishl");

            String h = findField(m, true, true, 1, 's', "iload 8", "putstatic .* I");//wheres the class name?
            list.add(asFieldHook(getNew(h.split("\\.")[0]), h, "hoveredRegionTileX"));

            h = findField(m, true, true, 1, 's', "iload 7", "putstatic .* I");
            list.add(asFieldHook(getNew(h.split("\\.")[0]), h, "hoveredRegionTileY"));

            return list;
        }
    }

    public class CanvasPlayerHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String type = "L" + "java/awt/Canvas" + ";";
            List<FieldHook> list = new ArrayList<FieldHook>();
            String hook = identifyField(Context.current().getClassNodes(), type);
            list.add(asFieldHook(hook, "canvas"));

            type = "L" + findObfClassName("Player") + ";";
            String p = identifyField(Context.current().getClassNodes(), type);
            list.add(asFieldHook(p, "localPlayer"));

            return list;
        }
    }

    public class LocalPlayerIdHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";;V", true);

            MethodNode m = identifyMethod(mn,false,"ldc You have only just left another world.", "ldc Your profile will be transferred in:");
            String p = findField(m,true,true,1,'s',"imul",".* 8","ishl");
            list.add(asFieldHook(p, "localPlayerIndex"));
            return list;
        }
    }

    public class ActorArrayHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String hook, tempo;
            List<FieldHook> list = new ArrayList<FieldHook>();

            tempo = "[L" + findObfClassName("NPC") + ";";
            hook = identify(cn, tempo, 's');
            list.add(asFieldHook(hook, "npcs"));

            tempo = "[L" + findObfClassName("Player") + ";";
            hook = identify(cn, tempo, 's');
            list.add(asFieldHook(hook, "players"));

            return list;
        }
    }

    public class SelectedItemHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String regex = ";.{3,4}Ljava/lang/String;Ljava/lang/String;.{2,3};V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode method = startWithBc(new String[]{"iload","sipush","if_icmplt"},mn)[0];

            TreeBuilder builder = new TreeBuilder();
            builder.build(method).accept(new NodeVisitor() {
                @Override
                public void visitJump(JumpNode jn) {
                    if(jn.firstNumber() != null && jn.firstNumber().number() == 21) {
                        int fieldCount = 0;
                        AbstractInsnNode node = jn.insn();
                        while(node != null){
                            if(node instanceof FieldInsnNode){
                                if(fieldCount == 4){
                                    list.add(asFieldHook((FieldInsnNode)node,"destinationX",true));
                                }
                                if(fieldCount == 5){
                                    list.add(asFieldHook((FieldInsnNode)node,"destinationY",true));
                                }else if(fieldCount > 5){
                                    break;
                                }
                                fieldCount++;
                            }
                            if(node instanceof JumpInsnNode && node.getOpcode() == GOTO){
                                node = ((JumpInsnNode) node).label;
                            }else{
                                node = node.getNext();
                            }

                        }
                    }else if(jn.firstNumber() != null && jn.firstNumber().number() == 38) {
                        int fieldCount = 0;
                        AbstractInsnNode node = jn.insn();
                        while(node != null){
                            if(node instanceof FieldInsnNode){
                                if(fieldCount == 1){
                                    list.add(asFieldHook((FieldInsnNode)node,"selectedItemIndex",true));
                                }
                                if(fieldCount == 3){
                                    list.add(asFieldHook((FieldInsnNode)node,"selectedItemId",true));
                                }else if(fieldCount > 3){
                                    break;
                                }
                                fieldCount++;
                            }
                            if(node instanceof JumpInsnNode && node.getOpcode() == GOTO){
                                node = ((JumpInsnNode) node).label;
                            }else{
                                node = node.getNext();
                            }

                        }
                    }else if(jn.firstNumber() != null && jn.firstNumber().number() == 25) {
                        int fieldCount = 0;
                        AbstractInsnNode node = jn.insn();
                        while(node != null){
                            if(node instanceof FieldInsnNode){
                                if(fieldCount == 7){
                                    list.add(asFieldHook((FieldInsnNode)node,"selectedSpellName",true));
                                }else if(fieldCount > 7){
                                    break;
                                }
                                fieldCount++;
                            }
                            if(node instanceof JumpInsnNode && node.getOpcode() == GOTO){
                                node = ((JumpInsnNode) node).label;
                            }else{
                                node = node.getNext();
                            }

                        }
                    }
                }
            });

            return list;
        }
    }

    public class MinimapHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String h, regex = ";.{0,1};V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode m = identifyMethod(mn, false, "ldc 120.0");

            h = findField(m, true, true, 1, 's', "ldc 120.0");
            list.add(asFieldHook(h, "mapScale"));

            h = findField(m, true, true, 1, 's', "ldc 30.0");
            list.add(asFieldHook(h, "mapOffset"));

            h = findField(m, true, true, 1, 's', "ldc 20.0");
            list.add(asFieldHook(h, "mapAngle"));

            /*
            regex = ";\\[L" + findObfClassName("Widget") + ";.{6,8};V";
            mn = findMethods(Context.current().getClassNodes(),regex,true);
            m = identifyMethod(mn,false,"bipush 63");
            findField3()
            AbstractInsnNode[] i = followJump(m,"BIPUSH 63",300);
            System.out.println(m.owner + " " + m.name + " " + i.length + "\t" + Arrays.toString(i));
            System.out.println(findField(m, true, true, 1, 's', "bipush 63"));
            System.out.println(findField(m, true, true, 2, 's', "bipush 63"));
            System.out.println(findField(m, true, true, 1, 's', "bipush"));
            System.out.println(findField(m, true, true, 2, 's', "bipush"));
            */
            return list;
        }
    }

    public class CameraHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String h, regex = ";III\\w{0,1};V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode m = startWithBc(new String[]{"iload", "sipush", "if_icmplt"}, mn)[0];

            h = findNearIns(m, "invokestatic", "put", "get");
            list.add(asFieldHook(h, "plane"));

            h = findField(m, true, false, 1, 's', "isub", "istore 0");
            list.add(asFieldHook(h, "cameraX"));

            h = findField(m, true, false, 1, 's', "isub", "istore 1");
            list.add(asFieldHook(h, "cameraY"));

            h = findField(m, true, false, 1, 's', "imul", "isub", "istore 4");
            list.add(asFieldHook(h, "cameraZ"));

            h = findField(m, true, false, 1, 's', "iaload", "istore 7");
            list.add(asFieldHook(h, "cameraYaw"));

            h = findField(m, true, false, 1, 's', "iaload", "istore 5");
            list.add(asFieldHook(h, "cameraPitch"));

            return list;
        }
    }

    public class MenuScreenHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {

            String h, regex = ";I.{0,2};V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            // The actual pattern is getstatic, getstatic, invokevirtual but
            // because the we inline strings and the 2nd getstatic is a
            // string constant, the pattern has to be changed.
            String[] p = {"getstatic", "ldc", "invokevirtual", "istore"};
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);

            MethodNode[] m = startWithBc(p, mn);

            AbstractInsnNode[] ins = followJump(m[0], 323);

            final String[] pattern = {"if_icmple", "iload 6", "ifge", "iconst_1"};

            h = findField(ins, true, true, 1, 's', pattern);
            list.add(asFieldHook(h, "menuOpen"));

            h = findField(ins, true, true, 2, 's', pattern);
            list.add(asFieldHook(h, "menuX"));

            h = findField(ins, true, true, 3, 's', pattern);
            list.add(asFieldHook(h, "menuY"));

            h = findField(ins, true, true, 4, 's', pattern);
            list.add(asFieldHook(h, "menuWidth"));

            h = findField(ins, true, true, 5, 's', pattern);
            list.add(asFieldHook(h, "menuSize"));

            h = findField(ins, true, true, 6, 's', pattern);
            list.add(asFieldHook(h, "menuHeight"));

            return list;
        }
    }

    public class ClientArrayHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String h,loopCycle,actor, regex = ";;V";
            String[] p = {"iconst_1", "putstatic"};
            String[] pattern = {"bipush 9", "iconst_2", "iastore"};
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);
            MethodNode[] ms = startWithBc(p, mn);

            MethodNode m = identifyMethod(ms, false, pattern);
            AbstractInsnNode[] ins = followJump(m, 323);

            String[] pat = {"iconst_1", "putstatic", "iconst_0"};
            String[] r = {"aconst_null", "putstatic .* Ljava/lang/String;", "iconst_0"};

            r = new String[]{"putstatic .*", "new .*", "dup", "bipush 8"};
            h = findField(ins, true, true, 2, 's', r);
            list.add(asFieldHook(h, "widgetNodes"));

            h = findField(ins, true, true, 1, 's', "sipush 500", "anewarray .*");
            list.add(asFieldHook(h, "menuActions"));

            h = findField(ins, true, true, 9, 's', "sipush 500", "anewarray .*");
            list.add(asFieldHook(h, "spellSelected"));

            h = findField(ins, true, true, 7, 's', "sipush 500", "anewarray .*");
            list.add(asFieldHook(h, "selectionState"));

            h = findField(ins, true, true, 8, 's', "sipush 500", "anewarray .*");
            list.add(asFieldHook(h, "selectedItemName"));

            h = findField(ins, true, true, 2, 's', "sipush 500", "anewarray .*");
            list.add(asFieldHook(h, "menuOptions"));

            //i dont like this :(
            h = findField(ins, true, true, 4, 's', "sipush 500", "newarray 10");
            list.add(asFieldHook(h, "menuVariable"));

            loopCycle = findField(ins, false, true, 2, 's', pat);
            list.add(asFieldHook(loopCycle, "loopCycle"));

            h = findField(ins, true, true, 2, 's', "iconst_1");
            list.add(asFieldHook(h, "currentWorld"));

            h = findField(ins, true, true, 8, 's', "iconst_1");
            list.add(asFieldHook(h, "gameState"));

            h = findField(ins, true, true, 1, 's', "bipush 25", "newarray 10");
            list.add(asFieldHook(h, "currentLevels"));

            h = findField(ins, true, true, 2, 's', "bipush 25", "newarray 10");
            list.add(asFieldHook(h, "realLevels"));

            h = findField(ins, true, true, 3, 's', "bipush 25", "newarray 10");
            list.add(asFieldHook(h, "skillsExp"));

            //h = findField(ins, true, true, 1, 's', r);
            //list.add(asFieldHook(h, "selectedItem"));

            try {
                MethodNode renderScreenMethod = identifyMethod(findMethods(Context.current().getClassNodes(), ";.*;.*;V", true), false, "ldc Fps:");
                h = findField(renderScreenMethod, true, true, 1, 'f', "getstatic " + loopCycle + " I", "ldc .*", "imul", "putfield " + findObfClassName("Widget") + ".* I");
                getAnalyser("Widget").getFoundHook().fields().add(asFieldHook(h, "loopCycle"));

                actor = findObfClassName("Actor");
                MethodNode renderHitBarMethod = identifyMethod(findMethods(Context.current().getClassNodes(), ";.*" + "L" + actor + ";III" + ".*;V", true),false,"getstatic " + loopCycle + " I");
                h = findField(renderHitBarMethod,true,false,1,'f',"getfield .*" + actor + ".* I","ldc .*","imul","getstatic " + loopCycle + " I");
                getAnalyser("Actor").getFoundHook().fields().add(asFieldHook(h, "combatTime"));
            }catch (Exception ignore){
                ignore.printStackTrace();
            }

            return list;
        }
    }

    public class TileInfoHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String hook, regex = ";III\\w{0,1};I";
            List<FieldHook> list = new ArrayList<FieldHook>();
            String bytesPattern = "getstatic \\w*.\\w* \\[\\[\\[B";
            String heightPattern = "getstatic \\w*.\\w* \\[\\[\\[I";

            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode method = identifyMethod(mn, false, "bipush 103");

            hook = findField(method, true, false, 1, 's', bytesPattern);
            list.add(asFieldHook(hook, "tileSettings"));

            hook = findField(method, true, false, 1, 's', heightPattern);
            list.add(asFieldHook(hook, "tileHeights"));

            return list;
        }
    }

    public class BaseXYHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String obj = "L" + findObfClassName("Actor");
            String h, regex = ";\\w*" + obj + ";" + "\\w*;V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            String mPattern = "invokestatic java/lang/Math.atan2 (DD)D";
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode method = identifyMethod(mn, false, mPattern);
            AbstractInsnNode[] ins = followJump(method, 120);

            h = findField(ins, false, false, 1, 's', "isub", "isub", "istore");
            list.add(asFieldHook(h, "baseX"));

            h = findField(ins, false, true, 1, 's', "isub", "isub", "istore");
            list.add(asFieldHook(h, "baseY"));

            return list;
        }
    }

    public class SettingsHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String hook, r = ";;V";
            String[] pat = {"bipush", "newarray"};
            String[] pat2 = {"sipush 2000", "newarray 10"};
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), r, true);
            MethodNode[] ms = startWithBc(pat, mn);
            MethodNode m = identifyMethod(ms, false, pat2);

            hook = findField(m, true, true, 1, 's', pat2);
            list.add(asFieldHook(hook, "clientSettings"));

            hook = findField(m, true, true, 2, 's', pat2);
            list.add(asFieldHook(hook, "widgetsSettings"));

            return list;
        }
    }

    public class WidgetPositionXY implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            ClassHook widgetHook = getAnalyser("Widget").getFoundHook();
            FieldHook relativeX = widgetHook.fbyRefactoredName("relativeX");
            FieldHook relativeY = widgetHook.fbyRefactoredName("relativeY");
            MethodNode[] methods = findMethods(Context.current().getClassNodes(), ";\\[L" + widgetHook.obfuscated() + ";I{8,9};V", true);

            for (MethodNode method : methods) {
                TreeBuilder builder = new TreeBuilder();
                NodeTree build = builder.build(method);
                build.accept(new NodeVisitor() {
                    @Override
                    public void visitAny(AbstractNode n) {
                        if (n.opcode() == Opcodes.IASTORE) {
                            AbstractNode first = n.child(0);
                            if (first instanceof FieldMemberNode) {
                                List<ArithmeticNode> fieldMemberNodes = n.t_findChildren(Opcodes.IADD);
                                if (fieldMemberNodes != null && fieldMemberNodes.size() == 1) {
                                    List<FieldMemberNode> widgetField = fieldMemberNodes.get(0).t_deepFindChildren(Opcodes.GETFIELD);
                                    if (widgetField.size() == 1) {
                                        FieldMemberNode fieldMemberNode = widgetField.get(0);
                                        if (fieldMemberNode.owner().equals(widgetHook.obfuscated())) {
                                            if (fieldMemberNode.name().equals(relativeX.obfuscated())) {
                                                list.add(asFieldHook(((FieldMemberNode) first).fin(), "widgetPositionsX"));
                                            } else if (fieldMemberNode.name().equals(relativeY.obfuscated())) {
                                                list.add(asFieldHook(((FieldMemberNode) first).fin(), "widgetPositionsY"));
                                            } else {
                                                throw new RuntimeException(fieldMemberNode.name());
                                            }
                                        }
                                    } else {
                                        throw new RuntimeException("Size = " + widgetField.size());
                                    }
                                }
                            }
                        }
                    }
                });
            }
            return list;
        }

    }

    public class HintArrowHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode client) {
            List<FieldHook> hooks = new ArrayList<FieldHook>();
            for (ClassNode classNode : Context.current().getClassNodes().values()) {
                for (MethodNode m : classNode.methods) {
                    if (identifyMethod(m,"ldc .*","imul", "iconst_2","if_icmplt") && identifyMethod(m,"ldc .*","imul", "bipush 6","if_icmpgt")) {
                        String h, id;
                        id = findField(m, true, false, 1, 's', "ldc .*","imul", "iconst_2","if_icmplt");
                        hooks.add(asFieldHook(id, "hintArrowType"));
                        h = findField(m, true, true, 3, 's', "getstatic " + id + " I", "ldc .*","imul", "iconst_1");
                        hooks.add(asFieldHook(h, "hintArrowNpcId"));
                        h = findField(m, true, true, 2, 's', "getstatic " + id + " I", "ldc .*","imul", "iconst_3");
                        hooks.add(asFieldHook(h, "hintArrowX"));
                        h = findField(m, true, true, 3, 's', "getstatic " + id + " I", "ldc .*","imul", "iconst_3");
                        hooks.add(asFieldHook(h, "hintArrowY"));
                        h = findField(m, true, true, 3, 's', "ldc .*", "putstatic " + id + " I");
                        hooks.add(asFieldHook(h, "hintArrowIconX"));
                        h = findField(m, true, true, 5, 's', "ldc .*", "putstatic " + id + " I");
                        hooks.add(asFieldHook(h, "hintArrowIconY"));
                        h = findField(m, true, true, 7, 's', "ldc .*", "putstatic " + id + " I");
                        hooks.add(asFieldHook(h, "hintArrowHeight"));
                        h = findField(m, true, true, 3, 's', "getstatic " + id + " I", "ldc .*","imul", "bipush 10");
                        hooks.add(asFieldHook(h, "hintArrowPlayerId"));
                        break;
                    }
                }
            }

            return hooks;
        }
    }

    // TODO: METHODS
    public class LoadDefinitionHook implements IMethodAnalyser {

        @Override
        public List<MethodHook> findMethods(ClassNode _unused) {
            List<MethodHook> list = new ArrayList<MethodHook>();
            // String npcClass = findObfClassName("NPC");
            // ClassNode cn = getClassNodeByRefactoredName("Renderable");
            // MethodNode[] ms = getMethodNodes(cn.methods.toArray());
            // String playerClass = findObfClassName("Player");
            // MethodNode m = identifyMethod(ms, true, "aconst_null", "areturn");
            String v = "L" + findObfClassName("ItemDefinition") + ";";
            // String mn
            String t = "L" + findObfClassName("ObjectDefinition") + ";";
            MethodNode mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + t);
            MethodHook mhook = getAnalyser("ObjectDefinition").asMethodHook(mNode, "loadObjDefinition").var(Constants.METHOD_TYPE, Constants.CALLBACK);
            list.add(mhook);
            //
            mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + v);
            mhook = getAnalyser("ItemDefinition").asMethodHook(mNode, "loadItemDefinition").var(Constants.METHOD_TYPE, Constants.CALLBACK);
            list.add(mhook);
            //
            // mn = npcClass + "." + m.name;
            mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + v);
            //
            //mn = playerClass + "." + m.name;
            //mhook = getAnalyser("Model").asMethodHook(mn, "getPlayerModel").var(Constants.METHOD_TYPE, Constants.CALLBACK);
            //list.add(mhook);

            // these hooks are actually method hook, I think we should make a
            // method format look

            // String name = "ObjectDefinition";
            // AbstractClassAnalyser analyser = getAnalyser(name);
            // ClassNode aCn = analyser.getFoundClass();
            // String defName = String.format("L%s;", aCn.name);
            // for (ClassNode cn : Context.current().getClassNodes().values()) {
            // for (MethodNode m : cn.methods) {
            // if (!Modifier.isStatic(m.access))
            // continue;
            // String desc = m.desc;
            // if (desc.startsWith("(I") && desc.endsWith(defName)) {
            // MethodHook hook = getAnalyser(name).asMethodHook(m, "loadObjDefinition");
            // list.add(hook);
            // }
            // }
            // }

            return list;
        }

        private MethodNode findMethodNode(Map<String, ClassNode> nodes, String r) {
            String g = "[()]";
            Iterator<ClassNode> it = nodes.values().iterator();
            while (it.hasNext()) {
                ClassNode cn = it.next();
                for (Object m : cn.methods)
                    if ((Modifier.isStatic(((MethodNode) m).access)))
                        if (((MethodNode) m).desc.replaceAll(g, ";").matches(r))
                            return (MethodNode) m;
            }
            return null;
        }

//        private String findMethod(Map<String, ClassNode> nodes, String r) {
//            String g = "[()]", result = null;
//            Iterator<ClassNode> it = nodes.values().iterator();
//            while (it.hasNext()) {
//                ClassNode cn = it.next();
//                for (Object m : cn.methods)
//                    if ((Modifier.isStatic(((MethodNode) m).access)))
//                        if (((MethodNode) m).desc.replaceAll(g, ";").matches(r))
//                            return cn.name + "." + ((MethodNode) m).name;
//            }
//            return result;
//        }
    }

    public class WidgetsHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            String t = "[[L" + findObfClassName("Widget") + ";";
            String widgetField = identifyField(Context.current().getClassNodes(), t);
            list.add(asFieldHook(widgetField, "widgets"));

            return list;
        }
    }

    public class GroundItemsHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String t = "[[[L" + findObfClassName("Deque") + ";";
            String p = identifyField(Context.current().getClassNodes(), t);
            List<FieldHook> list = new ArrayList<FieldHook>();
            list.add(asFieldHook(p, "groundItems"));

            return list;
        }
    }

    public class CurrentRegionHook implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();

            String type = "L" + findObfClassName("Region") + ";";
            String p = identifyField(Context.current().getClassNodes(), type);
            list.add(asFieldHook(p, "region"));

            return list;
        }
    }

    public class ReportMethodHookAnalyser implements IMethodAnalyser {

        @Override
        public List<MethodHook> findMethods(ClassNode _cn) {
            List<MethodHook> hooks = new ArrayList<MethodHook>();
            for (ClassNode cn : Context.current().getClassNodes().values()) {
                for (MethodNode m : cn.methods) {
                    if (m.desc.startsWith("(Ljava/lang/Throwable;Ljava/lang/String;)") && m.desc.contains(")L")) {
                        MethodHook mhook = asMethodHook(m, "reportException").var(Constants.METHOD_TYPE, Constants.PATCH);
                        hooks.add(mhook);
                        VarInsnNode beforeReturn = null;
                        try {
                            for (AbstractInsnNode ain : m.instructions.toArray()) {
                                if (ain.getOpcode() == ARETURN) {
                                    if (beforeReturn != null)
                                        System.err.println("WTF BOI");
                                    beforeReturn = (VarInsnNode) ain.getPrevious();
                                }
                            }
                            /* 1. Generate the event creation. 2. Call the dispatch method. */

                            // InsnList objCreateList = new InsnList();
                            // objCreateList.add(EventCallGenerator.generateEventCreate("org/zbot/api/event/ErrorEvent", "(Lorg/zbot/accessors/IWrappedException;)V",
                            // new VarInsnNode(beforeReturn.getOpcode(), beforeReturn.var), // load the raw object
                            // // cast it to an IWrappedException
                            // new TypeInsnNode(CHECKCAST, APIGenerator.ACCESSOR_BASE + APIGenerator.API_CANONICAL_NAMES.get("WrappedException"))));
                            // InsnList newInsns = EventCallGenerator.generateDispatch(objCreateList);

                            InsnList newInsns = new InsnList();
                            newInsns.add(EventCallGenerator.generatePrintLn(new LdcInsnNode("[Client] Create error.")));
                            newInsns.add(EventCallGenerator.generatePrintLn(new VarInsnNode(ALOAD, 1)));

                            newInsns.add(new VarInsnNode(ALOAD, 0));
                            newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V", false));

                            InsnList list2 = new InsnList();
                            list2.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
                            list2.add(new InsnNode(DUP));

                            list2.add(new LdcInsnNode("[Client] "));
                            list2.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));

                            list2.add(new VarInsnNode(ALOAD, 0));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getCanonicalName", "()Ljava/lang/String;", false));
                            list2.add(new MethodInsnNode(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));

                            list2.add(new LdcInsnNode(" "));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));

                            list2.add(new VarInsnNode(ALOAD, 0));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;", false));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
                            list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "valueOf", "()Ljava/lang/String;", false));

                            newInsns.add(EventCallGenerator.generatePrintLn(list2.toArray()));

                            InsnList mInsns = m.instructions;
                            mInsns.insertBefore(beforeReturn, newInsns);
                            mhook.insns(mInsns);
                            mhook.var(Constants.MAX_STACK, "7");
                            mhook.var(Constants.MAX_LOCALS, Integer.toString(m.maxLocals));
                            mInsns.reset();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            //for (MethodNode m : getClassNodeByRefactoredName("ExceptionReporter").methods) {
            //
            //}

            return hooks;
        }
    }

    public class ProccessActionMethodHookAnalyser implements IMethodAnalyser {

        @Override
        public List<MethodHook> findMethods(ClassNode _cn) {
            List<MethodHook> hooks = new ArrayList<MethodHook>();
            String descStart = "(IIIILjava/lang/String;Ljava/lang/String;II";
            for (ClassNode cn : Context.current().getClassNodes().values()) {
                for (MethodNode m : cn.methods) {
                    if (m.desc.startsWith(descStart)) {
                        InsnList insns = new InsnList();
                        final String sb = "java/lang/StringBuilder";
                        final String intAppendDesc = "(I)Ljava/lang/StringBuilder;";
                        final String stringAppendDesc = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
                        final String append = "append";

                        insns.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                        insns.add(new TypeInsnNode(NEW, sb));
                        insns.add(new InsnNode(DUP));

                        insns.add(new LdcInsnNode("[doAction] Op: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, "<init>", "(Ljava/lang/String;)V", false));

                        insns.add(new VarInsnNode(ILOAD, 2));
                        insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
                        insns.add(new LdcInsnNode(", Arg1: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ILOAD, 0));
                        insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
                        insns.add(new LdcInsnNode(", Arg2: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ILOAD, 1));
                        insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
                        insns.add(new LdcInsnNode(", Arg0: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ILOAD, 3));
                        insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
                        insns.add(new LdcInsnNode(", Action: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ALOAD, 4));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new LdcInsnNode(", Target: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ALOAD, 5));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));
                        insns.add(new LdcInsnNode(", var6: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ILOAD, 6));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, intAppendDesc, false));
                        insns.add(new LdcInsnNode(", var7: "));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

                        insns.add(new VarInsnNode(ILOAD, 7));
                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, intAppendDesc, false));

                        insns.add(new MethodInsnNode(INVOKESPECIAL, sb, "toString", "()Ljava/lang/String;", false));
                        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));

                        MethodHook h = asMethodHook(m, "processAction")
                                .var(Constants.METHOD_TYPE, Constants.PATCH)
                                .var(Constants.PATCH_POSITION, Constants.START);
                        h.insns(insns);

                        hooks.add(h);
                    }
                }
            }
            return hooks;
            // static final void processAction (
            // int arg1, int arg2, int opcode, int arg0, String action, String target,
            // int mouseX, int mouseY, xxx DUMMY
            // )
        }
    }

    public class CredentialAnalyser implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();

            //TODO: FIX
            /*
            final String methodPattern = ";L" + findObfClassName("Gameshell") + ";.{0,1};V";
			MethodNode[] filteredMethods = findMethods(Context.current().getClassNodes(), methodPattern, true);
			MethodNode method = startWithBc(new String[] { "getstatic", "ifeq" }, filteredMethods)[0];
			final String[] fieldPattern = { "iconst_0", "putstatic", "ldc", "putstatic", "ldc", "putstatic", "iconst_0" };

			String h = findField(method, false, true, 2, 's', fieldPattern);
			list.add(asFieldHook("username()", h));

			h = findField(method, false, true, 3, 's', fieldPattern);
			list.add(asFieldHook("password()", h));*/

            return list;
        }
    }

    public static void test(Throwable t) {
        System.out.println("[Client] " + t.getClass().getCanonicalName() + " " + t.getMessage());
    }

    static final void processAction(int arg1, int arg2, int opcode, int arg0, String action, String target, int mouseX, int mouseY, int DUMMY) {
        System.out.println("[doAction] Op: " + opcode + ", Arg1: " + arg1 + ", Arg2: " + arg2 + ", Arg0: " + arg0 + ", Action: " + action + ", Target: "
                + target + ", var6: " + mouseX + ", var7: " + mouseY);
    }
}