package org.nullbool.api.analysis;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public abstract interface IMethodAnalyser {

	public abstract List<MethodHook> find(ClassNode cn);
}