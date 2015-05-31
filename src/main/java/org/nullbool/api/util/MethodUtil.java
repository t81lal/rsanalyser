package org.nullbool.api.util;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 31 May 2015
 */
public class MethodUtil {

	private static final Set<String> DUMMY_PARAM_TYPES = new HashSet<String>();

	static {
		DUMMY_PARAM_TYPES.add("I");
		DUMMY_PARAM_TYPES.add("S");
		DUMMY_PARAM_TYPES.add("B");
	}
	
	public static boolean isDummy(Type desc) {
		return DUMMY_PARAM_TYPES.contains(desc.getDescriptor());
	}
	
	public static boolean isDummy(String desc) {
		return DUMMY_PARAM_TYPES.contains(desc);
	}
	
	public static int getLastParameterIndex(MethodNode m) {
		Type[] args = Type.getArgumentTypes(m.desc);
		return args.length + (Modifier.isStatic(m.access) ? -1 : 0);
	}
	
	public static Object[] getLastParameter(MethodNode m) {
		Type[] args = Type.getArgumentTypes(m.desc);
		if(args.length == 0)
			return null;
		
		return new Object[]{ args.length + (Modifier.isStatic(m.access) ? -1 : 0), args[args.length - 1]};
	}
	
	public static Object[] getLastDummyParameter(MethodNode m) {
		Object[] objs = getLastParameter(m);
		if(objs == null)
			return null;
		
		Type type = (Type) objs[1];
		if(!isDummy(type))
			return null;
		
		return objs;
	}
}