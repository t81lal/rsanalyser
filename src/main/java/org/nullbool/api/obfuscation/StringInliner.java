package org.nullbool.api.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.nullbool.api.obfuscation.refactor.DataCache;
import org.nullbool.api.obfuscation.refactor.FieldCache;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.FieldNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * @author Bibl (don't ban me pls)
 * @created 8 Jul 2015 03:17:16
 */
public class StringInliner extends Visitor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nullbool.api.obfuscation.Visitor#visit(org.topdank.byteengineer.commons
	 * .data.JarContents)
	 */
	@Override
	public void visit(JarContents<? extends ClassNode> contents) {

		int stats = 0;
		int statfins = 0;

		List<? extends ClassNode> classes = contents.getClassContents();
		@SuppressWarnings("unchecked")
		DataCache<FieldNode> cache = new FieldCache((Collection<ClassNode>) classes);
		ClassNode str = findStringClass(classes);
		Map<FieldNode, String> callmap = buildvalmap(str);

		for (ClassNode cn : classes) {
			for (MethodNode m : cn.methods) {
				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain.getOpcode() == GETSTATIC) {
						FieldInsnNode fin = (FieldInsnNode) ain;
						FieldNode f = cache.get(fin.owner, fin.name, fin.desc);
						if (f != null && Modifier.isStatic(f.access) && f.desc.equals("Ljava/lang/String;") && callmap.containsKey(f)) {
							if (Modifier.isFinal(f.access)) {
								statfins++;
							} else {
								stats++;
							}

							String val = callmap.get(f);
							m.instructions.insert(fin, new LdcInsnNode(val));
							m.instructions.remove(fin);
						}
					}
				}
			}
		}

		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Running constant inliner.");
			System.err.flush();
			System.out.flush();
			System.out.printf("   Inlined %d static final String constants.%n", statfins);
			System.out.printf("   Inlined %d static String constants.%n", stats);
		}
	}

	private static ClassNode findStringClass(List<? extends ClassNode> classes) {
		for(ClassNode cn : classes) {
			if(!cn.superName.equals("java/lang/Object"))
				continue;

			int c = cn.cstCount("Ljava/lang/String;");
			if(c > 30) {
				return cn;
			}
		}

		return null;
	}

	private static Map<FieldNode, String> buildvalmap(ClassNode cn) {
		Map<FieldNode, String> map = new HashMap<FieldNode, String>();

		NodeVisitor nv = new NodeVisitor(){
			@Override
			public void visitField(FieldMemberNode fmn) {
				ConstantNode cn = fmn.firstConstant();
				if(cn != null && fmn.children() == 1) {
					Object cst = cn.cst();
					if(cst instanceof String) {
						FieldNode f = lookup(fmn.fin());
						if(f != null) {
							if(map.containsKey(f)) {
								map.put(f, null);
							} else {
								map.put(f, cst.toString());
							}
						}
					}
				}
			}
		};

		TreeBuilder tb = new TreeBuilder();
		for(MethodNode m : cn.methods) {
			if(m.name.equals("<clinit>")) {
				tb.build(m).accept(nv);
			}
		}
		
		return map;
	}

	public static FieldNode lookup(FieldInsnNode fin) {
		return lookup(fin.owner, fin.name, fin.desc);
	}

	public static FieldNode lookup(String owner, String name, String desc) {
		String halfKey = name + " " + desc;
		ClassTree tree = Context.current().getClassTree();
		ClassNode cn = tree.getClass(owner);

		if(cn == null)
			return null;

		FieldNode f = find(cn, halfKey);
		if(f != null)
			return f;

		for(ClassNode sup : tree.getSupers(cn)) {
			f = find(sup, halfKey);
			if(f != null)
				return f;
		}

		return null;
	}

	private static FieldNode find(ClassNode cn, String halfKey) {
		for(FieldNode f : cn.fields) {
			if(f.halfKey().equals(halfKey))
				return f;
		}

		return null;
	}
}