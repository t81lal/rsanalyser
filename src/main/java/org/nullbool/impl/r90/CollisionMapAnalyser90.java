package org.nullbool.impl.r90;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.*;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by polish on 13.12.15.
 */
@SupportedHooks(fields = {"flags&[[I"}, methods = {})
public class CollisionMapAnalyser90 extends ClassAnalyser {
    public CollisionMapAnalyser90() {
        super("CollisionMap");
    }

    @Override
    protected boolean matches(ClassNode cn) {
        for (MethodNode method : cn.methods) {
            if (!Modifier.isStatic(method.access) && identifyMethod(method, "iload .*", "ldc 131072", "iadd")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return new Builder<IFieldAnalyser>().add(new CollisionMapFieldsAnalyser());
    }

    @Override
    protected Builder<IMethodAnalyser> registerMethodAnalysers() {
        return new Builder<>();
    }

    @Override
    public Builder<IMultiAnalyser> registerMultiAnalysers() {
        return new Builder<>();
    }

    private class CollisionMapFieldsAnalyser implements IFieldAnalyser {
        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            ArrayList<FieldHook> fieldHooks = new ArrayList<>();
            List<FieldNode> flagsFields = cn.fields.stream().filter(v -> v.desc.equals("[[I")).collect(Collectors.toList());

            if (flagsFields.size() != 1) {
                throw new RuntimeException("flagsFields.count() = " + flagsFields.size()+" "+flagsFields);
            }

            fieldHooks.add(asFieldHook(flagsFields.get(0), "flags"));

            return fieldHooks;
        }
    }
}
