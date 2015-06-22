package org.nullbool.pi.core.hook.api.serialisation;

import org.nullbool.pi.core.hook.api.HookMap;

public abstract interface IMapSerialisationFactory<M extends HookMap> {

	public static final String CONTENT_TYPE = "content-type";
	@Deprecated
	public static final String ACTIVITY = "activity";
	
	public abstract IMapSerialiser<M> createSerialiser();
	
	public abstract IMapDeserialiser<M> createDeserialiser();
}