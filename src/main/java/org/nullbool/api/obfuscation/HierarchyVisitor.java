package org.nullbool.api.obfuscation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.nullbool.api.Context;
import org.nullbool.api.util.ClassStructure;
import org.objectweb.custom_asm.tree.ClassNode;
import org.topdank.byteengineer.commons.data.JarContents;

public class HierarchyVisitor extends Visitor {

	@Override
	public void visit(JarContents<? extends ClassNode> contents) {
		@SuppressWarnings("unchecked")
		Map<String, ClassStructure> classes = (Map<String, ClassStructure>) contents.getClassContents().namedMap();
		for (final ClassStructure node : classes.values()) {
			for (final String iface : node.interfaces) {
				final ClassStructure ifacecs = classes.get(iface);
				if (ifacecs == null)
					continue;
				ifacecs.delegates.add(node);
				final Collection<ClassStructure> superinterfaces = new ArrayList<>();
				visitImpl(classes, superinterfaces, ifacecs);
				node.supers.addAll(superinterfaces);
			}
			ClassStructure currentSuper = classes.get(node.superName);
			while (currentSuper != null) {
				currentSuper.delegates.add(node);
				node.supers.add(currentSuper);
				for (final String iface : currentSuper.interfaces) {
					final ClassStructure ifacecs = classes.get(iface);
					if (ifacecs == null)
						continue;
					ifacecs.delegates.add(currentSuper);
					final Collection<ClassStructure> superinterfaces = new ArrayList<>();
					visitImpl(classes, superinterfaces, ifacecs);
					currentSuper.supers.addAll(superinterfaces);
					node.supers.addAll(superinterfaces);
				}
				currentSuper = classes.get(currentSuper.superName);
			}
		}
		
		if(Context.current().getFlags().getOrDefault("basicout", true))
			System.err.printf("Successfully built hierarchy tree for %s classes!%n", classes.size());
	}

	private void visitImpl(final Map<String, ClassStructure> classes, final Collection<ClassStructure> superinterfaces, final ClassStructure current) {
		superinterfaces.add(current);
		for (final String iface : current.interfaces) {
			final ClassStructure cs = classes.get(iface);
			if (cs != null) { 
				cs.delegates.add(current);
				visitImpl(classes, superinterfaces, cs);
			}
		}
	}
}