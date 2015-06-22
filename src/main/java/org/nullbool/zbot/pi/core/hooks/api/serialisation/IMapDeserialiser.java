package org.nullbool.zbot.pi.core.hooks.api.serialisation;

import java.io.IOException;
import java.io.InputStream;

import org.nullbool.zbot.pi.core.hooks.api.HookMap;

/**
 * @author Bibl (don't ban me pls)
 * @created 16 Jun 2015 12:45:46
 */
public abstract interface IMapDeserialiser<M extends HookMap> {

	public abstract M deserialise(InputStream is) throws IOException;
}