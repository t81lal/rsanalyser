package org.zbot.hooks.serialisation;

import java.io.IOException;
import java.io.InputStream;

import org.zbot.hooks.HookMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 16 Jun 2015 12:45:46
 */
public abstract interface IMapDeserialiser {

	public abstract HookMap deserialise(InputStream is) throws IOException;
}