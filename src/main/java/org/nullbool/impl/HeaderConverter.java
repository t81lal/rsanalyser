package org.nullbool.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Bibl (don't ban me pls)
 * @created 9 Jul 2015 17:11:57
 */
public class HeaderConverter {

	private static String reduce(String name, int prefix) {
		if(name.length() == prefix) {
			return name;
		}
		
		int index = name.indexOf('&');
		if(index != -1) {
			String sub = name.substring(0, index);
			if(sub.length() == prefix) {
				return name;
			}
		}
		
		name = name.substring(prefix);
		char first = name.charAt(0);
		char upper = Character.toLowerCase(first);
		name = name.substring(1);
		name = upper + name;
		return name;
	}
	
	private static String fix(String name) {
		if(name.startsWith("get")) {
			name = reduce(name, 3);
		} else if(name.startsWith("is")) {
			name = reduce(name, 2);
		}
		
		return name;
	}
	
	public static void main(String[] args) throws Exception {
		
		File src = new File("C:/Users/Bibl/Desktop/zbot git/rsanalyser/src/main/"
				+ "java/org/nullbool/impl/analysers/friend/IgnoredPlayerAnalyser.java");
//		ClassAnalyser analyser = new WidgetAnalyser();
//		
//		Map<String, String> mapped = new HashMap<String, String>();
//		StringBuilder mainSb = new StringBuilder();
//		mainSb.append("@SupportedHooks(");
//		mainSb.append('\n').append('\t');
//		mainSb.append("fields = {");
//		
//		for(String s : analyser.supportedFields()) {
//			String[] parts = s.split("&");
//			String name = parts[0];
//			name = fix(name);
//			mainSb.append('"');
//			mainSb.append(name).append("&").append(parts[1]);
//			mainSb.append('"').append(", ");
//			
//			mapped.put(parts[0], name);
//		}
//
//		mainSb.append("},");
//		mainSb.append('\n');
//
//		mainSb.append('\t');
//		mainSb.append("methods = {");
//		
//		for(String s : analyser.supportedMethods()) {
//			String[] parts = s.split("&");
//			mainSb.append('"');
//			mainSb.append(parts[0]).append("&").append(parts[1]);
//			mainSb.append('"').append(", ");
//		}
//		
//		mainSb.append("}");
//		mainSb.append('\n');
//
//		mainSb.append(")");
//		mainSb.append('\n');
//		
//		System.out.println(mainSb);
		
		
		BufferedReader br = new BufferedReader(new FileReader(src));
		String line;
		while((line = br.readLine()) != null) {
			char[] chars = line.toCharArray();
			
			Map<String, String> fixes = new HashMap<String, String>();
			StringBuilder tmp = new StringBuilder();
			boolean reading = false;
			
			for(int i=0; i < chars.length; i++) {
				char c = chars[i];
				if(reading) {
					if(c == '"') {
						char prev = chars[i - 1];
						if(prev != '\\') {
							String name = tmp.toString();
							String fix = fix(name);
							
							fixes.put(name, fix);
							
							reading = false;
							tmp.setLength(0);
							continue;
						}
					}
					tmp.append(c);
				} else {
					if(c == '"') {
						reading = true;
					}
				}
			}
			
			for(Entry<String, String> e : fixes.entrySet()) {
				line = line.replace(e.getKey(), e.getValue());
			}
			
			System.out.println(line);
		}
		
//		ListIterator<FieldHook> fit = fields.listIterator();
//		while(fit.hasNext()) {
//			FieldHook fh = fit.next();
//			mainSb.append('"');
//			mainSb.append(fh.refactored()).append("&");
//			String desc = fh.val(Constants.DESC);
//			String stripped = desc.replace("[", "");
//			if(DynamicDesc.isPrimitive(stripped)) {
//				mainSb.append(desc);
//			} else {
//				desc = DynamicDesc.convertSingleJavaStyle(classes, desc);
//				mainSb.append(desc);
//			}
//			mainSb.append('"');
//
//			if(fit.hasNext()) {
//				mainSb.append(", ");
//			}
//		}
//
//		mainSb.append("},");
//		mainSb.append('\n');
//
//		mainSb.append('\t');
//		mainSb.append("methods = {");
//		ListIterator<MethodHook> mit = ch.methods().listIterator();
//		while(mit.hasNext()) {
//			MethodHook fh = mit.next();
//			mainSb.append('"');
//			mainSb.append(fh.refactored()).append("&");
//			String desc = fh.val(Constants.DESC);
//			// if(DynamicDesc.isPrimitive(stripped)) {
//			// 	mainSb.append(desc);
//			// } else {
//			//	desc = DynamicDesc.convertSingleJavaStyle(classes, desc);
//			//	mainSb.append(desc);
//			//}
//			mainSb.append(DynamicDesc.convertMultiJavaStyle(classes, desc));
//			mainSb.append('"');
//
//			if(mit.hasNext()) {
//				mainSb.append(", ");
//			}
//		}
//		mainSb.append("}");
//		mainSb.append('\n');
//
//		mainSb.append(")");
//		mainSb.append('\n');
	}
}