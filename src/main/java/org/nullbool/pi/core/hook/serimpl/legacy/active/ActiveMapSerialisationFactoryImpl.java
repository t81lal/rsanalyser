//package org.nullbool.pi.core.hook.serimpl.legacy.active;
//
//import org.nullbool.zbot.pi.core.hooks.api.deprecated.ActiveHookMap;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapDeserialiser;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapSerialisationFactory;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapSerialiser;
//
//@Deprecated
//public class ActiveMapSerialisationFactoryImpl implements IMapSerialisationFactory<ActiveHookMap> {
//
//	@Override
//	public IMapSerialiser<ActiveHookMap> createSerialiser() {
//		return new ActiveMapSerialiserImpl();
//	}
//
//	@Override
//	public IMapDeserialiser<ActiveHookMap> createDeserialiser() {
//		return new ActiveMapDeserialiserImpl();
//	}
//}