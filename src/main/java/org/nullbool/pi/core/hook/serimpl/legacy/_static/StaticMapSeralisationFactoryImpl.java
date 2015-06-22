//package org.nullbool.pi.core.hook.serimpl.legacy._static;
//
//import org.nullbool.pi.core.hook.api.HookMap;
//import org.nullbool.pi.core.hook.api.serialisation.IMapDeserialiser;
//import org.nullbool.pi.core.hook.api.serialisation.IMapSerialisationFactory;
//import org.nullbool.pi.core.hook.api.serialisation.IMapSerialiser;
//
//public class StaticMapSeralisationFactoryImpl implements IMapSerialisationFactory<HookMap> {
//
//	@Override
//	public IMapSerialiser<HookMap> createSerialiser() {
//		return new StaticMapSerialiserImpl();
//	}
//
//	@Override
//	public IMapDeserialiser<HookMap> createDeserialiser() {
//		return new StaticMapDeserialiserImpl();
//	}
//}