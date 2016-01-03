package org.nullbool.impl.r79;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 17:51:10
 */
public class ClientAnalyser79 extends ClientAnalyser {
	
	public ClientAnalyser79() throws AnalysisException {
		super();
	}

	@Override
	public String[] supportedFields() {
		return new Builder<String>(super.supportedFields()).addAll("viewportWidth&I", "viewportHeight&I", "viewportScale&I").asArray(new String[0]);
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return super.registerFieldAnalysers().add(new ViewPort79());
	}

    public class ViewPort79 implements IFieldAnalyser{

        //103 == bm.ac
        //102 == f.aq

        @Override
        public List<FieldHook> findFields(ClassNode cn) {
            String h, regex = ";IIII.w{0,1};V";
            List<FieldHook> list = new ArrayList<FieldHook>();
            MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
            MethodNode m = startWithBc(new String[]{"iload", "iconst_1", "if_icmpge"}, mn)[0];

            h =  findField(m, true, true, 1, 's', "ishl");
            list.add(asFieldHook(h, "viewportScale"));

            h = findField(m, true, true, 1, 's', "iload 2", "ldc .*", "imul");
            list.add(asFieldHook(h, "viewportWidth"));

            h =  findField(m, true, true, 1, 's', "iload 3", "ldc .*", "imul");
            list.add(asFieldHook(h, "viewportHeight"));

            return list;
        }
    }
}