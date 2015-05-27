package org.nullbool.impl.analysers.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "getModelHeight&I" }, methods = {})
public class RenderableAnalyser extends ClassAnalyser {

	public RenderableAnalyser() throws AnalysisException {
		super("Renderable");
	}

	public class HeightHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String hook = getFieldOfType(cn, "I", false);
			list.add(asFieldHook(hook, "getModelHeight"));
			return list;
		}
	}

	@Override
	protected boolean matches(ClassNode cn) {
		/*String[] p = Context.current().getPattern("Renderable");
		MethodNode[] m = getMethodNodes(cn.methods.toArray());
		MethodNode[] mn = { searchMethodDesc(m, "(IIIIIIIII)V") };
		String superClassName = findObfClassName("DualNode");
		boolean rightSuperClass = cn.superName.equals(superClassName);
		boolean goodMeth = mn[0] != null && identifyMethod(mn, true, p) != null;
		return goodMeth && rightSuperClass;*/
		ClassNode nodeClass = getClassNodeByRefactoredName("Node");
		Set<ClassNode> supers = Context.current().getClassTree().getSupers(cn);
		if(supers.size() != 2)
			return false;
		if(!supers.contains(nodeClass))
			return false;
		
		for(MethodNode m : cn.methods) {
			if(m.desc.startsWith("(IIIIIIIII") && m.desc.endsWith("V")) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new HeightHook());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
}