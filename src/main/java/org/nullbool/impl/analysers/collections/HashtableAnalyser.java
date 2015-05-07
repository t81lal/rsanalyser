package org.nullbool.impl.analysers.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nullbool.api.analysis.AbstractClassAnalyser;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;

/**
 * @author Bibl
 * @author MalikDz
 */
@SupportedHooks(fields = { "getBuckets&[Node", "getHead&Node", "getTail&Node", }, methods = {})
public class HashtableAnalyser extends AbstractClassAnalyser {

	public HashtableAnalyser() throws AnalysisException {
		super("Hashtable");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String node = "\\[L" + findObfClassName("Node") + ";";
		boolean rightIntCount = getFieldOfTypeCount(cn, "I", false) == 2;
		boolean rightNodeCount = getFieldOfTypeCount(cn, node, false) == 1;
		return rightNodeCount && rightIntCount;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new HeadHooks());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class HeadHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String[] pat = { "iconst_0", "istore" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			String node = "[L" + findObfClassName("Node") + ";";
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(pat, ms)[0];
			// System.out.println("masdas d " + m.name + " " + m.desc + "  " +
			// m.instructions.size());
			AbstractInsnNode[] i = followJump(m, 300);

			String h = getFieldOfType(cn, node, false);
			list.add(asFieldHook(h, "getBuckets"));

			// System.out.println("with " + i.length);
			h = findField(i, true, true, 2, 'f', "iconst_0");
			list.add(asFieldHook(h, "getHead"));

			h = findField(i, true, true, 3, 'f', "iconst_0");
			list.add(asFieldHook(h, "getTail"));

			return list;
		}
	}
}