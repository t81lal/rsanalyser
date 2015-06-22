package org.nullbool.pi.core.hook.api.serialisation;

import java.io.IOException;
import java.io.OutputStream;

import org.nullbool.pi.core.hook.api.HookMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 16 Jun 2015 12:11:50
 */
public abstract interface IMapSerialiser<M extends HookMap> {

	public abstract void serialise(M map, OutputStream os) throws IOException;
}