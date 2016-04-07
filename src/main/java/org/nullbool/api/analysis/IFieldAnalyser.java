package org.nullbool.api.analysis;

import java.util.List;

import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.custom_asm.tree.ClassNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public interface IFieldAnalyser {

	List<FieldHook> findFields(ClassNode cn);
}