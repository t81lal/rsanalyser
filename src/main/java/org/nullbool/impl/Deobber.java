package org.nullbool.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.Revision;
import org.nullbool.api.obfuscation.refactor.BytecodeRefactorer;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.IRemapper;
import org.nullbool.impl.redeob.CallVisitor;
import org.nullbool.impl.redeob.CatchBlockFixer;
import org.nullbool.impl.redeob.ComparisonReorderer;
import org.nullbool.impl.redeob.FieldOpener;
import org.nullbool.impl.redeob.HierarchyVisitor;
import org.nullbool.impl.redeob.OpaquePredicateRemover;
import org.nullbool.impl.redeob.SimpleArithmeticFixer;
import org.nullbool.impl.redeob.UnusedFieldRemover;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.nullbool.pi.core.hook.api.serialisation.IMapSerialisationFactory;
import org.nullbool.pi.core.hook.serimpl.StaticMapDeserialiserImpl;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteio.out.CompleteJarDumper;

/**
 * @author Bibl (don't ban me pls)
 * @created 30 Jul 2015 16:16:20
 */
public class Deobber {

	private static final int revision = 86;
	private static TreeBuilder builder = new TreeBuilder();
	private static Map<String, ClassNode> classNodes;
	private static JarContents<ClassNode> contents;
	private static ClassTree classTree;
	
	public static void main(String[] _args) throws Exception {
		File base = new File("out");
		base = new File(base, Integer.toString(revision));

		File log = new File(base, "log.ser");
		HookMap hooks = loadHooks(new FileInputStream(log));

		File in = file(revision);
		File tmp = new File(in.getParentFile(), "tmp.jar");
//		String[] args = new String[]{in.getAbsolutePath(), tmp.getAbsolutePath()};
//		Deobfuscator.main(args);
		tmp = in;
		
		Revision rev = rev(revision, tmp);
		classNodes = rev.parse();
		contents = new JarContents<ClassNode>();
		Collection<ClassNode> nodes = classNodes.values();
		contents.getClassContents().addAll(nodes);
		
		new HierarchyVisitor().accept(contents);
		new CallVisitor().accept(contents);
		new UnusedFieldRemover().visit(contents);
		new FieldOpener().visit(contents);
		deob();
		
		classTree = new ClassTree(classNodes);

		AtomicInteger k = new AtomicInteger();
		Map<MethodNode, String> mapped = new HashMap<MethodNode, String>();
		IRemapper remapper = new IRemapper() {
			@Override
			public String resolveMethodName(String owner, String name, String desc, boolean isStatic) {
				if(true) {
					if(owner.indexOf('/') == -1 && isStatic && !name.equals("<clinit>")) {
						MethodNode m = resolveMethod(owner, name, desc, isStatic);
						if(mapped.containsKey(m))
							return mapped.get(mapped);
						String s = "method_" + (k.incrementAndGet());
						mapped.put(m, s);
						return s;
					}
					return name;
				}
				
				if(owner.indexOf('/') == -1) {
					MethodNode m = resolveMethod(owner, name, desc, isStatic);
					if(m != null) {
						for(ClassHook ch : hooks.classes()) {
							for(MethodHook mh : ch.methods()) {
								if(mh.obfuscated().equals(name)) {
									String o = mh.val(Constants.REAL_OWNER);
									String d = mh.val(Constants.REFACTORED_DESC);
									String stat = mh.val(Constants.STATIC);
									boolean s = Boolean.valueOf(stat);
									if(s == isStatic) {
										if(o.equals(owner) && d.equals(desc)) {
											return mh.refactored();
										}
									}
								}
							}
						}
						return m.name;
					} else {
						throw new RuntimeException(String.format("Couldn't resolve %s.%s %s (%b)", owner, name, desc, isStatic));
					}
				}
				return name;
			}

			@Override
			public String resolveFieldName(String owner, String name, String desc, boolean isStatic) {
				if(name.equals("do") || name.equals("if")) {
					return "f_" + name;
				}
				
				if(owner.indexOf('/') == -1) {
					FieldNode f = resolve(owner, name, desc, isStatic);
					if(f != null) {
						ClassHook ch = hooks.forName(f.owner.name, true);
						if(ch != null) {
							for(FieldHook fh : ch.fields()) {
								if(fh.obfuscated().equals(name) && fh.val(Constants.DESC).equals(desc)) {
									return fh.refactored();
								}
							}
						}
						return f.name;
					} else {
						throw new RuntimeException(String.format("Couldn't resolve %s.%s %s (%b)", owner, name, desc, isStatic));
					}
				}
				
				return name;
			}

			@Override
			public String resolveClassName(String oldName) {
				ClassHook ch = hooks.forName(oldName, true);
				if(ch != null) {
					return "rs/" + ch.refactored();
				}
				if(oldName.equals("if") || oldName.equals("do")) {
					return "rs/klass" + oldName;
				}
				if(oldName.indexOf('/') == -1) {
					return "rs/" + oldName;
				}
				return oldName;
			}
		};
		BytecodeRefactorer refactorer = new BytecodeRefactorer(nodes, remapper);
		refactorer.start();
		new CompleteJarDumper(contents).dump(new File(base, "redeob.jar"));
	}
	
	public static FieldNode resolve(String owner, String name, String desc, boolean isStatic) {
		ClassNode cn = classTree.getClass(owner);
		FieldNode f = resolve(cn, name, desc, isStatic);
		if(f != null) {
			return f;
		}
		
		for(ClassNode _cn : classTree.getSupers(cn)) {
			f = resolve(_cn, name, desc, isStatic);
			if(f != null) {
				return f;
			}
		}
		
		return null;
	}
	
	public static FieldNode resolve(ClassNode cn, String name, String desc, boolean isStatic) {
		for(FieldNode f : cn.fields) {
			if(Modifier.isStatic(f.access) == isStatic) {
				if(f.name.equals(name) && f.desc.equals(desc)) {
					return f;
				}
			}
		}
		return null;
	}
	
	public static MethodNode resolveMethod(ClassNode cn, String name, String desc, boolean isStatic) {
		for(MethodNode m : cn.methods) {
			if(Modifier.isStatic(m.access) == isStatic) {
				if(m.name.equals(name) && m.desc.equals(desc)) {
					return m;
				}
			}
		}
		return null;
	}
	
	public static MethodNode resolveMethod(String owner, String name, String desc, boolean isStatic) {
		ClassNode cn = classTree.getClass(owner);
		MethodNode m = resolveMethod(cn, name, desc, isStatic);
		if(m != null) {
			return m;
		}
		
		for(ClassNode _cn : classTree.getSupers(cn)) {
			m = resolveMethod(_cn, name, desc, isStatic);
			if(m != null) {
				return m;
			}
		}
		
		return null;
	}

	private static void deob() {
		NodeVisitor fixer = new SimpleArithmeticFixer();

		for(ClassNode cn : classNodes.values()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(fixer);
				}
			}
		}
		
		fixer = new ComparisonReorderer();

		for(ClassNode cn : classNodes.values()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					builder.build(m).accept(fixer);
				}
			}
		}
		
		OpaquePredicateRemover opaqueRemover = new OpaquePredicateRemover();

		for(ClassNode cn : classNodes.values()) {
			for(MethodNode m : cn.methods) {
				if(m.instructions.size() > 0) {
					if(opaqueRemover.methodEnter(m)) {
						builder.build(m).accept(opaqueRemover);
						opaqueRemover.methodExit();
					}
				}
			}
		}
		

//		EmptyParameterFixer emptyParameterFixer = new EmptyParameterFixer();
//		emptyParameterFixer.visit(contents);
		
		CatchBlockFixer.rek(contents.getClassContents());
	}

	public static Revision rev(int revision, File f) throws Exception {
		return new Revision(Integer.toString(revision), f);
	}
	
	public static File file(int revision) throws Exception {
		return new File(Boot.class.getResource(
				"/jars/gamepack" + revision + ".jar").toURI());
	}
	
	public static HookMap loadHooks(InputStream is) throws IOException {
		int avail = is.available();
		if(avail > 0) {
			//byte[] bytes = new byte[avail];
			//is.read(bytes, 0, bytes.length);

			StringBuilder sb = new StringBuilder();

			// content type ends with '\n'
			int b;
			while((b = is.read()) != (int)'\n' && b != -1) {
				sb.append((char)b);
			}

			if(b == -1) {
				throw new IOException("Empty data.");
			}

			String str = sb.toString();

			if(str != null && str.startsWith(IMapSerialisationFactory.CONTENT_TYPE) && str.contains("=")) {
				StaticMapDeserialiserImpl deserialiser = new StaticMapDeserialiserImpl();
				HookMap map = deserialiser.deserialise(is);
				return map;
			}
		} else {
			throw new IOException("No data available.");
		}
		return null;
	}
}