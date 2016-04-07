package org.nullbool.impl.r90;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.r82.ClientAnalyser82;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.Opcodes;
import org.objectweb.custom_asm.commons.cfg.tree.NodeTree;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

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
                .add(new CrosshairColorHook())
                .add(new LowestPitch());
    }

    @Override
    public String[] supportedFields() {
        return new Builder<String>(super.supportedFields()).addAll("lowestPitch&I","resizableMode&Z", "collisionMaps&[CollisionMap", "crosshairColor&I").asArray(new String[2]);
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
            TreeBuilder builder = new TreeBuilder();
            builder.build(Context.current().getCaseAnalyser().getMethod()).accept(new NodeVisitor() {
                @Override
                public void visitJump(JumpNode jn) {
                    if(jn.firstNumber() != null && jn.firstNumber().number() == 5307){
                        org.objectweb.custom_asm.tree.AbstractInsnNode node = jn.insn();
                        org.objectweb.custom_asm.tree.FieldInsnNode booleanNode = null;
                        while(node != null){
                            if(node instanceof org.objectweb.custom_asm.tree.FieldInsnNode){
                                if(((org.objectweb.custom_asm.tree.FieldInsnNode) node).desc.equals("Z")){
                                    if(booleanNode != null && booleanNode.owner.equals(((FieldInsnNode) node).owner) && booleanNode.name.equals(((FieldInsnNode) node).name)){
                                        hooks.add(asFieldHook(booleanNode.owner +"."+booleanNode.name, "resizableMode"));
                                        break;
                                    }else{
                                        booleanNode = (org.objectweb.custom_asm.tree.FieldInsnNode) node;
                                    }
                                }
                            }else if(node instanceof MethodInsnNode && node.getOpcode() == INVOKESTATIC){
                                ClassNode classNode = Context.current().getClassNodes().get(((MethodInsnNode) node).owner);
                                if (classNode != null) {
                                    MethodNode method = classNode.getMethod(((MethodInsnNode) node).name, ((MethodInsnNode) node).desc);
                                    String v = findField(followJump(method, 15), true, true, 1, 's', "iload .*", "iconst_2", "if_icmplt", "iconst_0");
                                    if (v != null) {
                                        hooks.add(asFieldHook(v, "resizableMode"));
                                        break;
                                    }
                                }
                            }
                            node = node.getNext();
                        }
                    }
                }
            });
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
                    String[] pattern1 = {"bipush 100", "idiv", "iconst_4", "iadd"};
                    String[] pattern2 = {"iconst_4","getstatic .*","ldc .*","imul","bipush 100", "idiv", "iadd"};
                    if (Modifier.isStatic(method.access) && (identifyMethod(method, pattern1)) || identifyMethod(method, pattern2)) {
                        String field = findField(method, true, true, 1, 's', "getstatic .* I", "ldc .*", "imul", "iconst_2", "if_icmpne");
                        fieldHooks.add(asFieldHook(field, "crosshairColor"));
                    }
                }
            }
            return fieldHooks;
        }
    }

    private class LowestPitch implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            for (ClassNode classNode : Context.current().getClassNodes().values()) {
                for (MethodNode method : classNode.methods) {
                    if (identifyMethod(method, new String[] {"bipush 24","idiv"}) &&
                            identifyMethod(method,new String[] {"bipush 80","idiv"})) {
                        String field1 = findField(method, true, false, 1, 's', "bipush 24","idiv");
                        String field2 = findField(method, true, false, 1, 's', "bipush 80","idiv");
                        if(field1.equals(field2)) {
                            fieldHooks.add(asFieldHook(field1, "lowestPitch"));
                            return fieldHooks;
                        }
                    }
                }
            }
            return fieldHooks;
        }
    }
}
