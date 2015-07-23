package org.nullbool.impl.analysers.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author : MalikDz
 */
@SupportedHooks(fields = { "quantities&[I", "itemIds&[I" }, methods = {})
public class ItemContainerAnalyser extends ClassAnalyser {

	private String className;
	private MethodNode analysedMethod;

	public ItemContainerAnalyser() throws AnalysisException {
		super("ItemContainer");
		MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";IIII.{0,2};V",false);
		analysedMethod = startWithBc(new String [] { "getstatic", "iload", "i2l" }, mn)[0];
		className= findField(analysedMethod, true, true, 1, 'f', "aload 6", "iload 8", "aload 5").split("\\.")[0];
	}

	@Override
	public boolean matches(ClassNode cn) {
		return cn.name.equals(className);
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new ItemAndStackHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}

	public class ItemAndStackHooks implements IFieldAnalyser {
		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			final List<FieldHook> hooks = new ArrayList<FieldHook>();
			
			String field = findField(analysedMethod, true, true, 1, 'f', "aload 5", "aload 7");
			hooks.add(asFieldHook(field, "quantities"));
			
			field = findField(analysedMethod, true, true, 1, 'f', "aload 6", "iload 8", "aload 5");
			hooks.add(asFieldHook(field, "itemIds"));
			return hooks;
		}
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMultiAnalysers()
	 */
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		// TODO Auto-generated method stub
		return null;
	}
}