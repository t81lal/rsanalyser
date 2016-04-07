package org.nullbool.api.analysis;

import java.util.List;

import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.custom_asm.tree.ClassNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 23 Jul 2015 16:55:20
 */
public abstract interface IMultiAnalyser {

	public abstract List<ObfuscatedData> findAny(ClassNode cn);
}