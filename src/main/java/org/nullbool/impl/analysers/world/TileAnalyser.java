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
@SupportedHooks(fields = { "objects&[GameObject", "groundObjects&GroundObject", "groundDecorations&GroundDecoration", "wallObjects&WallObject",
		"wallDecorations&WallDecoration", }, methods = {})
public class TileAnalyser extends ClassAnalyser {

	public TileAnalyser() throws AnalysisException {
		super("Tile");
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new GameObjectsHook());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	@Override
	public boolean matches(ClassNode c) {
		String cnType = "[[[L" + c.name + ";";
		String node = findObfClassName("Node");
		ClassNode cn = getClassNodeByRefactoredName("Region");
		boolean rightSuperClassValidation = c.superName.equals(node);
		boolean fieldIsInRegion = containFieldOfType(cn, cnType);
		return rightSuperClassValidation && fieldIsInRegion;
	}

	public class GameObjectsHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			String t = "[L" + findObfClassName("GameObject") + ";";
			String hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "objects"));

			t = "L" + findObfClassName("GroundObject") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "groundObjects"));

			t = "L" + findObfClassName("GroundDecoration") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "groundDecorations"));

			t = "L" + findObfClassName("WallObject") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "wallObjects"));

			t = "L" + findObfClassName("WallDecoration") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "wallDecorations"));

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