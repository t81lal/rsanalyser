//package org.nullbool.pi.core.hook.serimpl.legacy.active;
//
//import org.nullbool.pi.core.hook.api.deprecated.ActiveHookMap;
//import org.nullbool.pi.core.hook.api.serialisation.IMapDeserialiser;
//import org.nullbool.pi.core.hook.api.serialisation.IMapSerialisationFactory;
//import org.nullbool.pi.core.hook.api.serialisation.IMapSerialiser;
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