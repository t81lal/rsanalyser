package org.nullbool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;

import org.nullbool.api.analysis.ClassAnalyser;
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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.util.ClassUtil;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.JarInfo;
import org.topdank.byteengineer.commons.data.LocateableJarContents;
import org.topdank.byteio.in.SingleJarDownloader;
import org.topdank.byteio.out.CompleteJarDumper;

public class AntiPowerbot implements Opcodes {

	public static void main(String[] args) throws Exception {
		File dir = new File("C:/Users/Bibl/Desktop/osbots shit nigga");
		//File pb = new File(dir, "RSBot-6064.jar");
		//File out = new File(dir, "pbout.jar");
		File pb = new File(dir, "osb.jar");
		File out = new File(dir, "osbout.jar");
		
		SingleJarDownloader<ClassNode> dl = new SingleJarDownloader<ClassNode>(new JarInfo(pb));
		dl.download();

		List<ClassNode> classList = dl.getJarContents().getClassContents();
		removeUnreachableCode(classList);
		dupduppop2(classList);
		MethodCache cache = new MethodCache(classList);
		
		// allatori doesn't use jsr/ret
		
		for(ClassNode cn : classList) {
			for(MethodNode m : cn.methods) {
				if(m.localVariables != null && !m.localVariables.isEmpty()) {
					m.localVariables.clear();
				}
			}
		}
		
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

		final Class<?>[] PARAMS = new Class[]{String.class, String.class, String.class};

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

		System.out.println(valid.size());

		Set<String> KEYWORDS = new HashSet<String>();
		Set<String> ILLEGAL_NAMES = new HashSet<String>();
		
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

		ClassTree classTree = new ClassTree(classList);
		Map<String, String> classes = new HashMap<String, String>();
		Map<String, String> fields = new HashMap<String, String>();

		BytecodeRefactorer refactorer = new BytecodeRefactorer(dl.getJarContents().getClassContents(), new IRemapper() {

			@Override
			public String resolveMethodName(String owner, String name, String desc) {
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

		for(ClassNode cn : classList) {
			if(!classTree.getClasses().values().contains(cn)) {
				System.err.println("NO " + cn);
			}
		}
		System.out.println(classList.size());
		System.out.println(classTree.getClasses().size());
		LocateableJarContents<ClassNode> contents = new LocateableJarContents<ClassNode>(new JarContents.ClassNodeContainer<ClassNode>(classList), dl.getJarContents().getResourceContents(), dl.getJarContents().getJarUrls());
		
		new CompleteJarDumper(contents){
			@Override
			public int dumpResource(JarOutputStream out, String name, byte[] file) throws IOException {
				if(name.startsWith("META-INF/SERVER."))
					return 0;

				//if(name.equalsIgnoreCase("META-INF/MANIFEST.MF") && classes.containsKey(bootClass)) {
				//	file = new String(file).replace("Main-Class: org.powerbot.Boot", "Main-Class: " + classes.get(bootClass)).getBytes();
				//}

				return super.dumpResource(out, name, file);
			}
		}.dump(out);
	}
	
	private static void dupduppop2(Collection<ClassNode> classes) {
		for(ClassNode cn : classes) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					List<AbstractInsnNode[]> pat = ClassAnalyser.findAllOpcodePatterns(m, new int[]{DUP, DUP, POP2});
					if(pat != null && !pat.isEmpty()) {
						
						if(m.name.equals("main")) {
							for(AbstractInsnNode[] pat0 : pat) {
								System.out.println(Arrays.toString(pat0));
								for(AbstractInsnNode a : pat0) {
									m.instructions.remove(a);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void removeUnreachableCode(Collection<ClassNode> classes) throws ControlFlowException {
		for(ClassNode cn : classes) {
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

	public static void addPrints() {
		/*for(MethodNode m : valid) {
		//if(!(m.owner.name.equals("z/lpT9") && m.name.equals("try")))
		//	continue;

		ListIterator<AbstractInsnNode> it = m.instructions.iterator();
		while(it.hasNext()) {
			AbstractInsnNode ain = it.next();
			if(ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if(min.owner.equals("java/lang/StringBuffer") && min.name.equals("toString")) {

					InsnList list = new InsnList();
					list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
					list.add(new InsnNode(SWAP));
					list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));

					m.instructions.insert(min, list);
					m.instructions.insert(min, new InsnNode(DUP));

					InsnList two = new InsnList();
					two.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
					two.add(new LdcInsnNode("Calling: (" + m.owner + "." + m.name + ") with: "));
					two.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
					two.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
					two.add(new VarInsnNode(ALOAD, 0));
					two.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));


					two.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
					two.add(new LdcInsnNode("Called from: "));
					two.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false));

					m.instructions.insert(min, two);
				}
			} else if(ain.opcode() == ARETURN) {
				AbstractInsnNode prev = ain.getPrevious();

				InsnList list = new InsnList();
				list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
				list.add(new InsnNode(SWAP));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));

				m.instructions.insert(prev, list);
				m.instructions.insert(prev, new InsnNode(DUP));

				InsnList two = new InsnList();
				two.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
				two.add(new LdcInsnNode("Returning: "));
				two.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false));
				m.instructions.insert(prev, two);
			}
		}
	}*/
	}

	public static String t(final String s) {
		final StackTraceElement stackTraceElement = new Exception().getStackTrace()[1];
		System.out.println(stackTraceElement);
		final String string = new StringBuffer("try").append("tryorg.powerbot.bot.rt6.client.input.Canvas").toString();
		final int n = string.length() - 1;
		final int n2 = (0x2 ^ 0x5) << 4 ^ (3 << 2 ^ 0x1);
		final int n3 = 3;
		final int n4 = n3 << n3;
		final int length = s.length();
		final char[] array = new char[length];
		int n5;
		int i = n5 = length - 1;
		final char[] array2 = array;
		final int n6 = n4;
		final int n7 = n2;
		int n8 = n;
		final int n9 = n;
		while (i >= 0) {
			final char[] array3 = array2;
			final int n10 = n7;
			final int n11 = n5--;
			array3[n11] = (char)(n10 ^ (s.charAt(n11) ^ string.charAt(n8)));
			if (n5 < 0) {
				final char[] array4 = array2;
				return new String(array4);
			}
			final char[] array5 = array2;
			final int n12 = n6;
			final int n13 = n5;
			final char c = (char)(n12 ^ (s.charAt(n13) ^ string.charAt(n8)));
			--n5;
			--n8;
			array5[n13] = c;
			if (n8 < 0) {
				n8 = n9;
			}
			i = n5;
		}
		final char[] array4 = array2;
		return new String(array4);
	}
	
	public static final class ClassLoaderExt extends ClassLoader {
		@SuppressWarnings("deprecation")
		public Class<?> define(ClassNode cn) {
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			byte[] bytes = cw.toByteArray();
			return defineClass(cn.name, bytes, 0, bytes.length);
	    }
	}
}