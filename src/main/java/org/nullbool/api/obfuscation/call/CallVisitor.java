package org.nullbool.api.obfuscation.call;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Context;
import org.nullbool.api.obfuscation.Visitor;
import org.nullbool.api.util.ClassStructure;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class CallVisitor extends Visitor {

	private int used = 0;

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		int total = 0;
		@SuppressWarnings("unchecked")
		Map<String, ClassStructure> classesMap = (Map<String, ClassStructure>) contents.getClassContents().namedMap();
		for (ClassNode cs : classesMap.values())
			total += cs.methods.size();
		if(Context.current().getFlags().getOrDefault("basicout", true)) {
			System.err.println("Dummy method remover");
			System.out.printf("   %d  total methods.%n", total);
		}
		List<MethodNode> entries = new ArrayList<>();
		for (ClassStructure cs : classesMap.values()) {
			entries.addAll(cs.getMethods(m -> m.name.length() > 2)); // need to do this to check methods inherited from jdk
			entries.addAll(cs.getMethods(cs::isInherited)); // inherited methods from within the client
		}
		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.out.printf("   %d prospect methods.%n", entries.size());
		
		entries.forEach(e -> search(classesMap, e));
		classesMap.values().forEach(cs -> cs.getMethods(mn -> !cs.callGraph.containsVertex(mn)).forEach(cs.methods::remove));
		
		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.out.printf("   Found %d/%d used methods (removed %d dummy methods).%n", used, total, total - used);
	}

	private void search(Map<String, ClassStructure> tree, MethodNode vertex) {
		if (tree.get(vertex.owner).callGraph.containsVertex(vertex))
			return;
		tree.get(vertex.owner).callGraph.addVertex(vertex);
		used++;
		aParnyLoop: for (AbstractInsnNode ain : vertex.instructions.toArray()) {
			if (ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (tree.containsKey(min.owner)) {
					ClassStructure cs = tree.get(min.owner);
					MethodNode edge = cs.getMethod(min.name, min.desc);
					if (edge != null) {
						tree.get(vertex.owner).callGraph.addEdge(vertex, edge); // method is called, graph it
						search(tree, edge); // search outgoing calls from that method
						continue;
					}
					for (ClassStructure supertype : cs.supers) { // do the same for all supertypes and superinterfaces
						MethodNode superedge = supertype.getMethod(min.name, min.desc);
						if (superedge != null) {
							tree.get(vertex.owner).callGraph.addEdge(vertex, superedge);
							search(tree, superedge);
							continue aParnyLoop;
						}
					}
					/* for (final ClassStructure delegate : cs.delegates) { //trace downwards to verify
					 * 
					 * } */
				}
			}
		}
	}
}