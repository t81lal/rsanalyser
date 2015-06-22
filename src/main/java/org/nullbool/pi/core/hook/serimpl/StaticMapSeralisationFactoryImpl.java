package org.nullbool.pi.core.hook.serimpl;

import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.serialisation.IMapDeserialiser;
import org.nullbool.pi.core.hook.api.serialisation.IMapSerialisationFactory;
import org.nullbool.pi.core.hook.api.serialisation.IMapSerialiser;

/**
 * @author Bibl (don't ban me pls)
 * @created 19 Jun 2015 23:05:12
 */
public class StaticMapSeralisationFactoryImpl implements IMapSerialisationFactory<HookMap> {

	@Override
	public IMapSerialiser<HookMap> createSerialiser() {
		return new StaticMapSerialiserImpl();
	}

	@Override
	public IMapDeserialiser<HookMap> createDeserialiser() {
		return new StaticMapDeserialiserImpl();
	}
}