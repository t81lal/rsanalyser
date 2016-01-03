/**
 * pi-rs, a generic framework for loading Java Applets in a contained environment.
 * Copyright (C) 2015  NullBool
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nullbool.impl.r90;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.impl.analysers.entity.ActorAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.*;
import org.topdank.banalysis.filter.Filter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Aug 2015 12:20:04
 * <p>
 * move method was removed
 * queueposition method was removed
 * (thats how we used to hook the queue stuff)
 */
public class ActorAnalyser90 extends ActorAnalyser {

    public ActorAnalyser90() throws AnalysisException {
        super();
    }

    @Override
    public String[] supportedMethods() {
        return new Builder<String>(super.supportedMethods()).remove(new Filter<String>() {
            @Override
            public boolean accept(String t) {
                return t.contains("queuePosition") || t.contains("move");
            }
        }).asArray(new String[0]);
    }

    @Override
    public String[] supportedFields() {
        return new Builder<String>(super.supportedFields()).replace(new Filter<String>() {
            @Override
            public boolean accept(String t) {
                return t.equals("queueRun&[Z");
            }
        }, "queueRun&[B").asArray(new String[0]);
    }

    @Override
    protected Builder<IMethodAnalyser> registerMethodAnalysers() {
        return super.registerMethodAnalysers().remove(new Filter<IMethodAnalyser>() {
            @Override
            public boolean accept(IMethodAnalyser t) {
                return t instanceof QueuePositionMethodAnalyser || t instanceof MoveMethodAnalyser;
            }
        });
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return super.registerFieldAnalysers().replace(new Filter<IFieldAnalyser>() {
            @Override
            public boolean accept(IFieldAnalyser t) {
                return t instanceof QueueFieldsAnalyser;
            }
        }, new QueueFieldsAnalyser90()).replace(new Filter<IFieldAnalyser>() {
            @Override
            public boolean accept(IFieldAnalyser t) {
                return t instanceof HealthAndDamageHooks;
            }
        }, new HealthAndDamageHooks90()).add(new OverheadMessageHook90());
    }

    public class QueueFieldsAnalyser90 extends QueueFieldsAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode actor) {
            List<FieldHook> list = new ArrayList<FieldHook>();

            for (ClassNode cn : Context.current().getClassNodes().values()) {
                for (MethodNode m : cn.methods) {
                    if (Modifier.isStatic(m.access)) {
                        if (m.desc.contains(actor.name)) {
                            ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();

                            boolean found = false;
                            while (iterator.hasNext()) {
                                AbstractInsnNode next = iterator.next();
                                if (next instanceof IntInsnNode) {
                                    if (next.getOpcode() == SIPUSH && ((IntInsnNode) next).operand == 13184) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            final FieldHook[] queueXHook = {null};
                            final FieldHook[] queueYHook = {null};
                            final FieldHook[] queueLengthHook = {null};
                            if (found) {
                                FieldHook localX = getFoundHook().fields().stream().filter(v -> v.refactored().equals("localX")).findFirst().get();
                                FieldHook localY = getFoundHook().fields().stream().filter(v -> v.refactored().equals("localY")).findFirst().get();
                                NodeVisitor nv = new NodeVisitor() {
                                    @Override
                                    public void visitField(FieldMemberNode fmn) {
                                        if (fmn.putting() && fmn.owner().equals(actor.name) && fmn.desc().equals("I")) {
                                            AbstractInsnNode previous = fmn.insn().getPrevious();
                                            for (int i = 10; i >= 0 && previous != null; i--) {
                                                if (previous.getOpcode() == GETFIELD) {
                                                    final FieldInsnNode node = (FieldInsnNode) previous;
                                                    if (node.owner.equals(actor.name) && node.desc.equals("[I") && node.getNext().getOpcode() == ICONST_0) {
                                                        if (fmn.name().equals(localX.obfuscated()) && queueXHook[0] == null) {
                                                            list.add(queueXHook[0] = asFieldHook(node, "queueX"));
                                                        } else if (fmn.name().equals(localY.obfuscated()) && queueYHook[0] == null) {
                                                            list.add(queueYHook[0] = asFieldHook(node, "queueY"));
                                                        }
                                                    }
                                                }
                                                previous = previous.getPrevious();
                                            }
                                        }
                                    }
                                };
                                TreeBuilder tb = new TreeBuilder();
                                NodeTree tree = tb.build(m);
                                tree.accept(nv);

                                if (queueXHook[0] != null || queueYHook[0] != null) {
                                    FieldHook arrayHook = queueXHook[0] != null ? queueXHook[0] : queueYHook[0];

                                    tree.accept(new NodeVisitor() {
                                        @Override
                                        public void visitField(FieldMemberNode fmn) {
                                            if (fmn.getting() && fmn.owner().equals(actor.name) && fmn.name().equals(arrayHook.obfuscated())) {
                                                if (queueLengthHook[0] == null) {
                                                    AbstractInsnNode next = fmn.insn().getNext();
                                                    for (int i = 0; i < 8 && next != null; i++) {
                                                        if (next.getOpcode() == GETFIELD) {
                                                            FieldInsnNode node = (FieldInsnNode) next;
                                                            if (node.desc.equals("I") && node.owner.equals(actor.name)) {
                                                                list.add(queueLengthHook[0] = asFieldHook(node, "queueLength"));
                                                                break;
                                                            }
                                                        }
                                                        next = next.getNext();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            String hook = identify(actor, "[B", 'f');
            list.add(asFieldHook(hook, "queueRun"));

            return list;
        }
    }

    private class HealthAndDamageHooks90 implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            String actorObj = findObfClassName("Actor");
            String r = ";.*" + "L" + actorObj + ";III" + ".*;V";
            MethodNode[] ms = findMethods(Context.current().getClassNodes(), r, true);
            for (MethodNode methodNode : ms) {
                NodeTree tree = new TreeBuilder().build(methodNode);
                tree.accept(new NodeVisitor() {

                    @Override
                    public void visitOperation(ArithmeticNode an) {
                        if (an.opcode() == Opcodes.IDIV) {
                            AbstractNode parent = an.parent();
                            List<FieldMemberNode> abstractNodes = parent.t_deepFindChildren(Opcodes.GETFIELD);
                            if (abstractNodes != null && abstractNodes.size() == 2) {
                                FieldMemberNode healthMemberNode = abstractNodes.get(0);
                                FieldMemberNode maxHealthMemberNode = abstractNodes.get(1);
                                fieldHooks.add(asFieldHook(healthMemberNode.fin(), "health"));
                                fieldHooks.add(asFieldHook(maxHealthMemberNode.fin(), "maxHealth"));
                            }
                        }
                    }

                    @Override
                    public void visitMethod(MethodMemberNode mmn) {
                        //hit damages
                        if (mmn.owner().equals("java/lang/Integer") && mmn.name().equals("toString")) {
                            List<FieldMemberNode> abstractNodes = mmn.t_deepFindChildren(Opcodes.GETFIELD);
                            if (abstractNodes != null && abstractNodes.size() == 1) {
                                FieldMemberNode hitDamagesMemberNode = abstractNodes.get(0);
                                if (hitDamagesMemberNode.owner().equals(actorObj) && hitDamagesMemberNode.desc().equals("[I")) {//sanity check
                                    fieldHooks.add(asFieldHook(hitDamagesMemberNode.fin(), "hitDamages"));
                                } else {
                                    throw new RuntimeException("hitDamageMemberNode = " + hitDamagesMemberNode);
                                }
                            }
                        }
                    }

                    @Override
                    public void visitField(FieldMemberNode fmn) {
                        //hittypes
                        /*
                            getstatic      ap.eb [Lco;
	                        iaload
		                        getfield       aj.ag [I <- dis
			                        aload          #0
		                        iload          #9
                         */
                        if (fmn.opcode() == Opcodes.GETSTATIC &&
                                Type.getType(fmn.desc()).getSort() == Type.ARRAY && Type.getType(fmn.desc()).getDimensions() == 1
                                && Context.current().getClassNodes().containsKey(Type.getType(fmn.desc()).getClassName().replace("[", "").replace("]", ""))
                                && fmn.parent().opcode() == Opcodes.AALOAD) {
                            List<FieldMemberNode> fieldMemberNodes = fmn.parent().t_deepFindChildren(Opcodes.GETFIELD);
                            if (fieldMemberNodes != null && fieldMemberNodes.size() == 1) {
                                FieldMemberNode fieldMemberNode = fieldMemberNodes.get(0);
                                if (fieldMemberNode.owner().equals(actorObj) && fieldMemberNode.desc().equals("[I")) {
                                    fieldHooks.add(asFieldHook(fieldMemberNode.fin(), "hitTypes"));
                                }
                            }
                        }
                    }
                });
            }
            return fieldHooks;
        }
    }

    private class OverheadMessageHook90 implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            List<FieldNode> stringFields = cn.fields.stream().filter(v -> !Modifier.isStatic(v.access) && v.desc.equals("Ljava/lang/String;")).collect(Collectors.toList());
            if (stringFields.size() == 1) {
                fieldHooks.add(asFieldHook(stringFields.get(0), "message"));
            } else {
                throw new RuntimeException("Multiple string fields on actor node " + cn.name);
            }
            return fieldHooks;
        }
    }
}