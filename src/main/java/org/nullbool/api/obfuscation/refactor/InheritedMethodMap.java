package org.nullbool.api.obfuscation.refactor;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.Context;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class InheritedMethodMap {
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

		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.out.println(String.format("Built map with %d methods connected with %d others.", mCount, aCount));
		
		//for(ChainData data : methods.values()){
		//	System.out.println(data);
		//}
	}

	public ChainData getData(MethodNode m) {
		return methods.get(m);
	}
}