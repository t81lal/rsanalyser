package org.nullbool.api.output;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nullbool.api.Context;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteio.out.CompleteJarDumper;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.InterfaceMapping;
import org.zbot.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 19 Apr 2015 at 09:31:38 <br>
 */
public class APIGenerator {

	public static final String ACCESSOR_BASE = "org/zbot/accessors/";
	public static final Map<String, String> SUPER_INTERFACES = new HashMap<String, String>();
	public static final Map<String, String> API_CANONICAL_NAMES = new HashMap<String, String>();

	static {
		API_CANONICAL_NAMES.put("Client", "IOldschoolClient");
		API_CANONICAL_NAMES.put("Canvas", "ICanvas");
		API_CANONICAL_NAMES.put("WrappedException", "IWrappedException");
		API_CANONICAL_NAMES.put("ExceptionReporter", "IExceptionReporter");

		API_CANONICAL_NAMES.put("Node", "collections/INode");
		API_CANONICAL_NAMES.put("DualNode", "collections/IDualNode");
		API_CANONICAL_NAMES.put("Hashtable", "collections/IHashTable");
		API_CANONICAL_NAMES.put("Deque", "collections/IDeque");

		API_CANONICAL_NAMES.put("NPCDefinition", "definitions/INPCDefinition");
		API_CANONICAL_NAMES.put("ItemDefinition", "definitions/IItemDefinition");
		API_CANONICAL_NAMES.put("ObjectDefinition", "definitions/IObjectDefinition");

		API_CANONICAL_NAMES.put("Widget", "widgets/IWidget");
		API_CANONICAL_NAMES.put("WidgetNode", "widgets/IWidgetNode");

		API_CANONICAL_NAMES.put("Renderable", "entity/IRenderable");
		API_CANONICAL_NAMES.put("Actor", "entity/IActor");
		API_CANONICAL_NAMES.put("Model", "entity/IModel");
		API_CANONICAL_NAMES.put("NPC", "entity/INPC");
		API_CANONICAL_NAMES.put("Player", "entity/IPlayer");

		API_CANONICAL_NAMES.put("Tile", "world/ITile");
		API_CANONICAL_NAMES.put("GroundItem", "world/IGroundItem");
		API_CANONICAL_NAMES.put("GameObject", "world/IGameObject");
		API_CANONICAL_NAMES.put("GroundObject", "world/IGroundObject");
		API_CANONICAL_NAMES.put("GroundDecoration", "world/IGroundDecoration");
		API_CANONICAL_NAMES.put("WallObject", "world/IWallObject");
		API_CANONICAL_NAMES.put("WallDecoration", "world/IWallDecoration");
		API_CANONICAL_NAMES.put("Region", "world/IRegion");

		// Client must extend org/zbot/api/IGameClient which is the client accessor base

		SUPER_INTERFACES.put("Client", "org/zbot/api/IGameClient");
		SUPER_INTERFACES.put("DualNode", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("Tile", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("WidgetNode", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("Widget", API_CANONICAL_NAMES.get("Node"));

		SUPER_INTERFACES.put("Renderable", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("Model", API_CANONICAL_NAMES.get("Renderable"));
		SUPER_INTERFACES.put("Actor", API_CANONICAL_NAMES.get("Renderable"));

		SUPER_INTERFACES.put("NPC", API_CANONICAL_NAMES.get("Actor"));
		SUPER_INTERFACES.put("Player", API_CANONICAL_NAMES.get("Actor"));

		SUPER_INTERFACES.put("GroundItem", API_CANONICAL_NAMES.get("Renderable"));

		SUPER_INTERFACES.put("NPCDefinition", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("ObjectDefinition", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("ItemDefinition", API_CANONICAL_NAMES.get("DualNode"));
	}

	public static void createAPI(HookMap hookMap) {
		JarContents<ClassNode> contents = new JarContents<ClassNode>();
		for (ClassHook hook : hookMap.getClasses()) {
			ClassNode cn = new ClassNode();
			cn.version = Opcodes.V1_8;
			cn.superName = "java/lang/Object";
			cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE;
			String thisName = API_CANONICAL_NAMES.get(hook.getRefactored());
			if (thisName != null)
				thisName = cn.name = ACCESSOR_BASE + thisName;
			else
				thisName = ACCESSOR_BASE + "I" + hook.getRefactored();
			cn.name = thisName;

			String sup = SUPER_INTERFACES.get(hook.getRefactored());
			if (sup != null) {
				if (!sup.startsWith("org/zbot"))
					sup = ACCESSOR_BASE + sup;
				hook.getInterfaces().add(new InterfaceMapping(hook, sup));
				cn.interfaces.add(sup);
			}

			for (FieldHook f : hook.getFields()) {
				MethodNode mn = new MethodNode(cn, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, f.getName().getRefactored(), "()"
						+ convertSingleBytecodeStyle(hookMap.getClasses(), f.getDesc().getObfuscated()), null, null);
				cn.methods.add(mn);
			}

			for (MethodHook m : hook.getMethods()) {
				String d = convertMultiBytecodeStyle(hookMap.getClasses(), m.getDesc().getObfuscated());
				MethodNode mn = new MethodNode(cn, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, m.getName().getRefactored(), d, null, null);
				cn.methods.add(mn);

				// if (m.getInstructions() != null) {
				// System.out.println(mn.key());
				// MethodNode temp = new MethodNode(mn.owner);
				// temp.instructions = m.getInstructions();
				//
				// for (String s : new InstructionPrinter(temp).createPrint()) {
				// System.out.println("   " + s);
				// }
				//
				// temp.instructions.reset();
				// }
			}

			contents.getClassContents().add(cn);
		}

		CompleteJarDumper dumper = new CompleteJarDumper(contents);
		String name = Context.current().getRevision().getName();
		File file = new File("out/" + name + "/api" + name + ".jar");
		if (file.exists())
			file.delete();
		file.mkdirs();
		try {
			dumper.dump(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			sb.append(desc);
		}
		return sb.toString();
	}

	public static boolean isPrimitive(String desc) {
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

	public static String convertSingleBytecodeStyle(List<ClassHook> classes, String desc) {
		return convertSingleBytecodeStyle(classes, desc.replace("[", ""), array(desc));
	}

	public static String convertSingleBytecodeStyle(List<ClassHook> classes, String desc, int arr) {
		StringBuilder sb = new StringBuilder();
		sb.append(makeArray(arr, "["));
		if (isPrimitive(desc))
			sb.append(desc);
		else if (desc.startsWith("L") && desc.endsWith(";"))
			sb.append(getBytecodeDesc(classes, desc));
		return sb.toString();
	}

	public static String convertMultiBytecodeStyle(List<ClassHook> classes, String desc) {
		if (desc == null)
			return null;

		Type ret = Type.getReturnType(desc);
		String retVal = "";
		if (isVoid(ret)) {
			retVal = "V";
		} else {
			retVal = convertSingleBytecodeStyle(classes, ret.getDescriptor());
		}

		Set<String> args = parseArgs(desc);
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		Iterator<String> it = args.iterator();
		while (it.hasNext()) {
			String arg = it.next();
			sb.append(convertSingleBytecodeStyle(classes, arg));
		}
		sb.append(")");
		return String.format("%s%s", sb.toString(), retVal);
	}

	public static boolean isVoid(Type t) {
		return isVoid(t.getDescriptor());
	}

	public static boolean isVoid(String desc) {
		return desc.equals("V");
	}

	public static Set<String> parseArgs(String desc) {
		if (desc == null)
			return null;
		Type[] args = Type.getArgumentTypes(desc);
		// needs to be ordered
		Set<String> set = new LinkedHashSet<String>(args.length);
		for (Type arg : args) {
			set.add(arg.getDescriptor());
		}
		return set;
	}

	public static String standardise(String s) {
		return s.replace(".", "/");
	}

	public static String getBytecodeDesc(List<ClassHook> classes, String name) {
		if (name == null)
			return null;
		name = standardise(name);

		String className = name.substring(1, name.length() - 1);

		for (ClassHook hook : classes) {
			if (hook.getObfuscated().equals(className)) {
				className = hook.getRefactored();
				break;
			}
		}

		for (Entry<String, String> e : API_CANONICAL_NAMES.entrySet()) {
			String obf = e.getKey();
			String ref = ACCESSOR_BASE + e.getValue();
			if (obf != null && ref != null) {
				if (standardise(obf).equals(className))
					return String.format("L%s;", ref);
			}
		}

		return name;
	}
}