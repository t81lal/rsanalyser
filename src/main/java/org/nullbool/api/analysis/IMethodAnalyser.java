package org.nullbool.api.analysis;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.nullbool.pi.core.hook.api.MethodHook;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public abstract interface IMethodAnalyser {

	public abstract List<MethodHook> find(ClassNode cn);
}