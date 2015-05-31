package org.nullbool.api.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.refactor.ClassTree;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class UnusedFieldRemover extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		System.err.println("Running Unused Field Remover.");
		
		@SuppressWarnings("unchecked")
		ClassTree tree = new ClassTree((Collection<ClassNode>) contents.getClassContents());
		Set<FieldNode> traced = new HashSet<FieldNode>();
		int untraceable = 0;
		
		boolean debug = Context.current().getFlags().getOrDefault("debug", false);

		
		for(ClassNode cn : tree.getClasses().values()) {
			for(MethodNode m : cn.methods) {
				for(AbstractInsnNode ain : m.instructions.toArray()) {
					if(ain.opcode() == GETSTATIC || ain.opcode() == PUTSTATIC) {
						FieldInsnNode fin = (FieldInsnNode) ain;
						FieldNode ref = findReference(tree, fin.owner, fin.halfKey());
						if(ref == null) {
							if(debug)
								System.err.printf("%s is null.%n", fin.key());
							
							untraceable++;
						} else {
							traced.add(ref);
						}
					}
				}
			}
		}
		
		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.out.printf("   Traced %d static field calls.%n", traced.size());
			System.out.printf("   Couldn't trace %d static field calls.%n", untraceable);
		}
		
		int removed = 0;
		
		boolean superDebug = Context.current().getFlags().getOrDefault("superDebug", false);
		
		for(ClassNode cn : tree.getClasses().values()) {
			Iterator<FieldNode> it = cn.fields.iterator();
			while(it.hasNext()) {
				FieldNode f = it.next();
				if(Modifier.isStatic(f.access)) {
					if(!traced.contains(f)) {
						if(superDebug)
							System.out.printf("Removing %s.%n", f.key());
						it.remove();
						removed++;
					}
				}
			}
		}
		
		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.out.printf("   Removed %d unused fields.%n", removed);
	}
	
	private static FieldNode findReference(ClassTree tree, String owner, String halfKey) {
		ClassNode startNode = tree.getClass(owner);
		if(startNode == null)
			return null;
		
		FieldNode field = findReference(startNode, halfKey);
		if(field != null) 
			return field;

		Set<ClassNode> supers = tree.getSupers(startNode);
		for(ClassNode cn : supers) {
			field = findReference(cn, halfKey);
			if(field != null)
				return field;
		}
		
		//isn't called
		return null;
	}
	
	private static FieldNode findReference(ClassNode cn, String halfKey) {
		for(FieldNode f : cn.fields) {
			if(f.halfKey().equals(halfKey))
				return f;
		}
		return null;
	}
}