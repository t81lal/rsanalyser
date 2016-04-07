package org.nullbool.impl.r104;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.r90.ActorAnalyser90;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xerion on 10-1-2016.
 */
public class ActorAnalyser104 extends ActorAnalyser90 {

    public ActorAnalyser104() throws AnalysisException {
        super();
    }

    @Override
    protected Builder<IFieldAnalyser> registerFieldAnalysers() {
        return super.registerFieldAnalysers().replace(new Filter<IFieldAnalyser>() {
            @Override
            public boolean accept(IFieldAnalyser t) {
                return t instanceof QueueFieldsAnalyser;
            }
        }, new QueueFieldsAnalyser104());
    }

    public class QueueFieldsAnalyser104 extends QueueFieldsAnalyser90 {
        @Override
        public List<FieldHook> findFields(ClassNode actor) {
            String h, regex = ";IB\\w{0,1};V";
            ArrayList<FieldHook> list = new ArrayList<>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);
            MethodNode m = startWithBc(new String[]{"aload", "getfield", "iconst_0"}, mn)[0];
            h = findField(followJump(m,5),true,false,1,'f',"iconst_0").replace(m.owner.name + ".",actor.name + ".");
            list.add(asFieldHook(h,"queueX"));
            h = findField(followJump(m,5),true,true,1,'f',"iconst_0").replace(m.owner.name + ".",actor.name + ".");
            list.add(asFieldHook(h,"queueY"));
            h = findField(m,true,false,1,'f',"ldc .*","imul","bipush 9").replace(m.owner.name + ".",actor.name + ".");
            list.add(asFieldHook(h,"queueLength"));
            return list;
        }
    }
}
