package org.nullbool.zbot.pi.core.hooks.api.serialisation;

import org.nullbool.zbot.pi.core.hooks.api.HookMap;

public abstract interface IMapSerialisationFactory<M extends HookMap> {

	public static final String CONTENT_TYPE = "content-type";
	@Deprecated
	public static final String ACTIVITY = "activity";
	
	public abstract IMapSerialiser<M> createSerialiser();
	
	public abstract IMapDeserialiser<M> createDeserialiser();
}