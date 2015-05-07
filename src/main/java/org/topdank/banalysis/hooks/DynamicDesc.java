package org.topdank.banalysis.hooks;

import java.io.Serializable;
import java.util.Collection;

import org.objectweb.asm.Type;

public class DynamicDesc implements Serializable {

	private static final long serialVersionUID = 5520906934192018290L;

	private final boolean method;
	private String obfuscated;

	public DynamicDesc(boolean method) {
		this.method = method;
	}

	public DynamicDesc(String obfuscated, boolean method) {
		this.obfuscated = obfuscated;
		this.method = method;
	}

	public boolean isMethod() {
		return method;
	}

	public String getObfuscated() {
		return obfuscated;
	}

	public void setObfuscated(String obfuscated) {
		this.obfuscated = obfuscated;
	}

	public String calcRefactoredCalc(Collection<ClassHook> classes) {
		if (method) {
			Type[] args = Type.getArgumentTypes(obfuscated);
			Type ret = Type.getReturnType(obfuscated);
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (Type arg : args) {
				sb.append(calcRefactoredFieldDesc(arg, classes));
			}
			sb.append(")");
			if (ret.toString().equals("V")) {
				sb.append("V");
			} else {
				sb.append(calcRefactoredFieldDesc(ret, classes));
			}
			return sb.toString();
		} else {
			return calcRefactoredFieldDesc(Type.getType(obfuscated), classes);
		}
	}

	private String calcRefactoredFieldDesc(Type t, Collection<ClassHook> classes) {
		String desc = t.toString();
		String clean = desc.replace("[", "");
		if (isPrimitive(clean))
			return desc;
		int arr = array(desc);
		String c = Type.getType(clean).getClassName();
		if (c.contains("java"))
			return desc;
		for (ClassHook hook : classes) {
			if (hook.getObfuscated().equals(c))
				return makeArray(arr, hook.getRefactored());
		}
		return desc;
	}

	private static int array(String desc) {
		int c = 0;
		char[] chars = desc.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c1 = chars[i];
			if (c1 == '[') {
				c++;
			} else {
				break;
			}
		}
		return c;
	}

	private static String makeArray(int j, String desc) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < j; i++) {
			sb.append("[");
		}
		sb.append(desc);
		return sb.toString();
	}

	private static boolean isPrimitive(String desc) {
		switch (desc) {
			case "I":
			case "D":
			case "F":
			case "B":
			case "S":
			case "J":
			case "Z":
				return true;
			default:
				return false;
		}
	}

	@Override
	public String toString() {
		return "DynamicDesc [method=" + method + ", obfuscated=" + obfuscated + "]";
	}
}