package org.nullbool.pi.core.hook.api.deprecated;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

@Deprecated
public abstract class AbstractActor {

	private final Map<String, String> variables = new HashMap<String, String>();
	
	public Map<String, String> variables() {
		return variables;
	}
	
	/**
	 * Implementation specific action.
	 * @param hooks
	 * @param classes
	 * @param client Whether the code is being ran on a live gamepack or for analysis.
	 */
	public abstract void act(ActiveHookMap hooks, Map<String, ClassNode> classes, boolean client);
}