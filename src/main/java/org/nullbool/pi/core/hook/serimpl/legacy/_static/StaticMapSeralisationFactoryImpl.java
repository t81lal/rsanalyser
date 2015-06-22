//package org.nullbool.pi.core.hook.serimpl.legacy._static;
//
//import org.nullbool.zbot.pi.core.hooks.api.HookMap;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapDeserialiser;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapSerialisationFactory;
//import org.nullbool.zbot.pi.core.hooks.api.serialisation.IMapSerialiser;
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