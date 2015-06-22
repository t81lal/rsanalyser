package org.nullbool.api.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nullbool.api.Context;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.InterfaceMapping;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteio.out.CompleteJarDumper;

import com.google.gson.GsonBuilder;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 19 Apr 2015 at 09:31:38 <br>
 */
public class APIGenerator {

	public static boolean log = true;
	public static final String ACCESSOR_BASE = "org/nullbool/piexternal/game/api/accessors/";
	public static final Map<String, String> SUPER_INTERFACES = new HashMap<String, String>();
	public static final Map<String, String> API_CANONICAL_NAMES = new HashMap<String, String>();

	static {
		API_CANONICAL_NAMES.put("Client", "IOldschoolClient");
		API_CANONICAL_NAMES.put("Canvas", "ICanvas");
		API_CANONICAL_NAMES.put("WrappedException", "IWrappedException");
		API_CANONICAL_NAMES.put("ExceptionReporter", "IExceptionReporter");
		
		API_CANONICAL_NAMES.put("Rasteriser", "render/IRasteriser");
		
		API_CANONICAL_NAMES.put("IsaacCipher", "network/IsaacCipher");
		API_CANONICAL_NAMES.put("Buffer", "network/IBuffer");
		API_CANONICAL_NAMES.put("Packet", "network/IPacket");

		API_CANONICAL_NAMES.put("Node", "collections/INode");
		API_CANONICAL_NAMES.put("DualNode", "collections/IDualNode");
		API_CANONICAL_NAMES.put("Hashtable", "collections/IHashTable");
		API_CANONICAL_NAMES.put("Deque", "collections/IDeque");

		API_CANONICAL_NAMES.put("NPCDefinition", "definitions/INPCDefinition");
		API_CANONICAL_NAMES.put("ItemDefinition", "definitions/IItemDefinition");
		API_CANONICAL_NAMES.put("ObjectDefinition", "definitions/IObjectDefinition");

		API_CANONICAL_NAMES.put("Widget", "widgets/IWidget");
		API_CANONICAL_NAMES.put("WidgetNode", "widgets/IWidgetNode");
		API_CANONICAL_NAMES.put("ItemContainer", "widgets/IItemContainer");

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
		// NEW BASE
		// org/nullbool/piexternal/game/api/IGameClient
		
		SUPER_INTERFACES.put("Client", "org/nullbool/piexternal/game/api/IGameClient");
		SUPER_INTERFACES.put("DualNode", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("Tile", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("WidgetNode", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("Widget", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("ItemContainer", API_CANONICAL_NAMES.get("Node"));

		SUPER_INTERFACES.put("Renderable", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("Model", API_CANONICAL_NAMES.get("Renderable"));
		SUPER_INTERFACES.put("Actor", API_CANONICAL_NAMES.get("Renderable"));

		SUPER_INTERFACES.put("NPC", API_CANONICAL_NAMES.get("Actor"));
		SUPER_INTERFACES.put("Player", API_CANONICAL_NAMES.get("Actor"));

		SUPER_INTERFACES.put("GroundItem", API_CANONICAL_NAMES.get("Renderable"));

		SUPER_INTERFACES.put("NPCDefinition", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("ObjectDefinition", API_CANONICAL_NAMES.get("DualNode"));
		SUPER_INTERFACES.put("ItemDefinition", API_CANONICAL_NAMES.get("DualNode"));
		
		SUPER_INTERFACES.put("Rasteriser", API_CANONICAL_NAMES.get("DualNode"));
		
		SUPER_INTERFACES.put("Buffer", API_CANONICAL_NAMES.get("Node"));
		SUPER_INTERFACES.put("Packet", API_CANONICAL_NAMES.get("Buffer"));
		
		DefaultAPIHelper helper = new DefaultAPIHelper(ACCESSOR_BASE);
		for(Entry<String, String> e : API_CANONICAL_NAMES.entrySet()) {
			helper.remapCanonicalname(e.getKey(), e.getValue());
		}
		for(Entry<String, String> e : SUPER_INTERFACES.entrySet()) {
			helper.mapSuperInterfaces(e.getKey(), new String[]{e.getValue()}, false);
		}
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out/translate.json")))) {
			bw.write(new GsonBuilder().setPrettyPrinting().create().toJson(helper));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void createAPI(HookMap hookMap) {
		JarContents<ClassNode> contents = new JarContents<ClassNode>();
		for (ClassHook hook : hookMap.classes()) {
			ClassNode cn = new ClassNode();
			cn.version = Opcodes.V1_8;
			cn.superName = "java/lang/Object";
			cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE;
			String thisName = API_CANONICAL_NAMES.get(hook.refactored());
			if (thisName != null)
				thisName = cn.name = ACCESSOR_BASE + thisName;
			else
				thisName = ACCESSOR_BASE + "I" + hook.refactored();
			cn.name = thisName;

			String sup = SUPER_INTERFACES.get(hook.refactored());
			if (sup != null) {
				if (!sup.startsWith("org/nullbool"))
					sup = ACCESSOR_BASE + sup;
				hook.interfaces().add(new InterfaceMapping(hook, sup));
				cn.interfaces.add(sup);
			}

			for (FieldHook f : hook.fields()) {
				MethodNode mn = new MethodNode(cn, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, f.refactored(), "()"
						+ convertSingleBytecodeStyle(hookMap.classes(), f.val(FieldHook.DESC)), null, null);
				cn.methods.add(mn);
			}

			for (MethodHook m : hook.methods()) {
				String d = convertMultiBytecodeStyle(hookMap.classes(), m.val(MethodHook.DESC));
				MethodNode mn = new MethodNode(cn, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, m.refactored(), d, null, null);
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
		File baseDir = new File(String.format("out/%s/", name));
		File file = new File(baseDir, String.format("api.jar"));
		if (file.exists())
			file.delete();
		file.mkdirs();

		runFern(file, new File(baseDir, "apisrc"));
		try {
			dumper.dump(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runFern(File inJar, File outDir) {
		if (outDir.exists())
			outDir.delete();
		outDir.mkdirs();

		new Thread() {
			@Override
			public void run() {
				List<String> list = new ArrayList<String>();
				try {
					list.add("Decompiling api sourcecode to " + outDir.getAbsolutePath() + "...");
					File fern = new File(APIGenerator.class.getResource("/fernflower.jar").toURI());
					ProcessBuilder pb = new ProcessBuilder("java", "-jar", quote(fern.getAbsolutePath()), quote(inJar.getAbsolutePath()),
							quote(outDir.getAbsolutePath()));
					// pb = pb.inheritIO();
					Process p = pb.start();
					p.waitFor();
					list.add("   ... done. (" + p.exitValue() + ")");
					list.add("Unpacking...");
					unpack(new File(outDir, inJar.getName()), outDir);
					list.add("   ... done. ");
					// new File(outDir, inJar.getName()).delete();
				} catch (Throwable t) {
					list.add("   ... failed. (" + t.getMessage() + ")");
					t.printStackTrace();
				}

				if(log) {
					for (String s : list) {
						System.err.println(s);
					}
				}
			}
		}.start();
	}

	private static void unpack(File zip, File outDir) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
		ZipEntry entry;
		byte[] buffer = new byte[1024];
		while ((entry = zis.getNextEntry()) != null) {
			File newFile = new File(outDir, entry.getName());
			File parent = newFile.getParentFile();
			parent.mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);

			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
		}
		zis.close();
	}

	private static String quote(String s) {
		return "\"" + s.replace("\\", "/") + "\"";
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
			if (hook.obfuscated().equals(className)) {
				className = hook.refactored();
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