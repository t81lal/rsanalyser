package org.nullbool.impl.analysers.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.nullbool.pi.core.hook.api.FieldHook;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
@SupportedHooks(fields = {}, methods = {}) 
public class RasteriserAnalyser extends ClassAnalyser {

	public RasteriserAnalyser() {
		super("Rasteriser");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String dualName = getAnalyser("DualNode").getFoundClass().name;
		if(!cn.superName.equals(dualName))
			return false;
		Set<ClassNode> dels = Context.current().getClassTree().getDelegates(cn);
		if(dels.size() == 5) {
			return true;
		}
		return false;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldAnalyser());
	}
	
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	private class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			
			
			return list;
		}
	}
}