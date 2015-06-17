package org.zbot.hooks.serialisation.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.DynamicDesc;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.ObfuscatedData;
import org.zbot.hooks.MethodHook.MethodType;
import org.zbot.hooks.serialisation.impl.MapDeserialiserImpl;
import org.zbot.hooks.serialisation.impl.MapSerialiserImpl;

public class Test {

	public static void main(String[] args) throws IOException {
//		HookMap map = new HookMap();
//		ClassHook c = new ClassHook("TestObf", "TestRefac");
//		FieldHook f = new FieldHook(c, new ObfuscatedData("a", "getField"), new DynamicDesc("I", false), false, 1);
//		c.getFields().add(f);
//		
//		map.getClasses().add(c);
//		
//		System.out.println(map.toString());
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		map.write(baos);
//		
//		byte[] bytes = baos.toByteArray();
//		
//		HookMap map2 = HookMap.read(new ByteArrayInputStream(bytes));
//		System.out.println(map2.toString());
//		
//		System.out.println(bytes.length);
		
		HookMap map = new HookMap();
		ClassHook s = new ClassHook("string", "java/lang/String");
		
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader("java/lang/String");
		cr.accept(cn, 0);
		
		System.out.println(cn.methods.size());
		for(MethodNode m : cn.methods) {
			MethodHook mh = new MethodHook(MethodType.CALLBACK, s, new ObfuscatedData("obf", "ref"), new DynamicDesc(m.desc, true), true, m.instructions);
			s.getMethods().add(mh);
		}
		
		System.out.println(s.getMethods().size());
		
		map.getClasses().add(s);
		
		//System.out.println(map.toString());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MapSerialiserImpl serialiser = new MapSerialiserImpl();
		serialiser.serialise(map, baos);
		
		byte[] bytes = baos.toByteArray();
		
		System.out.println(bytes.length);
		
		MapDeserialiserImpl deserialiser = new MapDeserialiserImpl();
		HookMap hm = deserialiser.deserialise(new ByteArrayInputStream(bytes));
		
		System.out.println(hm.getClasses().size());
		System.out.println(hm.getClasses().get(0).getMethods().size());
//		
//		HookMap map2 = HookMap.read(new ByteArrayInputStream(bytes));
//		
//		System.out.println(map2.getClasses().size());
		//System.out.println(map2.toString());
		
		//System.out.println(bytes.length);
	}
}