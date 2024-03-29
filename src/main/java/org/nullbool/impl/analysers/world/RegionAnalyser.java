package org.nullbool.impl.analysers.world;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "tiles&[[[Tile", }, methods = {})
public class RegionAnalyser extends ClassAnalyser {

	public RegionAnalyser() throws AnalysisException {
		super("Region");
	}

	@Override
	public boolean matches(ClassNode c) {
		boolean rightSuperClass = c.superName.contains("Object");
		boolean goodMethod = containMethodOfType(c, "(III[[[I)V");
		return rightSuperClass && goodMethod;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new TilesHook());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class TilesHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String t = "[[[L" + findObfClassName("Tile") + ";";
			String hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "tiles"));
			return list;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {

		return null;
	}
}