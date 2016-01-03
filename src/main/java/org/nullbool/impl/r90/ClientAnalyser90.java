package org.nullbool.impl.r90;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.r82.ClientAnalyser82;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by polish on 08.12.15.
 */
public class ClientAnalyser90 extends ClientAnalyser82 {

    public ClientAnalyser90() throws AnalysisException {
        super();
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return super.registerFieldAnalysers()
                .replace(iFieldAnalyser -> iFieldAnalyser.getClass().isAssignableFrom(BaseXYHooks.class), new BaseXYHooks90())
                .add(new Viewport90Hooks())
                .add(new CollisionMapHook())
                .add(new CrosshairColorHook());
    }

    @Override
    public String[] supportedFields() {
        return new Builder<String>(super.supportedFields()).addAll("resizableMode&Z", "collisionMaps&[CollisionMap", "crosshairColor&I").asArray(new String[2]);
    }

    private class BaseXYHooks90 implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> hooks = new ArrayList<>();

            for (ClassNode classNode : Context.current().getClassNodes().values()) {
                for (MethodNode method : classNode.methods) {
                    if (Modifier.isStatic(method.access)) {
                        NodeTree tree = new TreeBuilder().build(method);
                        NodeVisitor visitor = new NodeVisitor() {
                            @Override
                            public void visitOperation(ArithmeticNode an) {
                                if (an.rightShifting()) {
                                    NumberNode numberNode = an.firstNumber();
                                    if (numberNode != null && numberNode.number() == 6) {
                                        List<FieldMemberNode> next = an.t_deepFindChildren(Opcodes.GETSTATIC);
                                        if (next != null) {
                                            NumberNode number = an.nextNumber();
                                            if (number != null && number.number() == 14) {
                                                hooks.add(asFieldHook(next.get(0).fin(), "baseX"));
                                            } else if (number == null) {
                                                hooks.add(asFieldHook(next.get(0).fin(), "baseY"));
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        tree.accept(visitor);
                    }
                }
            }
            return hooks;
        }
    }

    private class Viewport90Hooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> hooks = new ArrayList<>();
            /*
                           if(var36 >= 2) {
                              client.lx = true;
                           } else {
                              client.lx = false;
                           }
             */
            String h = findField(Context.current().getCaseAnalyser().getMethod().instructions.toArray(), true, true, 1, 's', "iload .*", "iconst_2", "if_icmplt");
            hooks.add(asFieldHook(h, "resizableMode"));
            return hooks;
        }
    }

    private class CollisionMapHook implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            ClassHook collisionMap = getAnalyser("CollisionMap").getFoundHook();
            for (ClassNode classNode : Context.current().getClassNodes().values()) {
                for (MethodNode method : classNode.methods) {
                    String[] pattern = {"getstatic .*", "iload .*", "new " + collisionMap.obfuscated(), "dup", "bipush 104", "bipush 104"};
                    if (Modifier.isStatic(method.access) && identifyMethod(method, pattern)) {
                        String field = findField(method, true, true, 1, 's', pattern);
                        fieldHooks.add(asFieldHook(field, "collisionMaps"));
                    }
                }
            }
            return fieldHooks;
        }
    }

    private class CrosshairColorHook implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            for (ClassNode classNode : Context.current().getClassNodes().values()) {
                for (MethodNode method : classNode.methods) {
                    String[] pattern = {"bipush 100", "idiv", "iconst_4", "iadd"};
                    if (Modifier.isStatic(method.access) && identifyMethod(method, pattern)) {
                        String field = findField(method, true, true, 1, 's', "getstatic .* I", "ldc .*", "imul", "iconst_2");
                        fieldHooks.add(asFieldHook(field, "crosshairColor"));
                    }
                }
            }
            return fieldHooks;
        }
    }
}
