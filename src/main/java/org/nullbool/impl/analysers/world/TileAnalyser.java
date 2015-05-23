package org.nullbool.impl.analysers.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getObjects&[GameObject", "getGroundObjects&GroundObject", "getGroundDecorations&GroundDecoration", "getWallObjects&WallObject",
		"getWallDecorations&WallDecoration", }, methods = {})
public class TileAnalyser extends ClassAnalyser {

	public TileAnalyser() throws AnalysisException {
		super("Tile");
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new GameObjectsHook());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
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
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			String t = "[L" + findObfClassName("GameObject") + ";";
			String hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "getObjects"));

			t = "L" + findObfClassName("GroundObject") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "getGroundObjects"));

			t = "L" + findObfClassName("GroundDecoration") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "getGroundDecorations"));

			t = "L" + findObfClassName("WallObject") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "getWallObjects"));

			t = "L" + findObfClassName("WallDecoration") + ";";
			hook = getFieldOfType(cn, t, false);
			list.add(asFieldHook(hook, "getWallDecorations"));

			return list;
		}
	}
}