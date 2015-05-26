package org.nullbool.api.obfuscation.refactor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.Context;
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

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public class BytecodeRefactorer implements Opcodes {

	private final Collection<ClassNode> classes;
	private final IRemapper             remapper;
	private final ClassTree             classTree;
	private final MethodCache           methodCache;
	private final InheritedMethodMap    methodChain;
	private final Map<String, String>   classMappings;
	private final Map<String, String>   fieldMappings;
	private final Map<String, String>   descMappings;
	private final Map<String, String>   methodMappings;

	public BytecodeRefactorer(Collection<ClassNode> classes, IRemapper remapper) {
		this.classes    = classes;
		this.remapper   = remapper;
		classTree       = new ClassTree(classes);
		methodCache     = new MethodCache(classes);
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
		
		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.out.printf("Changed: %n");
			System.out.printf("   %d classes and %d interfaces. %n", classc, iface);
			System.out.printf("   %d fields  and %d field calls. %n", fieldNodes, fieldCalls);
			System.out.printf("   %d methods and %d method calls. %n", methodNodes, methodCalls);
			System.out.printf("   %d news, %d anewarrays, %d checkcasts, %d instancofs, %d mnewarrays. %n", newCalls, newArray, checkcasts, instances, mArrray);
		}

	}
	
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
		if(m == null){
//			System.out.printf("Can't find %s.%s %s.%n", owner, name, desc);
			return name;
		}
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

		/*Data is cached and recalled when needed because of a runtime issue.
		 *Take class K with a method M. When the method K.M is renamed, if we
		 *attempt to do a deep search to find the MethodNode it will fail, since
		 *the key K.M will actually be be looking for Kn.M where Kn is the new name
		 *of class K.
		 */
		/*
		return methodCache.get(owner, name, desc);
		*/
	}
	
	public String getMappedMethodName(MethodNode m) {
		/*step 1. check already mapped ones*/
		String fullKey = m.cachedKey();
		if(methodMappings.containsKey(fullKey))
			return methodMappings.get(fullKey);
		
		String newName = null;
		
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
					newName = mn.name;
					break;
//					methodMappings.put(fullKey, newName);
//					return newName;
				}
			}
		}
		
		/*step 3. ask the remapper*/		
		newName = remapper.resolveMethodName(m.owner.name, m.name, m.desc);
		methodMappings.put(fullKey, newName);
		
		return newName;
	}
}