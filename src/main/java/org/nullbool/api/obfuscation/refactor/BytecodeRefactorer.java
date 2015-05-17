package org.nullbool.api.obfuscation.refactor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.obfuscation.refactor.test.RefactorTestClass;
import org.nullbool.api.util.map.NullPermeableMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
 
public class BytecodeRefactorer implements Opcodes{

	private final Collection<ClassNode> classes;
	private final IRemapper             remapper;
	private final ClassTree             classTree;
	private final InheritedMethodMap    methodChain;
	private final Map<String, String>   classMappings;
	private final Map<String, String>   fieldMappings;
	private final Map<String, String>   descMappings;
	private final Map<String, String>   methodMappings;

	public BytecodeRefactorer(Collection<ClassNode> classes, IRemapper remapper) {
		this.classes    = classes;
		this.remapper   = remapper;
		classTree       = new ClassTree(classes);
		methodChain     = new InheritedMethodMap(classTree);
		classMappings   = new HashMap<String, String>();
		fieldMappings   = new HashMap<String, String>();
		descMappings    = new HashMap<String, String>();
		methodMappings  = new HashMap<String, String>();
	}

	public void start() {
		startImpl();
	}

	private void startImpl() {
		
		int fieldNodes    = 0;
		int methodNodes   = 0;
		
		int fieldCalls    = 0;
		int methodCalls   = 0;
		
		//TypeInsnNode
		int newCalls      = 0;
		int newArray      = 0;
		int checkcasts    = 0;
		int instances     = 0;
		int mArrray       = 0;
		
		int classc        = 0;
		int iface         = 0;

	
		for (ClassNode cn : classes) {
			
			for(FieldNode fn : cn.fields) {
				fieldNodes++;
				fn.name = getMappedFieldName(fn);
				fn.desc = transformFieldDesc(fn.desc);
			}
			
			for(MethodNode mn : cn.methods) {
				methodNodes++;
				mn.name = getMappedMethodName(mn);
				mn.desc = transformMethodDesc(mn.desc);
				
				for(AbstractInsnNode ain : mn.instructions.toArray()) {
					if(ain instanceof FieldInsnNode) {
						fieldCalls++;
						
						FieldInsnNode fin = (FieldInsnNode) ain;
						
						String newOwner = getMappedClassName(fin.owner);
						String newName  = getMappedFieldName(fin.owner, fin.name, fin.desc);
						String newDesc  = transformFieldDesc(fin.desc);
						
						fin.owner = newOwner;
						fin.name  = newName;
						fin.desc  = newDesc;
					} else if(ain instanceof MethodInsnNode) {
						methodCalls++;
						
						MethodInsnNode min = (MethodInsnNode) ain;
						String newOwner = getMappedClassName(min.owner);
						String newName  = getMappedMethodName(min.owner, min.name, min.desc);
						String newDesc  = transformMethodDesc(min.desc);
						min.owner = newOwner;
						min.name  = newName;
						min.desc  = newDesc;
					} else if(ain instanceof TypeInsnNode) {
						TypeInsnNode tin = (TypeInsnNode) ain;
						
//						if(tin.opcode() == ANEWARRAY && tin.desc.contains("fb")) {
//							System.out.printf("opcode=%d, desc=%s.%n", tin.opcode(), tin.desc);
//						}
						
						if(tin.opcode() == NEW || tin.opcode() == ANEWARRAY) {
							if(tin.opcode() == NEW)
								newCalls++;
							else
								newArray++;
							
							String desc  = tin.desc;
							if(desc.startsWith("[") || desc.endsWith(";")) {
								tin.desc = transformFieldDesc(desc);
							} else {
								tin.desc = getMappedClassName(desc);
							}
						} else if(tin.opcode() == CHECKCAST || tin.opcode() == INSTANCEOF) {
							//ALOAD 1
							//CHECKCAST java/lang/Character
							//INVOKEVIRTUAL java/lang/Character.charValue ()C
							//Checkcasts are always object casts
							
							if(tin.opcode() == CHECKCAST)
								checkcasts++;
							else
								instances++;
							
							String desc  = tin.desc;
							if(desc.startsWith("[") || desc.endsWith(";")) {
								tin.desc = transformFieldDesc(desc);
							} else {
								tin.desc = getMappedClassName(desc);
							}
						}
					 } else if(ain instanceof MultiANewArrayInsnNode) {
						mArrray++;
						MultiANewArrayInsnNode main = (MultiANewArrayInsnNode) ain;
						main.desc = transformFieldDesc(main.desc);
					}
				}
			}
			
			classc++;
			
			cn.superName  = getMappedClassName(cn.superName);
			cn.name       = getMappedClassName(cn.name);
			
			List<String> oldInterfaces = cn.interfaces;
			List<String> newInterfaces = new ArrayList<String>();
			for(String oldIface : oldInterfaces) {
				iface++;
				newInterfaces.add(getMappedClassName(oldIface));
			}
			
			cn.interfaces = newInterfaces;
		}
		
		System.out.printf("Changed: %n");
		System.out.printf("   %d classes and %d interfaces. %n", classc, iface);
		System.out.printf("   %d fields  and %d field calls. %n", fieldNodes, fieldCalls);
		System.out.printf("   %d methods and %d method calls. %n", methodNodes, methodCalls);
		System.out.printf("   %d news, %d anewarrays, %d checkcasts, %d instancofs, %d mnewarrays. %n", newCalls, newArray, checkcasts, instances, mArrray);
	}
    
	public static void main1(String[] args) throws Exception {
		ClassReader cr = new ClassReader(RefactorTestClass.class.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		
		List<ClassNode> classes = new ArrayList<ClassNode>();
		classes.add(cn);
				
		IRemapper remapper = new IRemapper() {
			
			@Override
			public String resolveMethodName(String owner, String name, String desc) {
				return name;
			}
			
			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				return name;
			}
			
			@Override
			public String resolveClassName(String oldName) {
				return oldName;
			}
		};
		
		BytecodeRefactorer refactorer = new BytecodeRefactorer(classes, remapper);
		refactorer.start();
		
		System.out.println();
		System.out.println(ClassPrinter.print(cn));
//		
//		@SuppressWarnings("deprecation")
//		ClassLoader cl = new ClassLoader() {
//			{
//				ClassWriter cw = new ClassWriter(0);
//				cn.accept(cw);
//				byte[] b = cw.toByteArray();
//				defineClass(b, 0, b.length);
//			}
//		};
//		
//		Class<?> c = cl.loadClass(cn.name.replace("/", "."));
//		
//		Object o = c.newInstance();
//		call(c, o, "voidMethod", null);
//		call(c, o, "primitiveMethod", null);
//		call(c, o, "voidWithPrimitive", 1L);
//		call(c, o, "doubleParam", 1F);
//		call(c, o, "stringMethod", null);
//		call(c, o, "stringss", 1F);
//		call(c, o, "stirngsss", null);
//		call(c, o, "objjssss", null);
//		call(c, o, "intsss", null);
//		call(c, o, "castTest", new String("taslkdasd"));
//		call(c, o, "arrCast", new String[]{"asd", "fsdf"});
//		call(c, o, "intCastTest", 1);
//		call(c, o, "intsCastTest", new String[1][1]);
	}
	
//	private static void call(Class<?> c, Object o, String name, Object... args){
//		for(Method m : c.getDeclaredMethods()){
//			if(m.getName().equals(name)){
//				try {
//					System.out.println("Calling " + name);
//					m.invoke(o, args);
//				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//					System.out.println("err calling " + name +"  " + e.getMessage());
//				}
//			}
//		}
//	}
	
	public String transformMethodDesc(String desc){
		if(descMappings.containsKey(desc))
			return descMappings.get(desc);
		
		Type[] args = Type.getArgumentTypes(desc);
		Type ret    = Type.getReturnType(desc);
		
		StringBuilder sb = new StringBuilder("(");
		for(Type arg : args){
			sb.append(transformFieldDesc(arg.getDescriptor()));
		}
		sb.append(")");
		
		String retD = ret.getDescriptor();
		if(retD.equals("V"))
			sb.append("V");
		else
			sb.append(transformFieldDesc(ret.getDescriptor()));
		
		String newDesc = sb.toString();
		descMappings.put(desc, newDesc);
		
		return newDesc;
	}
	
	public String transformFieldDesc(String desc){
		String nonArrayDesc = desc.replace("[", "");
		
		if(isPrimitive(nonArrayDesc))
			return desc;
		
		if(descMappings.containsKey(desc))
			return descMappings.get(desc);
		
		//Type type = Type.getType(desc);
		//String oldClassName = type.getInternalName();
		//Remove the L and ; on the front and back of the desc
		
		int arraySize      = desc.length() - nonArrayDesc.length();
		nonArrayDesc       = nonArrayDesc.substring(1, nonArrayDesc.length() - 1);
		
		String newBaseDesc = String.format("L%s;", getMappedClassName(nonArrayDesc));
		
		String newDesc     = createArrayDescriptor(arraySize) + newBaseDesc;
		descMappings.put(desc, newDesc);
		
		return newDesc;
	}
	
	public static boolean isPrimitive(String desc) {
		switch(desc){
			case "I":
			case "B":
			case "S":
			case "J":
			case "D":
			case "F":
			case "Z":
			case "C":
				return true;
			default:
				return false;
		}
	}
	
	public static String createArrayDescriptor(int size) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < size; i++){
			sb.append("[");
		}
		return sb.toString();
	}

	public String getMappedClassName(String oldName) {
		if (classMappings.containsKey(oldName))
			return classMappings.get(oldName);
		String newName = remapper.resolveClassName(oldName);
		
		if(newName == null)
			newName = oldName;
		else
			newName = newName.replace(".", "/");
		
		classMappings.put(oldName, newName);
		return newName;
	}
	
	public String getMappedFieldName(FieldNode f){
		return getMappedFieldName(f.owner.name, f.name, f.desc);
	}
	
	public String getMappedFieldName(String owner, String name, String desc) {
		
		String fullKey = String.format("%s.%s %s", owner, name, desc);
		
		if(fieldMappings.containsKey(fullKey)){
			//if(fullKey.equals("gm.ec J")){
			//	System.out.println("BytecodeRefactorer.getMappedFieldName()");
			//}
			return fieldMappings.get(fullKey);
		}
		
		String newName = remapper.resolveFieldName(owner, name, desc);
		
		/* If the newName is null, it means that the remapper may not be doing deep mapping,
		 * ie. if we have a class gm which has a field ec and another class fe which extends
		 * gm, then accessing fe.ec may not be mapped, even though it is referring (albeit
		 * indirectly) to gm.ec.
		 * If this is the case, we should work downwards through the class hierarchy tree and
		 * poll the remapper for a new name.
		 *  eg. if we have the class gm with a field ec and we have another class fe which 
		 *      extends gm and another fu which extends fe, we look at fu.ec then fe.ec and 
		 *      then gm.ec for a new name.
		 */
		if(newName == null){
			ClassNode topKlass  = classTree.getClass(owner);
			
			if(topKlass != null){
				Set<ClassNode> tree = classTree.getSupers(topKlass);
				if(tree != null && tree.size() > 0){
					Iterator<ClassNode> it = tree.iterator();
					while(it.hasNext()){
						ClassNode next = it.next();
						if(next == null)
							break;
						
						newName = remapper.resolveFieldName(next.name, name, desc);
						if(newName != null)
							break;
					}
				}
			}
		}
		
		//If everything fails, we don't change the name.
		if(newName == null)
			newName = name;
		
		fieldMappings.put(fullKey, newName);
		return newName;
	}

	public String getMappedMethodName(String owner, String name, String desc){
		MethodNode m = findMethod(owner, name, desc);
		if(m == null)
			return name;
		return getMappedMethodName(m);
	}
	
	private MethodNode findMethod(String owner, String name, String desc){
		ClassNode cn = classTree.getClasses().get(owner);
		if(cn == null)
			return null;
		//throw new IllegalStateException(String.format("Class %s is not present in the cache. (%s.%s %s)", owner, owner, name, desc));
		String halfKey = name + "." + desc;
		for(MethodNode m : cn.methods){
			if(m.halfKey().equals(halfKey))
				return m;
		}
		return null;
	}
	
	public String getMappedMethodName(MethodNode m) {
		/*step 1. check already mapped ones*/
		String fullKey = m.key();
		if(methodMappings.containsKey(fullKey))
			return methodMappings.get(fullKey);
		
		/*step 2. check the tree to see if any of the ones in the
		 *        chain have already been changed (essentially
		 *        looking it up */
		
		/*If the method is static it won't be part of a chain
		 * since static methods can't be overridden.*/
		if(!Modifier.isStatic(m.access)) {
			ChainData cd = methodChain.getData(m);
			if(cd == null){
				System.err.println(m.key() +" is null " + Modifier.isStatic(m.access));
				System.exit(1);
			}
			for(MethodNode mn : cd.getAggregates()){
				if(!mn.name.equals(m.name)) {
					String newName = mn.name;
					methodMappings.put(fullKey, newName);
					return newName;
				}
			}
		}
		
		/*step 3. ask the remapper*/		
		String newName = remapper.resolveMethodName(m.owner.name, m.name, m.desc);
		methodMappings.put(fullKey, newName);
		return newName;
	}

	public static class ClassTree {
		private static final SetCreator<ClassNode> SET_CREATOR = new SetCreator<ClassNode>();

		private final Map<String, ClassNode>                      classes;
		private final NullPermeableMap<ClassNode, Set<ClassNode>> supers;
		private final NullPermeableMap<ClassNode, Set<ClassNode>> delgates;

		public ClassTree(Collection<ClassNode> classes) {
			this(convertToMap(classes));
		}

		public ClassTree(Map<String, ClassNode> classes_) {
			classes  = classes_;
			supers   = new NullPermeableMap<ClassNode, Set<ClassNode>>(SET_CREATOR);
			delgates = new NullPermeableMap<ClassNode, Set<ClassNode>>(SET_CREATOR);

			build(classes);
		}

		private static Map<String, ClassNode> convertToMap(Collection<ClassNode> classes) {
			Map<String, ClassNode> map = new HashMap<String, ClassNode>();
			for (ClassNode cn : classes) {
				map.put(cn.name, cn);
			}
			return map;
		}

		// TODO: optimise
		private void build(Map<String, ClassNode> classes) {
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

			if (classes.size() == delgates.size() && classes.size() == supers.size() && delgates.size() == supers.size()) {
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
				if(cs != null){
					getDelegates0(cs).add(current);
					buildSubTree(classes, superinterfaces, cs);
				}else{
					System.out.println("Null interface -> " + iface);
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
			return supers.getNotNull(cn);
		}

		private Set<ClassNode> getDelegates0(ClassNode cn) {
			return delgates.getNotNull(cn);
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

	public static class InheritedMethodMap {
		private final Map<MethodNode, ChainData> methods;

		public InheritedMethodMap(ClassTree tree) {
			methods = new HashMap<MethodNode, ChainData>();

			build(tree);
		}

		private void build(ClassTree tree) {
			int mCount = 0;
			int aCount = 0;
			for (ClassNode node : tree.getClasses().values()) {
				for (MethodNode m : node.methods) {
					if (!Modifier.isStatic(m.access)) {
						Set<MethodNode> supers    = tree.getMethodsFromSuper(node, m.name, m.desc);
						Set<MethodNode> delegates = tree.getMethodsFromDelegates(node, m.name, m.desc);
						ChainData data            = new ChainData(m, supers, delegates);
						this.methods.put(m, data);

						mCount ++;
						aCount += data.getAggregates().size();
					}
				}
			}

			System.out.println(String.format("Built map with %d methods connected with %d others.", mCount, aCount));
			
			//for(ChainData data : methods.values()){
			//	System.out.println(data);
			//}
		}

		public ChainData getData(MethodNode m) {
			return methods.get(m);
		}
	}

	public static class ChainData {
		private final MethodNode centre;
		private final Set<MethodNode> supers;
		private final Set<MethodNode> delegates;
		private final Set<MethodNode> aggregates;

		public ChainData(MethodNode m, Set<MethodNode> supers, Set<MethodNode> delegates) {
			this.centre    = m;
			this.supers    = supers;
			this.delegates = delegates;
			
			this.supers.remove(m);
			this.delegates.remove(m);
			
			aggregates     = new HashSet<MethodNode>();
			aggregates.addAll(supers);
			aggregates.addAll(delegates);
		}

		public Set<MethodNode> getSupers() {
			return supers;
		}

		public Set<MethodNode> getDelegates() {
			return delegates;
		}

		public Set<MethodNode> getAggregates() {
			return aggregates;
		}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Centre: ").append(centre.key()).append("   (").append(supers.size()).append(", ").append(delegates.size()).append(")");
			
			boolean sups = supers.size() > 0;
			boolean dels = delegates.size() > 0;
			if(sups || dels){
				sb.append("\n");
			}
			
			if(sups){
				sb.append("   >S>U>P>E>R>S>\n");
				Iterator<MethodNode> it = supers.iterator();
				while(it.hasNext()){
					MethodNode sup = it.next();
					sb.append("    ").append(sup.key());
					if(it.hasNext() || dels)
						sb.append("\n");
				}
			}
			
			if(dels){
				sb.append("   >D>E>L>E>G>A>T>E>S>\n");
				Iterator<MethodNode> it = delegates.iterator();
				while(it.hasNext()){
					MethodNode del = it.next();
					sb.append("    ").append(del.key());
					if(it.hasNext())
						sb.append("\n");
				}
			}
			
			return sb.toString();
		}
	}

	public static class SetCreator<T> implements ValueCreator<Set<T>> {

		@Override
		public Set<T> create() {
			return new HashSet<T>();
		}
	}
}