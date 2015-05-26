package org.nullbool.api.obfuscation.refactor;

/**
 * @author Bibl (don't ban me pls)
 * @created 25 May 2015 (actually before this)
 */
public abstract interface IRemapper {

	public abstract String resolveClassName(String oldName);

	public abstract String resolveFieldName(String owner, String name, String desc);

	public abstract String resolveMethodName(String owner, String name, String desc);
}