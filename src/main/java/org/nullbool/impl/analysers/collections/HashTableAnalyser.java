package org.nullbool.impl.analysers.collections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl
 * @author MalikDz
 */
@SupportedHooks(
		fields = { "buckets&[Node", "head&Node", "firstNode&Node", "size&I", "index&I" }, 
		methods = {"put&(LNode;J)V", "first&()LNode;", "next&()LNode;", "clear&()V", "get&(J)LNode;"})
public class HashTableAnalyser extends ClassAnalyser {

	private static final int[] PUT_METHOD_PATTERN = new int[]{ALOAD, GETFIELD, IFNULL};
	private static final int[] KEY_CALC_PATTERN = new int[]{GETFIELD, ICONST_1, ISUB};
	private static final int[] INDEX_ZERO_SET_PATTERN = new int[]{ALOAD, ICONST_0, PUTFIELD};
	private static final int[] RETURN_FIRST_PATTERN = new int[]{ALOAD, INVOKEVIRTUAL, ARETURN};
	private static final int[] SET_NULL_FIELD = new int[]{ALOAD, ACONST_NULL, PUTFIELD};
	
	public HashTableAnalyser() throws AnalysisException {
		super("HashTable");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		String node = "\\[L" + findObfClassName("Node") + ";";
		boolean rightIntCount = getFieldOfTypeCount(cn, "I", false) == 2;
		boolean rightNodeCount = getFieldOfTypeCount(cn, node, false) == 1;
		return rightNodeCount && rightIntCount;
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new HeadHooks(), new IntFieldAnalyser());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().add(new MethodAnalyser());
	}
	
	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> findMethods(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			if(putMethod != null) {
				list.add(asMethodHook(putMethod, "put").var(Constants.METHOD_TYPE, Constants.CALLBACK));
			}
			
			if(firstMethod != null) {
				list.add(asMethodHook(firstMethod, "first").var(Constants.METHOD_TYPE, Constants.CALLBACK));
			}
			
			if(nextMethod != null) {
				list.add(asMethodHook(nextMethod, "next").var(Constants.METHOD_TYPE, Constants.CALLBACK));
			}
			
            //aload0 // reference to self
            //aconst_null
            //putfield Hashtable.firstNode:Node
            //aload0 // reference to self
            //aconst_null
            //putfield Hashtable.head:Node
            
			String nodeDesc = (")L" + getAnalyser("Node").getFoundClass().name + ";");
			
			for(MethodNode m : cn.methods) {
				if(m.desc.endsWith(")V")) {
					List<AbstractInsnNode[]> ainsl = findAllOpcodePatterns(m, SET_NULL_FIELD);
					if(ainsl.size() == 2) {
						list.add(asMethodHook(m, "clear").var(Constants.METHOD_TYPE, Constants.CALLBACK));
					}
				} else if(m.desc.startsWith("(J") && m.desc.endsWith(nodeDesc)) {
					List<AbstractInsnNode> ains = findAllOpcodePatternsStarts(m, KEY_CALC_PATTERN);
					if(ains.size() == 1) {
						List<AbstractInsnNode[]> ainsl = findAllOpcodePatterns(m, SET_NULL_FIELD);
						if(ainsl.size() == 1) {
							list.add(asMethodHook(m, "get").var(Constants.METHOD_TYPE, Constants.CALLBACK));
						}
					}
					//getfield Hashtable.size:int
		            //iconst_1
		            //isub
		            //i2l
		            //land
		            //l2i
		            //aaload
				}
			}
			
			return list;
		}
	}
	
	private MethodNode putMethod;
	private MethodNode firstMethod;
	private MethodInsnNode nextMethod;
	
	private class IntFieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

            //aload1 // reference to arg0
            //getfield Node.getPrevious:Node
            //ifnull L2
            //aload1 // reference to arg0
            //invokevirtual Node if(()V);
			
//			aload1 // reference to arg0
//            getfield Node.getPrevious:Node
//            ifnull L2


			MethodNode[] ms  = getMethodNodes(cn.methods.stream().filter(m -> !Modifier.isStatic(m.access) && m.desc.endsWith(")V") && !m.name.equals("<init>")).collect(Collectors.toList()).toArray());
			Set<MethodNode> ms2 = findMethodsByPattern(ms, PUT_METHOD_PATTERN);
			
			if(ms2.size() == 1) {
//				getfield Hashtable.x:int
//	             iconst_1
//	             isub
				MethodNode m = ms2.toArray(new MethodNode[0])[0];
				List<AbstractInsnNode> ains = findAllOpcodePatternsStarts(m, KEY_CALC_PATTERN);
				if(ains.size() == 1) {
					putMethod = m;
					
					FieldInsnNode fin = (FieldInsnNode) ains.get(0);
					list.add(asFieldHook(fin, "size"));
				}
			}
			
			String nodeDesc = (")L" + getAnalyser("Node").getFoundClass().name + ";");

			// aload0 // reference to self
            // iconst_0
            // putfield Hashtable.h:int
            // aload0 // reference to self
            // invokevirtual Hashtable h(()LNode;);
            // areturn

			for(MethodNode m : cn.methods) {
				if(m.desc.endsWith(nodeDesc)) {
					List<AbstractInsnNode[]> ainsl1 = findAllOpcodePatterns(m, INDEX_ZERO_SET_PATTERN);
					if(ainsl1.size() == 1) {
						List<AbstractInsnNode[]> ainsl2 = findAllOpcodePatterns(m, RETURN_FIRST_PATTERN);
						if(ainsl2.size() == 1) {
							firstMethod = m;
							
							list.add(asFieldHook((FieldInsnNode) ainsl1.get(0)[2], "index"));
							
							nextMethod = (MethodInsnNode) ainsl2.get(0)[1];
						}
					}
				}
			}
			
			return list;
		}
	}

	public class HeadHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> findFields(ClassNode cn) {
			String[] pat = { "iconst_0", "istore" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			String node = "[L" + findObfClassName("Node") + ";";
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			MethodNode m = startWithBc(pat, ms)[0];
			// System.out.println("masdas d " + m.name + " " + m.desc + "  " +
			// m.instructions.size());
			AbstractInsnNode[] i = followJump(m, 300);

			String h = getFieldOfType(cn, node, false);
			list.add(asFieldHook(h, "buckets"));

			// System.out.println("with " + i.length);
			h = findField(i, true, true, 2, 'f', "iconst_0");
			list.add(asFieldHook(h, "head"));

			h = findField(i, true, true, 3, 'f', "iconst_0");
			list.add(asFieldHook(h, "firstNode"));

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