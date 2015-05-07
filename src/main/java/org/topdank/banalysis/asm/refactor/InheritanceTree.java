package org.topdank.banalysis.asm.refactor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 6 Apr 2015 at 22:06:37 <br>
 */
public class InheritanceTree {

	private Map<String, ClassNode> classes;
	private Map<MethodData, MethodData> mappedMethods;

	public InheritanceTree(Collection<ClassNode> classes, MethodHook... mappedMethodHooks) {
		this(createMap(classes), mappedMethodHooks);
	}

	private static Map<String, ClassNode> createMap(Collection<ClassNode> classes) {
		Map<String, ClassNode> mapped = new HashMap<String, ClassNode>();
		for (ClassNode cn : classes) {
			mapped.put(cn.name, cn);
		}
		return mapped;
	}

	public InheritanceTree(Map<String, ClassNode> classes, MethodHook... mappedMethodHooks) {
		this.classes = classes;
		if ((classes == null) || (classes.size() == 0))
			throw new IllegalArgumentException("Expected at least some clases!");

		if (mappedMethodHooks == null)
			throw new IllegalArgumentException("Need a target method to rename for.");
		/* We can't find/rename stuff if we don't have the class! */
		if (isMissingClasses(mappedMethodHooks))
			throw new IllegalArgumentException("Target methods aren't in the classes (invalid target methods?).");
		mappedMethods = new HashMap<MethodData, MethodData>();
		/* We can't make any assumptions about the mappedMethod as it could be at any point in the
		 * inheritance tree so first we have to traverse backwards through the super classes and
		 * super interfaces and map the methods with the same name and desc as the one we want to
		 * change. Obviously it won't be static because static methods aren't inherited so we don't
		 * have to worry about that. After that we need to traverse forward finding all affected
		 * methods which involves the interfaces and classes that extend/implement at ANY POINT in
		 * the inheritance tree. Sigh... */
		fixAllMethods(mappedMethodHooks);
	}

	private boolean isMissingClasses(MethodHook[] hooks) {
		for (MethodHook hook : hooks) {
			if (!classes.containsKey(hook.getOwner().getObfuscated()))
				return true;
		}
		return false;
	}

	public MethodData getMethodData(String owner, String name, String desc) {
		return mappedMethods.get(new MethodData(owner, name, desc));
	}

	public void printData() {
		for (Entry<MethodData, MethodData> e : mappedMethods.entrySet()) {
			MethodData md1 = e.getKey();
			MethodData md2 = e.getValue();
			System.out.println(String.format("%s.%s %s -> %s.%s %s", md1.getOwner(), md1.getName(), md1.getDesc(), md2.getOwner(), md2.getName(), md2.getDesc()));
		}
	}

	private void fixAllMethods(MethodHook[] hooks) {
		/* There are probably hundreds of billions of optimisations we could do to improve
		 * performance but I don't think that it's necessary and I'd much rather than good readable
		 * code over mangled IOCC entries. */
		for (MethodHook hook : hooks) {
			fixMethods(hook);
		}
	}

	private void fixMethods(MethodHook hook) {
		Set<ClassNode> classSet = new HashSet<ClassNode>();
		/* Get all of the superclass/superinterfaces of the inital class (the one told to us by the
		 * constructor). */
		ClassNode initalClassNode = classes.get(hook.getOwner().getObfuscated());
		classSet.add(initalClassNode);
		classSet.addAll(traverseSupers(initalClassNode));

		/* Now we need to go through all of the classes that we have (as input from the constructor)
		 * and find which ones are subclasses of the classes in the classSet we have. Note that we
		 * have to make sure the class isn't already in the classSet (although this is why I used a
		 * Set rather than a list). */
		for (ClassNode cn : classes.values()) {
			if (isSubclass(classSet, cn)) {
				classSet.add(cn);
			}
		}

		/* Now we have all the classes which contain (I think, no, I hope) the target method so we
		 * need to go through them and map them. */
		for (ClassNode cn : classSet) {
			for (Object oM : cn.methods) {
				MethodNode m = (MethodNode) oM;
				if (shouldMap(m, hook)) {
					/* This methods data. */
					MethodData oldMethodData = new MethodData(cn.name, m.name, m.desc);
					/* This methods class name but replaced with the new methods name (and desc for
					 * future support/extra information). UPDATE: we can't create the new
					 * description as it requires more data. */
					MethodData newMethodData = new MethodData(cn.name, hook.getName().getRefactored(), m.desc);
					mappedMethods.put(oldMethodData, newMethodData);
				}
			}
		}
	}

	private boolean shouldMap(MethodNode m, MethodHook hook) {
		/* We can't inherit from static methods. */
		if ((m.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
			return false;
		}
		boolean valid = hook.getName().getObfuscated().equals(m.name);
		valid &= hook.getDesc().getObfuscated().equals(m.desc);
		return valid;
	}

	/**
	 * Checks if we the candidate ClassNode is a class that we should add to the classSet. Note that
	 * this means that the candidate isn't already in the Set(although this doesn't really matter
	 * because Sets don't contain duplicates by definition. After this we get all of the super
	 * classes and super interfaces and see if these two Sets overlap(intersect).
	 *
	 * @param classSet
	 * @param candidate
	 * @return
	 */
	private boolean isSubclass(Set<ClassNode> classSet, ClassNode candidate) {
		if (classSet.contains(candidate)) {
			return false;
		}

		Set<ClassNode> candidateSupers = traverseSupers(candidate);
		candidateSupers.remove(candidate);
		return overlaps(classSet, candidateSupers);
	}

	private boolean overlaps(Set<ClassNode> classSet1, Set<ClassNode> classSet2) {
		for (ClassNode cn : classSet1) {
			if (classSet2.contains(cn))
				return true;
		}
		return false;
	}

	private Set<ClassNode> traverseSupers(ClassNode cn) {
		Set<ClassNode> set = new HashSet<ClassNode>();
		set.add(cn);
		String superName = cn.superName;
		ClassNode superNode = classes.get(superName);
		if (shouldTraverse(superNode)) {
			/* recursively get the supers of the class */
			set.addAll(traverseSupers(superNode));
		}

		for (Object oInterfaceName : cn.interfaces) {
			String interfaceName = oInterfaceName.toString();// Essentially calling
																// String.toString() but since we
																// don't have compile generics and I
																// don't like compiler warnings this
																// is what we do.
			ClassNode interfaceNode = classes.get(interfaceName);
			if (shouldTraverse(interfaceNode)) {
				/* recursively get the supers of the class. */
				set.addAll(traverseSupers(interfaceNode));
			}
		}
		return set;
	}

	private boolean shouldTraverse(ClassNode cn) {
		if (cn == null)
			return false;
		/* We obviously can't modify anything in the standard library or even anything that we don't
		 * have loaded because we can't guarantee methods will be there (which could mess up the
		 * forward traversal). */
		if (cn.name.contains("java/"))
			return false;
		return true;
	}

	public static final class MethodData {
		private static final Map<Integer, MethodData> CACHE = new HashMap<Integer, MethodData>();

		public static MethodData create(String owner, String name, String desc) {
			int hashcode = hashCode(owner, name, desc);
			MethodData instance = CACHE.get(hashcode);
			if (instance != null)
				return instance;
			return new MethodData(owner, name, desc);
		}

		private final String owner;
		private final String name;
		private final String desc;

		private MethodData(String owner, String name, String desc) {
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}

		@Deprecated
		public boolean matches(MethodNode m) {
			return m.name.equals(name) && m.desc.equals(desc);
		}

		public String getOwner() {
			return owner;
		}

		public String getName() {
			return name;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public int hashCode() {
			return hashCode(owner, name, desc);
		}

		private static int hashCode(String owner, String name, String desc) {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((desc == null) ? 0 : desc.hashCode());
			result = (prime * result) + ((name == null) ? 0 : name.hashCode());
			result = (prime * result) + ((owner == null) ? 0 : owner.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodData other = (MethodData) obj;
			if (desc == null) {
				if (other.desc != null)
					return false;
			} else if (!desc.equals(other.desc))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (owner == null) {
				if (other.owner != null)
					return false;
			} else if (!owner.equals(other.owner))
				return false;
			return true;
		}
	}
}