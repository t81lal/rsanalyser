package org.zbot.hooks.serialisation.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.zbot.hooks.ClassHook;
import org.zbot.hooks.DynamicDesc;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.ObfuscatedData;

public class Test {

	public static void main(String[] args) throws IOException {
		HookMap map = new HookMap();
		ClassHook c = new ClassHook("TestObf", "TestRefac");
		FieldHook f = new FieldHook(c, new ObfuscatedData("a", "getField"), new DynamicDesc("I", false), false, 1);
		c.getFields().add(f);
		
		map.getClasses().add(c);
		
		System.out.println(map.toString());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		map.write(baos);
		
		byte[] bytes = baos.toByteArray();
		
		HookMap map2 = HookMap.read(new ByteArrayInputStream(bytes));
		System.out.println(map2.toString());
		
		System.out.println(bytes.length);
		
	}
}