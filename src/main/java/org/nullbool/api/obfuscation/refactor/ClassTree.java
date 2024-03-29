package org.nullbool.api.obfuscation.refactor;

import static org.nullbool.api.obfuscation.refactor.ClassHelper.convertToMap;
import static org.nullbool.api.obfuscation.refactor.ClassHelper.copyOf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.Context;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public class ClassTree {
	private static final SetCreator<ClassNode> SET_CREATOR = new SetCreator<ClassNode>();

	private final Map<String, ClassNode>                      classes;
	private final NullPermeableHashMap<ClassNode, Set<ClassNode>> supers;
	private final NullPermeableHashMap<ClassNode, Set<ClassNode>> delgates;

	public ClassTree(Collection<ClassNode> classes) {
		this(convertToMap(classes));
	}

	public ClassTree(Map<String, ClassNode> classes_) {
		classes  = copyOf(classes_);
		supers   = new NullPermeableHashMap<ClassNode, Set<ClassNode>>(SET_CREATOR);
		delgates = new NullPermeableHashMap<ClassNode, Set<ClassNode>>(SET_CREATOR);

		build(classes);
	}

	// TODO: optimise
	public void build(Map<String, ClassNode> classes) {
		for (ClassNode node : classes.values()) {
			for (String iface : node.interfaces) {
				ClassNode ifacecs = classes.get(iface);
				if (ifacecs == null)
					continue;

				getDelegates0(ifacecs).add(node);

				Set<ClassNode> superinterfaces = new HashSet<ClassNode>();
				buildSubTree(classes, superinterfaces, ifacecs);

				getSupers0(node).addAll(superinterfaces);
			}
			ClassNode currentSuper = classes.get(node.superName);
			while (currentSuper != null) {
				getDelegates0(currentSuper).add(node);
				getSupers0(node).add(currentSuper);
				for (String iface : currentSuper.interfaces) {
					ClassNode ifacecs = classes.get(iface);
					if (ifacecs == null)
						continue;
					getDelegates0(ifacecs).add(currentSuper);
					Set<ClassNode> superinterfaces = new HashSet<ClassNode>();
					buildSubTree(classes, superinterfaces, ifacecs);
					getSupers0(currentSuper).addAll(superinterfaces);
					getSupers0(node).addAll(superinterfaces);
				}
				currentSuper = classes.get(currentSuper.superName);
			}

			getSupers0(node);
			getDelegates0(node);
		}
	}
	
	public void build(ClassNode node) {
		for (String iface : node.interfaces) {
			ClassNode ifacecs = classes.get(iface);
			if (ifacecs == null)
				continue;

			getDelegates0(ifacecs).add(node);

			Set<ClassNode> superinterfaces = new HashSet<ClassNode>();
			buildSubTree(classes, superinterfaces, ifacecs);

			getSupers0(node).addAll(superinterfaces);
		}
		ClassNode currentSuper = classes.get(node.superName);
		while (currentSuper != null) {
			getDelegates0(currentSuper).add(node);
			getSupers0(node).add(currentSuper);
			for (String iface : currentSuper.interfaces) {
				ClassNode ifacecs = classes.get(iface);
				if (ifacecs == null)
					continue;
				getDelegates0(ifacecs).add(currentSuper);
				Set<ClassNode> superinterfaces = new HashSet<ClassNode>();
				buildSubTree(classes, superinterfaces, ifacecs);
				getSupers0(currentSuper).addAll(superinterfaces);
				getSupers0(node).addAll(superinterfaces);
			}
			currentSuper = classes.get(currentSuper.superName);
		}

		getSupers0(node);
		getDelegates0(node);
		
		classes.put(node.name, node);
	}
	
	public void output() {
		if (classes.size() == delgates.size() && classes.size() == supers.size() && delgates.size() == supers.size()) {
			if(Context.current().getFlags().getOrDefault("basicout", true)) 
				System.out.println(String.format("Built tree for %d classes (%d del, %d sup).", classes.size(), delgates.size(), supers.size()));
		} else {
			System.out.println(String.format("WARNING: Built tree for %d classes (%d del, %d sup), may be erroneous.", classes.size(), delgates.size(),
					supers.size()));
		}
	}

	private void buildSubTree(Map<String, ClassNode> classes, Collection<ClassNode> superinterfaces, ClassNode current) {
		superinterfaces.add(current);
		for (String iface : current.interfaces) {
			ClassNode cs = classes.get(iface);
			if(cs != null) {
				getDelegates0(cs).add(current);
				buildSubTree(classes, superinterfaces, cs);
			} else {
				//System.out.println("Null interface -> " + iface);
			}
		}
	}

	public Set<MethodNode> getMethodsFromSuper(MethodNode m) {
		return getMethodsFromSuper(m.owner, m.name, m.desc);
	}

	public Set<MethodNode> getMethodsFromSuper(ClassNode node, String name, String desc) {
		Set<MethodNode> methods = new HashSet<MethodNode>();
		for (ClassNode super_ : getSupers(node)) {
			for (MethodNode mn : super_.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc)) {
					methods.add(mn);
				}
			}
		}
		return methods;
	}

	public Set<MethodNode> getMethodsFromDelegates(MethodNode m) {
		return getMethodsFromDelegates(m.owner, m.name, m.desc);
	}

	public Set<MethodNode> getMethodsFromDelegates(ClassNode node, String name, String desc) {
		Set<MethodNode> methods = new HashSet<MethodNode>();
		for (ClassNode delegate : getDelegates(node)) {
			for (MethodNode mn : delegate.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc)) {
					methods.add(mn);
				}
			}
		}
		return methods;
	}

	public MethodNode getFirstMethodFromSuper(ClassNode node, String name, String desc) {
		for (ClassNode super_ : getSupers(node)) {
			for (MethodNode mn : super_.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc)) {
					return mn;
				}
			}
		}
		return null;
	}

	public ClassNode getClass(String name) {
		return classes.get(name);
	}

	public boolean isInherited(ClassNode cn, String name, String desc) {
		return getFirstMethodFromSuper(cn, name, desc) != null;
	}

	public boolean isInherited(ClassNode first, MethodNode mn) {
		return mn.owner.name.equals(first.name) && isInherited(mn.owner, mn.name, mn.desc);
	}

	private Set<ClassNode> getSupers0(ClassNode cn) {
		return supers.getNonNull(cn);
	}

	private Set<ClassNode> getDelegates0(ClassNode cn) {
		return delgates.getNonNull(cn);
	}

	public Map<String, ClassNode> getClasses() {
		return classes;
	}

	public Set<ClassNode> getSupers(ClassNode cn) {
		return Collections.unmodifiableSet(supers.get(cn));
		// return supers.get(cn);
	}

	public Set<ClassNode> getDelegates(ClassNode cn) {
		return Collections.unmodifiableSet(delgates.get(cn));
		// return delgates.get(cn);
	}
}