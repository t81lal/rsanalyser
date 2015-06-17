package org.zbot.hooks.serialisation;

import java.io.IOException;
import java.io.OutputStream;

import org.zbot.hooks.HookMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 16 Jun 2015 12:11:50
 */
public abstract interface IMapSerialiser {

	public abstract void serialise(HookMap map, OutputStream os) throws IOException;
}