package org.nullbool.impl.analysers.entity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.topdank.banalysis.asm.desc.Desciption;

/**
 * @author MalikDz
 */
@SupportedHooks(fields = { "npcDefinition&NPCDefinition", }, methods = {})
public class NPCAnalyser extends ClassAnalyser {

	public NPCAnalyser() throws AnalysisException {
		super("NPC");
	}

	@Override
	protected boolean matches(ClassNode c) {
		
		Set<ClassNode> supers = Context.current().getClassTree().getSupers(c);
		ClassNode actorClass = getClassNodeByRefactoredName("Actor");
		if(!supers.contains(actorClass))
			return false;
		
		int[] count = count(c);
		if(count[0] != 0 || count[1] != 1)
			return false;
		
		return true;
		
		/*String[] pattern = Context.current().getPattern("NPC");
		String superClassName = findObfClassName("Actor");
		boolean rightSuperClass = c.superName.equals(superClassName);
		boolean goodPattern = findMethod(c, "init", pattern);
		return goodPattern && rightSuperClass;*/
	}
	
	private static int[] count(ClassNode cn) {
		int[] arr = new int[2];
		for(FieldNode f : cn.fields) {
			if(!Modifier.isStatic(f.access)) {
				if(Desciption.isPrimitive(f.desc)) {
					arr[0]++;
				} else {
					arr[1]++;
				}
			}
		}
		return arr;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new NpcDefHook());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class NpcDefHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String obfName = findObfClassName("NPCDefinition");
			String hook = identify(cn, "L" + obfName + ";", 'f');
			list.add(asFieldHook(hook, "npcDefinition"));
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