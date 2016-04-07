package org.nullbool.impl.r105;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.analysers.client.grandexchange.ExchangeOfferAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xerion on 24-2-2016.
 */
public class ExchangeOfferAnalyser105 extends ExchangeOfferAnalyser {

    @Override
    public String[] supportedFields() {
        return new Builder<String>(super.supportedFields())
                .addAll("id&I", "amount&I", "price&I", "sold&I","spent&I", "progress&B").asArray(new String[0]);
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return new Builder<IFieldAnalyser>().add(new CompleteHooks());
    }

    public class CompleteHooks implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] ms = getMethodNodes(cn.methods.toArray());
            for(MethodNode m : ms) {
                if(!m.name.equals("<init>") || !m.desc.contains("L"+ findObfClassName("Buffer") + ";")){
                    continue;
                }

                AbstractInsnNode[] i = followJump(m, 70);

                String h;
                h = findField(i, false, true, 1, 'f', "putfield");
                list.add(asFieldHook(h, "progress"));

                h = findField(i, false, true, 2, 'f', "putfield");
                list.add(asFieldHook(h, "id"));

                h = findField(i, false, true, 3, 'f', "putfield");
                list.add(asFieldHook(h, "price"));

                h = findField(i, false, true, 4, 'f', "putfield");
                list.add(asFieldHook(h, "amount"));

                h = findField(i, false, true, 5, 'f', "putfield");
                list.add(asFieldHook(h, "sold"));

                h = findField(i, false, true, 6, 'f', "putfield");
                list.add(asFieldHook(h, "spent"));
            }
            return list;
        }
    }

}
