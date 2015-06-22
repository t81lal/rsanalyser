/**
 * 
 */
package org.nullbool.byeallatori;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.util.ClassUtil;
import org.topdank.byteengineer.commons.data.JarInfo;
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

		payload = (new StringBuilder()).insert(0, "name=").append("willie lynch")
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
	
	private static File out;
	private static SingleJarDownloader<ClassNode> dl;
	private static List<ClassNode> classList;
	private static MethodCache cache;
	private static int two = 0, three = 0;
	private static MethodNode threes;
	private static Set<MethodNode> complex, simple;

	public static void main(String[] args) throws Exception {

		//		System.out.println(iIIiIIIiiI("rd_\u007fTee\u0006eraN"));

		request();
		// SESSION = "655207082866909a0-f69b-43d1-90f6-6804ff43cdb0";
		byte[] hooks = hooks();
		parseHooks(ByteBuffer.wrap(hooks));
		System.out.println(hooks.length);
		
		System.out.println(MiscHelper.add());
		if(true)
			return;

		File dir = new File("C:/Users/Bibl/Desktop/osbots shit nigga");
		File pb = new File(dir, "osb.jar");
		out = new File(dir, "osbout.jar");

		dl = new SingleJarDownloader<ClassNode>(new JarInfo(pb));
		dl.download();

		classList = dl.getJarContents().getClassContents();
		cache = new MethodCache(classList);

		nottheasshol();
		removeUnreachableCode();

		complex = findComplex();
		//Map<MethodNode, Set<MethodNode>> complexCalls = graphCalls(complex);
		Map<MethodNode, Class<?>> complexgen = generateComplex(complex);

		Class<?> klass = complexgen.get(cache.get("org/osbot/COM1", "iiIIiiiIiI", "(Ljava/lang/String;)Ljava/lang/String;"));
		Method m = klass.getDeclaredMethods()[0];

		// "zbQxVlV\"\u00198Fl3v~Cq@\u001e D\u0010e+\u0000\"\u00020\fA)\u000b\u00019\u00110MsL\u007f\u000f#\u001b8\u001aGRnGyY~+\u0010\'\u001d$\u001fXs\"7CnPcI$\u001f.Hl"

		for(MethodNode mn : cache.get("org/osbot/COM1", "iiIIiiiIiI", "(Ljava/lang/String;)Ljava/lang/String;").owner.methods) {
			for(AbstractInsnNode ain : mn.instructions.toArray()) {
				if(ain instanceof LdcInsnNode) {
					LdcInsnNode ldc = (LdcInsnNode) ain;
					if(ldc.cst instanceof String) {
						System.out.println("enc: " + escapeJavaStyleString(ldc.cst.toString(), true));
						System.out.print("dec: ");
						System.out.println(m.invoke(null, new Object[]{
								ldc.cst.toString(),
								"org.osbot.COM1", mn.name}));
					}
				}
			}
		}


		patchComplex(complex, complexgen);



		simple = findSimple2and3();
		Map<MethodNode, Class<?>> simpgen = generateSimple(simple);
		patchSimple(simple, simpgen);

		System.out.printf("%d two, %d three.%n", two, three);

		openmyassdaddy();
		unfuck();
		dump();
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
		Map<String, String> classes = new HashMap<String, String>();
		Map<String, String> methods = new HashMap<String, String>();
		Map<String, String> fields = new HashMap<String, String>();

		BytecodeRefactorer refactorer = new BytecodeRefactorer(dl.getJarContents().getClassContents(), new IRemapper() {

			@Override
			public String resolveMethodName(String owner, String name, String desc) {
				if(owner.equals(threes.owner.name) && name.equals(threes.name) && desc.equals(threes.desc)) {
					return "threessss";
				}

				//TODO:
				//				for(MethodNode m : complex) {
				//					if(owner.equals(m.owner.name) && name.equals(m.name) && desc.equals(m.desc)) {
				//						return "decrypt_complex";
				//					}
				//				}
				//
				//				for(MethodNode m : simple) {
				//					if(owner.equals(m.owner.name) && name.equals(m.name) && desc.equals(m.desc)) {
				//						return "decrypt_simple";
				//					}
				//				}

				
				MethodNode mn = cache.get(owner, name, desc);
				if(mn != null && Modifier.isStatic(mn.access)) {
					return "method_" + mn.owner.methods.indexOf(mn);
				}
				
				return name;
			}

			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				//if(KEYWORDS.contains(name))
				//	return "field_" + name;

				if(fields.containsKey(owner)) {
					return fields.get(owner);
				}

				String rep = name.replace("i", "").replace("I", "");
				if(owner.startsWith("org/osbot") && rep.length() == 0) {
					String newName = "field_" + (f_count++);
					fields.put(name, newName);
					return newName;			
				}

				return name;
			}

			int c_count = 0;
			int f_count = 0;
			int m_count = 0;

			@Override
			public String resolveClassName(String oldName) {
				if(classes.containsKey(oldName))
					return classes.get(oldName);

				String upper = ClassUtil.getClassName(oldName).toUpperCase();
				if(classTree.getClass(oldName) != null && ILLEGAL_NAMES.contains(upper)) {
					String newName = "org/nullbool/Klass" + (c_count++);
					classes.put(oldName, newName);
					return newName;
				}

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
		new CompleteJarDumper(dl.getJarContents()){
			@Override
			public int dumpResource(JarOutputStream out, String name, byte[] file) throws IOException {
				if(name.startsWith("META-INF/SERVER."))
					return 0;

				return super.dumpResource(out, name, file);
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
	static String pass = "0x90n0p";
	
}