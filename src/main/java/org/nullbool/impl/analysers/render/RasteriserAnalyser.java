package org.nullbool.impl.analysers.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.FieldHook;

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
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new FieldAnalyser());
	}
	
	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
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