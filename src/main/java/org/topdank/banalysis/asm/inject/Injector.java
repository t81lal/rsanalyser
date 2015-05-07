package org.topdank.banalysis.asm.inject;

import org.objectweb.asm.tree.ClassNode;
import org.topdank.banalysis.hooks.HookMap;

public abstract class Injector {

	protected ClassNode cn;
	protected HookMap hookMap;

	public Injector(HookMap hookMap) {
		this.hookMap = hookMap;
	}

	public abstract boolean shouldInject(ClassNode cn) throws Exception;

	public abstract void inject() throws Exception;

	public final void runInjector(ClassNode cn) throws InjectionException {
		try {
			if (shouldInject(cn)) {
				this.cn = cn;
				inject();
			}
		} catch (Exception e) {
			throw new InjectionException(e);
		}
	}
}