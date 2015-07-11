package org.nullbool.byeallatori;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.nullbool.AntiPowerbot.ClassLoaderExt;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.InsaneControlFlowGraph;
import org.nullbool.api.obfuscation.refactor.BytecodeRefactorer;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.IRemapper;
import org.nullbool.api.obfuscation.refactor.MethodCache;
import org.nullbool.api.obfuscation.refactor.SetCreator;
import org.nullbool.api.util.ClassStructure;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.nullboolext.ad_overide;
import org.nullboolext.append_print;
import org.nullboolext.check_map;
import org.nullboolext.md5gen_override;
import org.nullboolext.process_builder_fixer;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.byteengineer.commons.data.JarInfo;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.in.SingleJarDownloader;
import org.topdank.byteio.out.CompleteJarDumper;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jun 2015 17:47:33
 */
public class Boot implements Opcodes {



	public static void request() throws Exception {
		NetworkInterface var4 = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		byte[] var11 = null;
		if(var4 != null) {
			var11 = var4.getHardwareAddress();
		}

		if(var11 == null) {
			var11 = "".getBytes();
		}

		StringBuilder macsb = new StringBuilder();

		int var7;
		for(int var16 = var7 = 0; var16 < var11.length; var16 = var7) {
			Object[] var10002 = new Object[]{Byte.valueOf(var11[var7]), var7 < var11.length - 1?"-":""};
			++var7;
			macsb.append(String.format("%02X%s", var10002));
		}

		String mac = macsb.toString();
		String os;
		String payload = System.getProperty("os.name");

		if(payload.contains("Mac")) {
			os = "Mac";
		} else if(payload.contains("Windows")) {
			os = "Windows";
		} else if(payload.contains("Linux")) {
			os = "Linux";
		} else {
			os = "Unknown";
		}

		payload = (new StringBuilder()).insert(0, "name=").append("jketelaar")
				.append("&password=").append(pass)
				.append("&identifier=").append(mac)
				.append("&v1=").append(2)
				.append("&v2=").append(3)
				.append("&v3=").append(75)
				.append("&os=").append(os)
				.append("&add=").append(MiscHelper.add()).toString();

		HttpsURLConnection connection = ConnectionHelper.openSecureHandle("https://osbot.org:443/mvc/botauth/create");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", 
				new StringBuilder().insert(0, "")
				//payload length
				.append(Integer.toString(payload.getBytes().length))
				.toString()
				);
		connection.setRequestProperty("User-Agent", "OSBot Comms");
		connection.setUseCaches(false);
		DataOutputStream var10001 = new DataOutputStream(connection.getOutputStream());
		var10001.writeBytes(payload);
		var10001.flush();
		var10001.close();

		int resp = connection.getResponseCode();
		System.out.println("resp: "+ resp);

		if(resp == 200) {
			DataInputStream var13;
			DataInputStream var17 = var13 = new DataInputStream(connection.getInputStream());
			String var8 = BufferUtilities.method_16(var17);
			boolean var9 = var17.readUnsignedByte() == 0;
			String var18 = payload = BufferUtilities.method_16(var13);
			connection.disconnect();

			SESSION = var8;


			System.out.println(var8);
			System.out.println(var9);
			System.out.println(var18);
			// if(var18.equals("None")) {
			//   var10000 = this;
			// } else {
			//    var10000 = this;
			//   this.field_96 = var12;
			// }

			//this.field_94 = var8;
			//this.field_92 = var9;
			//this.field_93 = true;
			//BotApplication.method_41().getInterface().field_100.setText((new StringBuilder()).insert(0, "Logged in as : ").append(threessss(var1)).toString());
			//BotApplication.method_41().getInterface().field_74.setText("Disconnect");

			//TODO:
			// LOADS HOOKS WHEN KLASS25.RUN IS CALLED
			// Klass15.method_3(new Klass25(this, var1, var2));



			//Klass4.method_15();
			// Klass4.method_7();
			//Klass4.method_9();
			//Klass4.method_17();
			//return var3;
		} else 
			connection.disconnect();
	}

	private static String SESSION = "";

	public static byte[] hooks() {
		try {
			String var1 = (new StringBuilder()).insert(0, "sessionID=").append(SESSION).toString();
			HttpsURLConnection var2 = ConnectionHelper.openSecureHandle("https://osbot.org:443/mvc/botserver/hooks");
			var2.setDoOutput(true);
			var2.setDoInput(true);
			var2.setInstanceFollowRedirects(false);
			var2.setRequestMethod("POST");
			var2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			var2.setRequestProperty("charset", "utf-8");
			var2.setRequestProperty("Content-Length", (new StringBuilder()).insert(0, "").append(Integer.toString(var1.getBytes().length)).toString());
			var2.setRequestProperty("User-Agent", "OSBot Comms");
			var2.setUseCaches(false);
			DataOutputStream var10001 = new DataOutputStream(var2.getOutputStream());
			var10001.writeBytes(var1);
			var10001.flush();
			var10001.close();
			if(var2.getResponseCode() == 200) {
				DataInputStream var3 = new DataInputStream(var2.getInputStream());
				ByteArrayOutputStream var4 = new ByteArrayOutputStream();
				DataInputStream var10000 = var3;

				int var5;
				while((var5 = var10000.read()) != -1) {
					var10000 = var3;
					var4.write(var5);
				}

				var3.close();
				var2.disconnect();
				byte[] var7 = var4.toByteArray();
				return var7;
			}

			var2.disconnect();
		} catch (Exception var6) {
			var6.printStackTrace();
		}

		return null;
	}


	public static String threessss(String var0) {
		char[] var1 = var0.replace("_", " ").toLowerCase().toCharArray();

		int var2;
		for(int var10000 = var2 = 0; var10000 < var1.length; var10000 = var2) {
			if(var1[var2] == 32 || var1[var2] == 95) {
				var1[var2] = 32;
				if(var2 + 1 < var1.length && var1[var2 + 1] >= 97 && var1[var2 + 1] <= 122) {
					var1[var2 + 1] = (char)(var1[var2 + 1] + 65 - 97);
				}
			}

			++var2;
		}

		if(var1[0] >= 97 && var1[0] <= 122) {
			var1[0] = (char)(var1[0] + 65 - 97);
		}

		return new String(var1);
	}

	private static String readString(ByteBuffer var0) {
		ByteArrayOutputStream var1 = new ByteArrayOutputStream();
		byte var2;
		while ((var2 = var0.get()) != 10) {
			var1.write(var2);
		}
		return new String(var1.toByteArray());
	}

	private static void parseHooks(ByteBuffer buf) {
		final int magic = buf.getInt();
		final int expectedMagic = 0xbaddcafe;
		if (magic != expectedMagic) {
			buf.reset();
			return;
		}
		int k =0;

		System.out.println("Found hooks...");
		final short aParnyShort = buf.getShort();
		final int count = aParnyShort & 0xffff;
		for (int i = 0; i < count; i++) {
			String var4 = readString(buf);
			String var5 = readString(buf);
			System.out.println(var4 + " := " + var5);
			final short aParnyShort0 = buf.getShort();
			final int mcount = aParnyShort0 & 0xffff;
			// System.out.println((k += mcount));
			for (int j = 0; j < mcount; j++) {
				String var7 = readString(buf);
				String var8 = readString(buf);
				String var9 = readString(buf);
				String var10 = readString(buf);
				String var11;
				if ((var11 = readString(buf)).equals("")) {
					var11 = null;
				}
				if (buf.get() == 1) {
					long var13 = buf.getLong();
					long var15 = buf.getLong();
					System.out.println("\t" + var7 + "." + var9 + " identified as " + var8 + " * [" + var13 + "][" + var15 + "]");
				} else {
					System.out.println("\t" + var7 + "." + var9 + " identified as " + var8);
				}
			}
		}
		System.out.println("...Complete");


	}

	private static void patchClient() throws Exception {
		//patch jarmd5 method, only called from request class


		cl: for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				boolean md5 = false, dir = false;

				for(AbstractInsnNode ain : m.instructions.toArray()) {
					if(ain instanceof LdcInsnNode) {
						LdcInsnNode ldc = (LdcInsnNode) ain;
						if(ldc.cst instanceof String) {
							if(ldc.cst.toString().equals("MD5")) {
								md5 = true;
							} else if(ldc.cst.toString().equals("(Is a directory)")) {
								dir = true;
							}
						}
					}
				}


				if(md5 && dir) {
					//ClassNode realCn = dl.getJarContents().getClassContents().namedMap().get(classes.get(cn.name));
					//int index = Integer.parseInt(m.name.replace("method_", ""));
					//MethodNode rmn = realCn.methods.get(index);
					InsnList insns = new InsnList();
					insns.add(new VarInsnNode(ALOAD, 0));
					insns.add(new MethodInsnNode(INVOKESTATIC, "org/nullboolext/md5gen_override", "generate", "(Ljava/io/File;)Ljava/lang/String;", false));
					insns.add(new InsnNode(ARETURN));

					m.instructions.clear();
					m.instructions = insns;
					break cl;
				}
			}
		}

	ClassNode cn = contents.getClassContents().namedMap().get("org/osbot/Boot");
	for(MethodNode m : cn.methods) {
		//if(m.name.equals("launch")) {
		for(AbstractInsnNode ain : m.instructions.toArray()) {
			if(ain instanceof LdcInsnNode) {
				LdcInsnNode ldc = (LdcInsnNode) ain;
				if(ldc.cst instanceof String) {
					if(ldc.cst.toString().equals("-XX:-UseSplitVerifier -Xmx")) {
						ldc.cst = "-noverify -Xmx";
						System.out.println("replaced splitverifier");
					}
				}
			} else if(ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if(min.owner.equals("java/lang/ProcessBuilder") && min.name.equals("<init>")) {
					System.out.println("pb call " + min);
					m.instructions.insertBefore(min, new MethodInsnNode(INVOKESTATIC, "org/nullboolext/process_builder_fixer", "fix", "([Ljava/lang/String;)[Ljava/lang/String;", false));
				}
			}
		}
		//}
	}




	dl.getJarContents().getClassContents().add(ClassStructure.create(md5gen_override.class.getCanonicalName()));
	dl.getJarContents().getClassContents().add(ClassStructure.create(process_builder_fixer.class.getCanonicalName()));

	new CompleteJarDumper(dl.getJarContents()){
		@Override
		public int dumpResource(JarOutputStream out, String name, byte[] file) throws IOException {
			if(name.startsWith("META-INF/SERVER."))
				return 0;

			return super.dumpResource(out, name, file);
		}
	}.dump(new File(out.getParentFile(), "runnableosbout.jar"));
	}

	private static Map<String, String> reverse(Map<String, String> map) {
		Map<String, String> newMap = new HashMap<String, String>();
		for(Entry<String, String> e : map.entrySet()) {
			newMap.put(e.getValue(), e.getKey());
		}
		return newMap;
	}


	public static void test() throws IOException {
		//		http://osbot.org/bot/adreq.php
		URLConnection var10000;
		InputStream var14 = null;
		try {
			var10000 = (new URL("http://osbot.org/bot/adreq.php")).openConnection();
			var10000.setConnectTimeout(300000);
			var10000.setRequestProperty("User-Agent", "OSBot Comms");
			ByteArrayOutputStream var12 = new ByteArrayOutputStream();
			var14 = var10000.getInputStream();

			int var3;
			while((var3 = var14.read()) != -1) {
				var12.write(var3);
			}

			byte[] var13 = var12.toByteArray();
			System.out.println(new String(var13));
		} finally {
			try {
				if(var14 != null) {
					var14.close();
				}
			} catch (Exception var10) {
				var10.printStackTrace();
			}

		}

	}


	public static void scrapt(int id) throws Exception {

		try {
			String var4 = (new StringBuilder()).insert(0, "sessionID=").append(SESSION).append("&scriptID=").append(id).append("&action=").append("add").toString();
			HttpsURLConnection var5 = ConnectionHelper.openSecureHandle("https://osbot.org:443/mvc/botserver/appendscript");
			var5.setDoOutput(true);
			var5.setDoInput(true);
			var5.setInstanceFollowRedirects(false);
			var5.setRequestMethod("POST");
			var5.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			var5.setRequestProperty("charset", "utf-8");
			var5.setRequestProperty("Content-Length", (new StringBuilder()).insert(0, "").append(Integer.toString(var4.getBytes().length)).toString());
			var5.setRequestProperty("User-Agent", "OSBot Comms");
			var5.setUseCaches(false);
			DataOutputStream var10002 = new DataOutputStream(var5.getOutputStream());
			var10002.writeBytes(var4);
			var10002.flush();
			var10002.close();
			System.out.println(var5.getResponseCode());
			var5.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}



		//		Class<?> klass = Class.forName("org.osbot.COm1");
		//		Method m = klass.getDeclaredMethod("method_10", new Class[]{String.class});
		//		System.out.println(m.invoke(null, "M)@\u0012K\bZKz\u001f^\u0003"));
	}

	public static void dumpscript(int id) {
		try {
			String var2 = (new StringBuilder()).insert(0, "sessionID=").append(SESSION).append("&scriptID=").append(id).toString();
			HttpsURLConnection var3 = ConnectionHelper.openSecureHandle("https://osbot.org:443/mvc/botserver/req");
			var3.setDoOutput(true);
			var3.setDoInput(true);
			var3.setInstanceFollowRedirects(false);
			var3.setRequestMethod("POST");
			var3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			var3.setRequestProperty("charset", "utf-8");
			var3.setRequestProperty("Content-Length", (new StringBuilder()).insert(0, "").append(Integer.toString(var2.getBytes().length)).toString());
			var3.setRequestProperty("User-Agent", "OSBot Comms");
			var3.setUseCaches(false);
			DataOutputStream var10001 = new DataOutputStream(var3.getOutputStream());
			var10001.writeBytes(var2);
			var10001.flush();
			var10001.close();
			if(var3.getResponseCode() == 200) {
				DataInputStream var7 = new DataInputStream(var3.getInputStream());
				ByteArrayOutputStream var4 = new ByteArrayOutputStream();
				DataInputStream var10000 = var7;

				int var5;
				while((var5 = var10000.read()) != -1) {
					var10000 = var7;
					var4.write(var5);
				}

				var7.close();
				var3.disconnect();
				byte[] bs =  var4.toByteArray();

				dump(id, bs);



			} else {
				System.out.printf("resp %d for id=%d.%n", var3.getResponseCode(), id);
			}

			var3.disconnect();
		} catch (Exception var6) {
			var6.printStackTrace();
		}


	}

	public static void dump(int i, byte[] var1) throws IOException {
		//		HashMap<String, byte[]> classes = new HashMap<String, byte[]>();
		//		HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
		//		byte[] buff = new byte[1024];



		//		JarInputStream jis = new JarInputStream(new ByteArrayInputStream(var1));
		//		JarOutputStream jos = new JarOutputStream(new FileOutputStream(new File("C:/Users/Bibl/Desktop/BACKUP/osbots shit nigga/script_" + i + ".jar")));
		//
		//		ZipEntry entry;
		//		while((entry = jis.getNextEntry()) != null) {
		//			System.out.println(entry.getName());
		//			jos.putNextEntry(entry);
		//		}
		//		jos.close();
		//		jis.close();

		FileOutputStream fos = new FileOutputStream(new File("C:/Users/Bibl/Desktop/BACKUP/osbots shit nigga/script_" + i + ".jar"));
		fos.write(var1);
		fos.close();

		//        ZipEntry entry;
		//        while((entry = jis.getNextEntry()) != null) {
		//           ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//
		//           int read;
		//           while((read = jis.read(buff, 0, buff.length)) != -1) {
		//              baos.write(buff, 0, read);
		//           }
		//
		//           if(entry.getName().endsWith(".class")) {
		//              classes.put(entry.getName(), baos.toByteArray());
		//           } else {
		//              resources.put(entry.getName(), baos.toByteArray());
		//           }
		//        }

	}

	private static File pb;
	private static File out;
	private static SingleJarDownloader<ClassNode> dl;
	private static LocateableJarContents<ClassNode> contents;
	private static List<ClassNode> classList;
	private static MethodCache cache;
	private static int two = 0, three = 0;
	private static MethodNode threes;
	private static Set<MethodNode> complex, simple;

	private static Map<String, String> classes = new HashMap<String, String>();
	private static Map<String, String> methods = new HashMap<String, String>();
	private static Map<String, String> fields = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {

		//		System.out.println(iIIiIIIiiI("rd_\u007fTee\u0006eraN"));

		// SESSION = "655207082866909a0-f69b-43d1-90f6-6804ff43cdb0";
		request();
				for(int i=0; i < 1000; i++) {
					try {
						scrapt(i);
						dumpscript(i);
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
//		byte[] hooks = hooks();
//		parseHooks(ByteBuffer.wrap(hooks));
//		System.out.println(hooks.length);

//		System.out.println(MiscHelper.add());
		if(true)
			return;

		if(true)
			return;
		//		
		//		if(true)
		//			return;
		//		
		//		test();
		//		if(true)
		//			return;

		File dir = new File("C:/Users/Bibl/Desktop/BACKUP/osbots shit nigga");
		pb = new File(dir, "osbot 2.3.77.jar");
		out = new File(dir, "osbout.jar");

		dl = new SingleJarDownloader<ClassNode>(new JarInfo(pb));
		dl.download();

		contents = dl.getJarContents();
		classList = contents.getClassContents();
		cache = new MethodCache(classList);

		nottheasshol();
		removeUnreachableCode();

		complex = findComplex();
		//Map<MethodNode, Set<MethodNode>> complexCalls = graphCalls(complex);
		Map<MethodNode, Class<?>> complexgen = generateComplex(complex);

		patchComplex(complex, complexgen);

		simple = findSimple2and3();
		Map<MethodNode, Class<?>> simpgen = generateSimple(simple);
		patchSimple(simple, simpgen);

		System.out.printf("%d two, %d three.%n", two, three);

		removeDummyMethods();

		openmyassdaddy();
		unfuck();

		// "zbQxVlV\"\u00198Fl3v~Cq@\u001e D\u0010e+\u0000\"\u00020\fA)\u000b\u00019\u00110MsL\u007f\u000f#\u001b8\u001aGRnGyY~+\u0010\'\u001d$\u001fXs\"7CnPcI$\u001f.Hl"

		//		ClassNode cn = contents.getClassContents().namedMap().get("org/osbot/D");
		//		for(MethodNode m : cn.methods) {
		//			
		//			if(m.name.equals("method_2")) {
		//				
		//				Method method = complexgen.get(m).getDeclaredMethods()[0];
		//				
		//				for(MethodNode m2 : cn.methods) {
		//					if(m != m2) {
		//						for(AbstractInsnNode ain : m2.instructions.toArray()) {
		//							if(ain instanceof LdcInsnNode) {
		//								LdcInsnNode ldc = (LdcInsnNode) ain;
		//								if(ldc.cst instanceof String) {
		//									System.out.println("enc: " + ldc.cst);
		//									System.out.printf("dec: ");
		//									System.out.println(method.invoke(null, ldc.cst.toString()));
		//								}
		//							}
		//						}
		//					}
		//				}
		//				break;
		//			}
		//		}

		//		Class<?> klass = simpgen.get(cache.get("org/osbot/D", "method_2", "(Ljava/lang/String;)Ljava/lang/String;"));
		//		Method m = klass.getDeclaredMethods()[0];
		//		for(MethodNode mn : contents.getClassContents().namedMap().get("org/osbot/D").methods) {
		//			for(AbstractInsnNode ain : mn.instructions.toArray()) {
		//				if(ain instanceof LdcInsnNode) {
		//					LdcInsnNode ldc = (LdcInsnNode) ain;
		//					if(ldc.cst instanceof String) {
		//						System.out.println("enc: " + escapeJavaStyleString(ldc.cst.toString(), true));
		//						System.out.print("dec: ");
		//						System.out.println(m.invoke(null, ldc.cst.toString()));
		//						System.out.println(m.invoke(null, new Object[]{
		//								ldc.cst.toString(),
		//								"org.osbot.COM1", mn.name}));
		//					}
		//				}
		//			}
		//		}

		//System.out.println("dec: " + org.osbot.D.method_2("0J>H<K \b0J>U!@ V"));

		patchgc();
		intercept_append();
		loaded_map();

		reflectno();

		dump();

		patchClient();
	}

	private static void loaded_map() {
		for(ClassNode cn : dl.getJarContents().getClassContents()) {
			//			org/osbot/auX.iIiIIIiiiI:java.util.Map

			if(cn.name.equals("org/osbot/auX")) {
				for(MethodNode m : cn.methods) {
					if(m.name.equals("method_13")) {
						for(AbstractInsnNode ain : m.instructions.toArray()) { 
							if(ain.opcode() == RETURN) {
								InsnList list = new InsnList();
								//								list.add(new VarInsnNode(ALOAD, 0));
								list.add(new FieldInsnNode(GETSTATIC, "org/osbot/auX", "iIiIIIiiiI", "Ljava/util/Map;"));
								list.add(new MethodInsnNode(INVOKESTATIC, check_map.class.getCanonicalName().replace(".", "/"), "check", "(Ljava/util/Map;)V", false));
								m.instructions.insertBefore(ain, list);
								System.out.println("Boot.loaded_map()");
							}
						}
					}
				}
			}
		}



		dl.getJarContents().getClassContents().add(ClassStructure.create(check_map.class.getCanonicalName()));

	}

	private static void intercept_append() {

		//public iiIiiIIIII(int arg0, java.lang.String arg1) { //(ILjava/lang/String;)Z

		for(ClassNode cn : dl.getJarContents().getClassContents()) {
			if(cn.name.equals("org/osbot/COm1")) {
				for(MethodNode m : cn.methods) {
					if(m.name.equals("iiIiiIIIII") && m.desc.equals("(ILjava/lang/String;)Z")) {
						InsnList list = new InsnList();
						list.add(new VarInsnNode(ILOAD, 1));
						list.add(new VarInsnNode(ALOAD, 2));
						list.add(new MethodInsnNode(INVOKESTATIC, append_print.class.getCanonicalName().replace(".", "/"), "print", "(ILjava/lang/String;)I", false));
						list.add(new VarInsnNode(ISTORE, 1));
						m.instructions.insertBefore(m.instructions.getFirst(), list);
						System.out.println("Boot.intercept_append()");
					}
				}
			}
		}
		dl.getJarContents().getClassContents().add(ClassStructure.create(append_print.class.getCanonicalName()));

	}

	private static void patchgc() {
		for(ClassNode cn : dl.getJarContents().getClassContents()) {
			if(cn.name.equals("org/osbot/Gc")) {
				for(MethodNode m : cn.methods) {
					if(m.name.equals("<init>")) {
						for(AbstractInsnNode ain : m.instructions.toArray()) {
							if(ain instanceof MethodInsnNode) {
								MethodInsnNode min = (MethodInsnNode) ain;
								if(min.owner.equals("org/osbot/IA") && min.name.equals("method_3")) {
									min.owner = ad_overide.class.getCanonicalName().replace(".", "/");
									min.name = "create";
									min.desc = "()L" + ad_overide.class.getCanonicalName().replace(".", "/") + ";";
									System.out.println("Boot.patchgc()");
								}
							}
						}
					}
				}
			}
		}

		dl.getJarContents().getClassContents().add(ClassStructure.create(ad_overide.class.getCanonicalName()));
	}

	private static void reflectno() {
		TreeBuilder tb = new TreeBuilder();

		NullPermeableHashMap<FieldNode, AtomicInteger> map = new NullPermeableHashMap<FieldNode, AtomicInteger>(new ValueCreator<AtomicInteger>() {
			@Override
			public AtomicInteger create() {
				return new AtomicInteger();
			}
		});

		NodeVisitor nv = new NodeVisitor() {
			@Override
			public void visitConstant(ConstantNode cn) {
				if(cn.cst() != null) {
					String cst = cn.cst().toString();
					if(cst.startsWith("org.osbot.")) {
						String bcst = cst.replace(".", "/");
						if(classes.containsKey(bcst)) {
							System.out.println("Replacing " + bcst + " with " + classes.get(bcst));
							cn.insn().cst = classes.get(bcst).replace("/", ".");
						}
					}
				}
			}
		};

		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				tb.build(m).accept(nv);
			}
		}
	}

	private static void removeDummyMethods() {
		//new HierarchyVisitor().accept(contents);
		//new CallVisitor().accept(contents);
	}

	public static String hex(char ch) {
		return Integer.toHexString(ch).toUpperCase();
	}

	public static String escapeJavaStyleString(String str, boolean escapeSingleQuote) {
		if (str == null) {
			return "";
		}
		int sz = str.length();
		StringBuilder sb = new StringBuilder(sz);
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);

			// handle unicode
			if (ch > 0xfff) {
				sb.append("\\u" + hex(ch));
			} else if (ch > 0xff) {
				sb.append("\\u0" + hex(ch));
			} else if (ch > 0x7f) {
				sb.append("\\u00" + hex(ch));
			} else if (ch < 32) {
				switch (ch) {
					case '\b':
						sb.append('\\');
						sb.append('b');
						break;
					case '\n':
						sb.append('\\');
						sb.append('n');
						break;
					case '\t':
						sb.append('\\');
						sb.append('t');
						break;
					case '\f':
						sb.append('\\');
						sb.append('f');
						break;
					case '\r':
						sb.append('\\');
						sb.append('r');
						break;
					default:
						if (ch > 0xf) {
							sb.append("\\u00" + hex(ch));
						} else {
							sb.append("\\u000" + hex(ch));
						}
						break;
				}
			} else {
				switch (ch) {
					case '\'':
						if (escapeSingleQuote) {
							sb.append('\\');
						}
						sb.append('\'');
						break;
					case '"':
						sb.append('\\');
						sb.append('"');
						break;
					case '\\':
						sb.append('\\');
						sb.append('\\');
						break;
					default:
						sb.append(ch);
						break;
				}
			}
		}
		return sb.toString();
	}

	public static String iIIiIIIiiI(String var0) {
		char[] var1 = var0.replace("_", " ").toLowerCase().toCharArray();

		int var2;
		for(int var10000 = var2 = 0; var10000 < var1.length; var10000 = var2) {
			if(var1[var2] == 32 || var1[var2] == 95) {
				var1[var2] = 32;
				if(var2 + 1 < var1.length && var1[var2 + 1] >= 97 && var1[var2 + 1] <= 122) {
					var1[var2 + 1] = (char)(var1[var2 + 1] + 65 - 97);
				}
			}

			++var2;
		}

		if(var1[0] >= 97 && var1[0] <= 122) {
			var1[0] = (char)(var1[0] + 65 - 97);
		}

		return new String(var1);
	}


	private static void nottheasshol() {
		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				if(m.localVariables != null && !m.localVariables.isEmpty()) {
					m.localVariables.clear();
				}
			}
		}	
	}

	private static void patchSimple(Collection<MethodNode> valid, Map<MethodNode, Class<?>> generated) {
		final Class<?>[] PARAMS = new Class[]{String.class};

		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				if(valid.contains(m))
					continue;

				NodeVisitor nv = new NodeVisitor() {
					@Override
					public void visitMethod(MethodMemberNode mmn) {
						if(mmn.children() > 0) {
							MethodInsnNode min = mmn.min();
							MethodNode callee = cache.get(min.owner, min.name, min.desc);

							if(callee != null && valid.contains(callee)) {

								ConstantNode cstn = mmn.firstConstant();
								if(cstn != null && cstn.cst() instanceof String) {
									String enc = (String) cstn.cst();
									Class<?> klass = generated.get(callee);
									try {
										Method refMethod = klass.getMethod(callee.name, PARAMS);
										refMethod.setAccessible(true);
										String dec = (String) refMethod.invoke(null, enc);
										// System.out.println(enc + " -> " + dec);
										cstn.insn().cst = dec;
										mmn.method().instructions.remove(min);
									} catch(Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				};

				TreeBuilder tb = new TreeBuilder();
				tb.build(m).accept(nv);
			}
		}
	}

	private static void patchComplex(Collection<MethodNode> valid, Map<MethodNode, Class<?>> generated) {
		final Class<?>[] PARAMS = new Class[]{String.class, String.class, String.class};

		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				if(valid.contains(m))
					continue;

				NodeVisitor nv = new NodeVisitor() {

					@Override
					public void visitConstant(ConstantNode cst) {
						if(cst.cst().toString().equals("rd_tEe\u0006}n_leC")) {
							//System.out.println("Boot.patchComplex() " + cst.parent().opname() + " " + cst.method() + " " + cst.parent().parent().opname());
						}
					}

					@Override
					public void visitMethod(MethodMemberNode mmn) {
						if(mmn.children() > 0) {
							MethodInsnNode min = mmn.min();
							MethodNode callee = cache.get(min.owner, min.name, min.desc);
							if(callee != null && valid.contains(callee)) {
								ConstantNode cstn = mmn.firstConstant();
								if(cstn != null && cstn.cst() instanceof String) {

									String enc = (String) cstn.cst();
									Class<?> klass = generated.get(callee);
									try {
										Method refMethod = klass.getMethod(callee.name, PARAMS);
										refMethod.setAccessible(true);
										String dec = (String) refMethod.invoke(null, enc, cn.name.replace("/", "."), m.name);

										cstn.insn().cst = dec;
										mmn.method().instructions.remove(min);
									} catch(Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				};

				TreeBuilder tb = new TreeBuilder();
				tb.build(m).accept(nv);
			}
		}
	}

	private static Map<MethodNode, Set<MethodNode>> graphCalls(Collection<MethodNode> valid) {
		NullPermeableHashMap<MethodNode, Set<MethodNode>> callMap = new NullPermeableHashMap<MethodNode, Set<MethodNode>>(new SetCreator<MethodNode>());
		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				for(AbstractInsnNode ain : m.instructions.toArray()) {
					if(ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						MethodNode callee = cache.get(min.owner, min.name, min.desc);
						if(callee != null && valid.contains(callee)) {
							callMap.getNonNull(callee).add(m); // add caller
						}
					}
				}
			}
		}

		return callMap;
	}

	private static Map<MethodNode, Class<?>> generateSimple(Collection<MethodNode> valid) throws ClassNotFoundException {
		Map<MethodNode, Class<?>> generated = new HashMap<MethodNode, Class<?>>();

		ClassLoaderExt cl = new ClassLoaderExt();
		int klass_count = 0;
		for(MethodNode m : valid) {
			ClassNode cn = new ClassNode();
			cn.access = ACC_PUBLIC;
			cn.version = V1_8;
			cn.superName = "java/lang/Object";
			cn.name = "Generated_Klass_" + (++klass_count);

			// Copy
			MethodNode mNew = new MethodNode(ASM5, cn, m.access, m.name, m.desc, m.signature, null){
				/**
				 * Label remapping.
				 * Old label -> new label.
				 */
				private final Map<Label, Label> labels = new HashMap<Label, Label>();
				@Override
				protected LabelNode getLabelNode(Label label) {
					Label newLabel = labels.get(label);
					if (newLabel == null) {
						newLabel = new Label();
						labels.put(label, newLabel);
					}
					return super.getLabelNode(newLabel);
				}
			};

			m.accept(mNew);
			cn.methods.add(mNew);
			cl.define(cn);

			Class<?> klass = cl.loadClass(cn.name);
			generated.put(m, klass);
		}

		return generated;
	}

	private static Map<MethodNode, Class<?>> generateComplex(Collection<MethodNode> valid) throws ClassNotFoundException {
		Map<MethodNode, Class<?>> generated = new HashMap<MethodNode, Class<?>>();

		ClassLoaderExt cl = new ClassLoaderExt();
		int klass_count = 0;
		for(MethodNode m : valid) {
			ClassNode cn = new ClassNode();
			cn.access = ACC_PUBLIC;
			cn.version = V1_8;
			cn.superName = "java/lang/Object";
			cn.name = "Generated_Klass_" + (++klass_count);

			// Copy
			MethodNode mNew = new MethodNode(ASM5, cn, m.access, m.name, m.desc, m.signature, null){
				/**
				 * Label remapping.
				 * Old label -> new label.
				 */
				private final Map<Label, Label> labels = new HashMap<Label, Label>();
				@Override
				protected LabelNode getLabelNode(Label label) {
					Label newLabel = labels.get(label);
					if (newLabel == null) {
						newLabel = new Label();
						labels.put(label, newLabel);
					}
					return super.getLabelNode(newLabel);
				}
			};

			m.accept(mNew);

			// (encrypted string, caller class, caller method) decrypted string
			mNew.desc = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
			// shift all loads/stores where vin.var > 0 (0 being the encrypted string local index)
			// by +2 (2 new params)

			for(AbstractInsnNode ain : mNew.instructions.toArray()) {
				if(ain instanceof VarInsnNode) {
					VarInsnNode vin = (VarInsnNode) ain;
					if(vin.var > 0) {
						vin.var += 2;
					}
				} else if(ain instanceof IincInsnNode) {
					IincInsnNode in = (IincInsnNode) ain;
					if(in.var > 0) {
						in.var += 2;
					}
				} else if(ain instanceof MethodInsnNode) {
					MethodInsnNode min = (MethodInsnNode) ain;
					if(min.owner.equals("java/lang/StackTraceElement")) {

						int index = -1;
						if(min.name.equals("getClassName")) {
							index = 1;
						} else if(min.name.equals("getMethodName")) {
							index = 2;
						}

						if(index != -1) {
							InsnList list = new InsnList();
							list.add(new InsnNode(POP));
							list.add(new VarInsnNode(ALOAD, index));
							mNew.instructions.insert(ain, list);
						}
					}
				}
			}

			cn.methods.add(mNew);

			// TODO: check for collisions
			cl.define(cn);

			Class<?> klass = cl.loadClass(cn.name);
			generated.put(m, klass);
		}

		return generated;
	}

	private static Set<MethodNode> findSimple2and3() {
		Set<MethodNode> valid = new HashSet<MethodNode>();

		for(ClassNode c : classList) {
			for(MethodNode m : c.methods) {
				if(m.desc.equals("(Ljava/lang/String;)Ljava/lang/String;")) {

					boolean fail = false;
					boolean len = false, chr = false, init = false;
					boolean rep = false, low = false, arr = false;

					//				    invokevirtual java/lang/String replace((Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;);
					//				    invokevirtual java/lang/String toLowerCase(()Ljava/lang/String;);
					//				    invokevirtual java/lang/String toCharArray(()[C);

					for(AbstractInsnNode ain : m.instructions.toArray()) {
						if(ain instanceof MethodInsnNode) {
							MethodInsnNode min = (MethodInsnNode) ain;
							if(min.owner.equals("java/lang/String")) {
								if(min.name.equals("length")) {
									len = true;
								} else if(min.name.equals("charAt")) {
									chr = true;
								} else if(min.name.equals("<init>")) {
									init = true;
								} else if(min.name.equals("replace")) {
									rep = true;
								} else if(min.name.equals("toLowerCase")) {
									low = true;
								} else if(min.name.equals("toCharArray")) {
									arr = true;
								} else {
									fail = true;
									break;
								}
							} else {
								fail = true;
								break;
							}
						} else if(ain instanceof FieldInsnNode) {
							fail = true;
							break;
						}
					}

					if(!fail) {
						//	if(len && ((chr && init) || (rep && low && arr))) {
						if(len && chr && init) {
							valid.add(m);
							two++;
						} else if(rep && low && arr) {
							// TODO: three is actually put in by them to encode
							valid.add(m);
							threes = m;
							three++;
						}

					}

				}
			}
		}

		return valid;
	}

	private static Set<MethodNode> findComplex() {
		Set<MethodNode> valid = new HashSet<MethodNode>();
		for(ClassNode c : classList) {
			for(MethodNode m : c.methods) {
				boolean se = false, sb = false, s = false, e = false, cn = false, mn = false;

				boolean fail = false;
				for(AbstractInsnNode ain : m.instructions.toArray()) {
					if(ain instanceof MethodInsnNode) {
						MethodInsnNode min = (MethodInsnNode) ain;
						if(min.owner.equals("java/lang/StackTraceElement")) {
							se = true;

							if(min.name.equals("getClassName")) {
								cn = true;
							} else if(min.name.equals("getMethodName")) {
								mn = true;
							}
						} else if(min.owner.equals("java/lang/Exception")) {
							e = true;
						} else if(min.owner.equals("java/lang/String")) {
							s = true;
						} else if(min.owner.equals("java/lang/StringBuffer")) {
							sb = true;
						} else {
							fail = true;
						}
					}
				}

				if(c.name.equals("org/osbot/CB") && m.name.equals("iIIiIIIiiI")) {
					System.out.println(fail);
					System.out.println(Arrays.toString(new boolean[]{se, sb, s, e, cn, mn}));
				}

				//boolean se, sb, s, e, cn, mn;
				if(fail) {
					// System.err.println(m);
				} else if(!se || !sb || !s || !e || !cn || !mn){
					//System.err.println("fail2: " + m);
				} else {
					// always static anyway.
					valid.add(m);
				}
			}
		}

		return valid;
	}
	private static void openmyassdaddy() {
		for(ClassNode cn : classList) {
			cn.access |= ACC_PUBLIC;
			cn.access &= ~ACC_PRIVATE;
			cn.access &= ~ACC_PROTECTED;

			for(MethodNode m : cn.methods) {
				m.access |= ACC_PUBLIC;
				m.access &= ~ACC_PRIVATE;
				m.access &= ~ACC_PROTECTED;
			}
		}
	}

	private static void unfuck() {
		ClassTree classTree = new ClassTree(classList);

		//		classes.put("org/osbot/AB",  "org/objectweb/asm/commons/TryCatchBlockSorter");
		//		classes.put("org/osbot/G",   "org/objectweb/asm/commons/JSRInlinerAdapter");
		//		classes.put("org/osbot/db",  "org/objectweb/asm/commons/AnalyzerAdapter");
		//		classes.put("org/osbot/eb",  "org/objectweb/asm/commons/LocalVariablesSorter");
		//
		//		classes.put("org/osbot/Dc",  "org/objectweb/asm/ClassWriter");
		//		classes.put("org/osbot/xB",  "org/objectweb/asm/MethodVisitor");
		//		classes.put("org/osbot/Cc",  "org/objectweb/asm/ClassVisitor");
		//		classes.put("org/osbot/aUx", "org/objectweb/asm/Opcodes");
		//		classes.put("org/osbot/PA",  "org/objectweb/asm/MethodWriter");
		//		classes.put("org/osbot/XA",  "org/objectweb/asm/util/CheckClassAdapter");
		//
		//		classes.put("org/osbot/uA",  "org/objectweb/asm/tree/MethodNode");
		//		classes.put("org/osbot/Cb",  "org/objectweb/asm/tree/InsnList");
		//		classes.put("org/osbot/cb",  "org/objectweb/asm/tree/AbstractInsnNode");
		//		classes.put("org/osbot/tB",  "org/objectweb/asm/tree/TypeAnnotationNode");
		//		classes.put("org/osbot/j",   "org/objectweb/asm/tree/LabelNode");
		//		
		//		classes.put("org/osbot/aB",   "org/objectweb/asm/xml/SAXClassAdapter");
		//		classes.put("org/osbot/gB",   "org/objectweb/asm/xml/Rule");
		//		classes.put("org/osbot/dA",   "org/objectweb/asm/xml/MaxRule");
		//		classes.put("org/osbot/aA",   "org/objectweb/asm/xml/InterfacesRule");
		//		classes.put("org/osbot/cc",   "org/objectweb/asm/xml/AnnotationValueRule");
		//		classes.put("org/osbot/LPt2", "org/objectweb/asm/Handle");
		//		classes.put("org/osbot/T",    "org/objectweb/asm/Type");
		//		classes.put("org/osbot/x",    "org/objectweb/asm/ClassReader");
		//		classes.put("org/osbot/Y",    "org/objectweb/asm/tree/ClassNode");
		//		
		//		classes.put("org/osbot/LPT1", "org/objectweb/asm/util/TraceSignatureVisitor");
		//		classes.put("org/osbot/zA",    "org/objectweb/asm/util/ASMifier");
		//
		//		classes.put("org/osbot/e",    "org/objectweb/asm/signature/SignatureVisitor");
		//		classes.put("org/osbot/pA",   "org/objectweb/asm/tree/analysis/Analyzer");
		//		classes.put("org/osbot/lPT7", "org/objectweb/asm/tree/analysis/AnalyzerException");
		//		classes.put("org/osbot/lPt1", "org/objectweb/asm/tree/analysis/BasicInterpreter");
		//		classes.put("org/osbot/Lpt4", "org/objectweb/asm/tree/analysis/BasicVerfier");
		//		classes.put("org/osbot/LPt1", "org/objectweb/asm/tree/analysis/SimpleVerifier");
		//		classes.put("org/osbot/lPt5", "org/objectweb/asm/tree/analysis/Frame");
		//
		//		classes.put("org/osbot/com4", "org/nullbool/hook/HookWriter");
		//		classes.put("org/osbot/cOM3", "org/nullbool/hook/FieldHook");
		//
		//		classes.put("org/osbot/com1", "org/nullbool/client/TooltipCallbackInjector");
		//
		//		classes.put("org/osbot/COm2", "org/nullbool/client/BotRefCallbackInjector");
		//		classes.put("org/osbot/cOM1", "org/nullbool/client/LoginReturnInjector");
		//		classes.put("org/osbot/COm1", "org/nullbool/client/SkipDrawInjector");
		//		classes.put("org/osbot/CoM1", "org/nullbool/client/HitsplatCallbackInjector");
		//		classes.put("org/osbot/cOm1", "org/nullbool/client/HeadMessageCallbackInjector");
		//		classes.put("org/osbot/Com1", "org/nullbool/client/ModelCallbackInjector");
		//		classes.put("org/osbot/coM2", "org/nullbool/client/DefinitionTransformationCallInjector");
		//		classes.put("org/osbot/coM1", "org/nullbool/client/ConfigCallbackInjector");
		//		classes.put("org/osbot/pRN",  "org/nullbool/client/ChatboxMessageCallbackInjector");
		//		classes.put("org/osbot/cOm5", "org/nullbool/client/ClassAndFieldAccessorInjector");
		//		classes.put("org/osbot/COm3", "org/nullbool/client/RandomDatPatchForBotNumberInjector");
		//		classes.put("org/osbot/PRn",  "org/nullbool/client/CanvasInjector");
		//		classes.put("org/osbot/CoM6", "org/nullbool/client/ClassThatInjectsBotReferenceIntoClient");
		//		classes.put("org/osbot/Com4", "org/nullbool/client/ComponentReshapeInjector");
		//		classes.put("org/osbot/coM4", "org/nullbool/client/FilterInjector");
		//
		//		classes.put("org/osbot/cOm4", "org/nullbool/game/WorldHopRunnable");
		//		
		//
		//		classes.put("org/osbot/con", "org/nullbool/script/ScriptPermissions");
		//		classes.put("org/osbot/Con", "org/nullbool/script/SDNHandler");
		//		
		//
		//		classes.put("org/osbot/dc",   "org/nullbool/util/InputStreamMonitor");
		//		classes.put("org/osbot/COM2", "org/nullbool/util/BotTabHelper");
		//		classes.put("org/osbot/Com2", "org/nullbool/util/AccountSetterRunnable");
		//		classes.put("org/osbot/com2", "org/nullbool/util/jagex/RSByteBuffer");
		//		
		//		classes.put("org/osbot/COn", "org/nullbool/util/net/VersionChecker");
		//		classes.put("org/osbot/COM1", "org/nullbool/util/net/WebServiceRequester");
		//		classes.put("org/osbot/CON",  "org/nullbool/util/net/SSLTrustManager");
		//		
		//
		//		classes.put("org/osbot/COM3", "org/nullbool/ui/AccountSelectorSelectionListener");
		//		classes.put("org/osbot/coM3", "org/nullbool/ui/MultiComponentEnabler");
		//		classes.put("org/osbot/COm4", "org/nullbool/ui/SetVisibleRunnable");
		//		classes.put("org/osbot/COM6", "org/nullbool/ui/BotFrame");
		//		classes.put("org/osbot/Db",   "org/nullbool/ui/CustomTableModel");
		//		
		//		
		//		classes.put("org/osbot/CB", "org/nullbool/util/StringEncoding");

		BytecodeRefactorer refactorer = new BytecodeRefactorer(dl.getJarContents().getClassContents(), new IRemapper() {

			@Override
			public String resolveMethodName(String owner, String name, String desc, boolean isStatic) {
				if(owner.equals(threes.owner.name) && name.equals(threes.name) && desc.equals(threes.desc)) {
					return "threessss";
				}

				//TODO:
				//				for(MethodNode m : complex) {
				//					if(owner.equals(m.owner.name) && name.equals(m.name) && desc.equals(m.desc)) {
				//						return "decrypt_complex";
				//					}
				//				}
				//				for(MethodNode m : simple) {
				//					if(owner.equals(m.owner.name) && name.equals(m.name) && desc.equals(m.desc)) {
				//						return "decrypt_simple";
				//					}
				//				}


				MethodNode mn = cache.get(owner, name, desc);
				String rep = name.replace("i", "").replace("I", "");
				if(mn != null && Modifier.isStatic(mn.access) && rep.length() == 0) {
					String newName = "method_" + mn.owner.methods.indexOf(mn);
					methods.put(mn.key(), owner + "." + newName + desc);
					return newName;
				}

				return name;
			}

			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				//if(KEYWORDS.contains(name))
				//	return "field_" + name;

				//				if(fields.containsKey(owner)) {
				//					return fields.get(owner);
				//				}
				//
				//				String rep = name.replace("i", "").replace("I", "");
				//				if(owner.startsWith("org/osbot") && rep.length() == 0) {
				//					ClassNode cn = classTree.getClass(owner);
				//					for(FieldNode fn : cn.fields) {
				//						if(fn.name.equals(name) && fn.desc.equals(desc)) {
				//							String newName = "field_" + (cn.fields.indexOf(fn));
				//							fields.put(name, newName);
				//							return newName;			
				//						}
				//					}
				//				}

				return name;
			}

			int c_count = 0;

			@Override
			public String resolveClassName(String oldName) {
				if(classes.containsKey(oldName))
					return classes.get(oldName);

				//				String upper = ClassUtil.getClassName(oldName).toUpperCase();
				//				if(classTree.getClass(oldName) != null && ILLEGAL_NAMES.contains(upper)) {
				//					String newName = "org/nullbool/Klass" + (c_count++);
				//					classes.put(oldName, newName);
				//					return newName;
				//				}

				return oldName;
			}
		});

		refactorer.start();
	}

	private static void removeUnreachableCode() throws ControlFlowException {
		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					IControlFlowGraph graph = new InsaneControlFlowGraph();
					graph.create(m);

					for(FlowBlock b : graph.blocks()) {
						boolean start = false;
						for(AbstractInsnNode ain : b.insns()) {
							if(!start) {
								if(InstructionUtil.isUnconditional(ain.opcode()) || InstructionUtil.isExit(ain.opcode())) {
									start = true;
								}
							} else {
								m.instructions.remove(ain);
							}
						}
					}

					graph.destroy();
				}
			}
		}
	}

	private static void dump() throws IOException {
		//TODO: full
		new CompleteJarDumper(dl.getJarContents()){

			@Override
			public int dumpClass(JarOutputStream out, String name, ClassNode cn) throws IOException {
				if(name.startsWith("org/") && !(name.startsWith("org/pushingpixels/") || name.startsWith("org/xmlpull/")))
					return super.dumpClass(out, name, cn);
				return 0;
			}
			@Override
			public int dumpResource(JarOutputStream out, String name, byte[] file) throws IOException {
				//if(name.startsWith("META-INF/SERVER."))
				//	return 0;

				//return super.dumpResource(out, name, file);

				return 0;
			}
		}.dump(out);
	}


	private static final Set<String> KEYWORDS = new HashSet<String>();
	private static final Set<String> ILLEGAL_NAMES = new HashSet<String>();

	static {
		String ks[] = { "abstract", "assert", "boolean",
				"break", "byte", "case", "catch", "char", "class", "const",
				"continue", "default", "do", "double", "else", "extends", "false",
				"final", "finally", "float", "for", "goto", "if", "implements",
				"import", "instanceof", "int", "interface", "long", "native",
				"new", "null", "package", "private", "protected", "public",
				"return", "short", "static", "strictfp", "super", "switch",
				"synchronized", "this", "throw", "throws", "transient", "true",
				"try", "void", "volatile", "while" };
		String[] iln = { "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", 
				"lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "con", "nul", "prn"};

		for(String s : ks) {
			KEYWORDS.add(s);
		}
		for(String s : iln) {
			ILLEGAL_NAMES.add(s.toUpperCase());
		}
	}


	//TODO: remove
	static String pass = "ATNFAimpnP9RdWwc8ijtXEWkHzB2id";
}