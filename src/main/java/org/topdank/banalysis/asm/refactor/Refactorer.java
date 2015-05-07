package org.topdank.banalysis.asm.refactor;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.topdank.banalysis.hooks.HookMap;
import org.topdank.byteengineer.commons.data.JarContents;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 6 Apr 2015 at 23:47:53 <br>
 */
public class Refactorer {

	private JarContents contents;
	private HookMap hooks;

	public Refactorer(JarContents contents, HookMap hooks) {
		this.contents = contents;
		this.hooks = hooks;
	}

	public HookMap getHooks() {
		return hooks;
	}

	public JarContents getContents() {
		return contents;
	}

	public void run() {
		if (getHooks() == null)
			return;
		if (getContents() == null)
			return;
		JarContents contents = getContents();
		RefactorMapper mapper = new RefactorMapper(contents.getClassContents().namedMap(), getHooks());
		Map<String, ClassNode> refactored = new HashMap<String, ClassNode>();
		ListIterator<ClassNode> it = contents.getClassContents().listIterator();
		while (it.hasNext()) {
			ClassNode cn = it.next();
			String oldName = cn.name;
			ClassReader cr = new ClassReader(getBytes(cn));
			ClassWriter cw = new ClassWriter(cr, 0);
			RemappingClassAdapter rca = new RemappingClassAdapter(cw, mapper);
			cr.accept(rca, ClassReader.EXPAND_FRAMES);
			cr = new ClassReader(cw.toByteArray());
			ClassNode cn1 = new ClassNode();
			cr.accept(cn1, 0);
			refactored.put(oldName, cn1);
			it.remove();
		}
		for (Map.Entry<String, ClassNode> factor : refactored.entrySet()) {
			contents.getClassContents().add(factor.getValue());
		}
	}

	protected byte[] getBytes(ClassNode cn) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		byte[] b = cw.toByteArray();
		return b;
	}
}
