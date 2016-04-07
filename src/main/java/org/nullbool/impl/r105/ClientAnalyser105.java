package org.nullbool.impl.r105;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.r90.ClientAnalyser90;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xerion on 1-2-2016.
 */
public class ClientAnalyser105 extends ClientAnalyser90{

    public ClientAnalyser105() throws AnalysisException {
    }


    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return super.registerFieldAnalysers()
                .addAll(new LoginUIStateAnalyser(),new ExchangeOffersAnalyser());
    }

    @Override
    public String[] supportedFields() {
        return new Builder<String>(super.supportedFields()).addAll("loginUIState&I", "exchangeOffers&[ExchangeOffer").asArray(new String[0]);
    }


    public class LoginUIStateAnalyser implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<>();
            for (org.objectweb.custom_asm.tree.ClassNode classNode : Context.current().getClassNodes().values()) {
                for (org.objectweb.custom_asm.tree.MethodNode m : classNode.methods) {
                    if (!m.name.equals("<clinit>") && identifyMethod(m, "ldc We suspect someone knows your password.")) {
                        list.add(asFieldHook(findField(m,false,false,1,'s',"putstatic"),"loginUIState"));
                    }
                }
            }
            return list;
        }
    }

    public class ExchangeOffersAnalyser implements IFieldAnalyser {

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            List<FieldHook> list = new ArrayList<>();
            String tempo = "[L" + findObfClassName("ExchangeOffer") + ";";
            String hook = identify(cn, tempo, 's');
            System.out.println(findObfClassName("ExchangeOffer") + " " + hook);
            list.add(asFieldHook(hook, "exchangeOffers"));
            return list;
        }
    }
}
