package org.nullbool.api.analysis;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.zbot.hooks.FieldHook;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public abstract interface IFieldAnalyser {

	public abstract List<FieldHook> find(ClassNode cn);
}