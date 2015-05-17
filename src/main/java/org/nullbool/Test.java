package org.nullbool;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.obfuscation.refactor.BytecodeRefactorer;
import org.nullbool.api.obfuscation.refactor.IRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.topdank.banalysis.util.ClassUtil;
import org.topdank.byteengineer.commons.data.JarContents;
import org.topdank.byteengineer.commons.data.JarInfo;
import org.topdank.byteio.in.SingleJarDownloader;
import org.topdank.byteio.out.CompleteJarDumper;

public class Test {

	public static void main(String[] args) throws IOException {
		SingleJarDownloader<ClassNode> dl = new SingleJarDownloader<ClassNode>(new JarInfo(new File("C:/Users/Bibl/Desktop/KiLO.jar")));
		dl.download();
		
		JarContents<ClassNode> contents = dl.getJarContents();
		System.out.printf("Loaded %d classes.%n", contents.getClassContents().size());
		System.out.printf("Loaded %d resources.%n", contents.getResourceContents().size());
		
		Map<String, ClassNode> classes = contents.getClassContents().namedMap();
		AtomicInteger fieldCount = new AtomicInteger();		
		AtomicInteger methodCount = new AtomicInteger();
		AtomicInteger classCount = new AtomicInteger();
		BytecodeRefactorer refactorer = new BytecodeRefactorer(contents.getClassContents(), new IRemapper() {
			
			@Override
			public String resolveMethodName(String owner, String name, String desc) {
				if(!isChangeable(classes, owner))
					return name;
				
				if(!name.equals("<init>") && !name.equals("<clinit>"))
					return "method_" + (methodCount.getAndIncrement()) + "_" + name;
				return name;
			}
			
			@Override
			public String resolveFieldName(String owner, String name, String desc) {
				if(!isChangeable(classes, owner))
					return name;
				
				return "field_" + (fieldCount.getAndIncrement()) + "_" + name;
			}
			
			@Override
			public String resolveClassName(String oldName) {
				if(!isChangeable(classes, oldName))
					return oldName;
				return "class_" + (classCount.getAndIncrement()) + "_" + ClassUtil.getClassName(oldName);
			}
		});
		
		refactorer.start();
		
		new CompleteJarDumper(contents).dump(new File("C:/Users/Bibl/Desktop/kiloout.jar"));
	}
	
	private static boolean isChangeable(Map<String, ClassNode> classes, String cname){
		return classes.containsKey(cname);
	}
}