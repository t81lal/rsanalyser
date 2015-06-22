/**
 * 
 */
package org.nullboolext;

import java.util.Arrays;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jun 2015 19:23:46
 */
public class process_builder_fixer {

	public static String[] fix(String[] args) {
//		"C:\Program Files\Java\jre1.8.0_25\bin\java.exe", , 
//		-Xbootclasspath/p:"C:\Users\Bibl\OSBot\Data\filter_8fb1be7.jar", 
//		-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005, -Xmx512m, 
//		-cp, "/C:/Users/Bibl/Desktop/osbots, shit, nigga/runnableosbout.jar";
//		"/C:/Users/Bibl/Desktop/osbots, shit, nigga/runnableosbout.jar", 
//		org.osbot.BotApplication, b]
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = args[0];
		newArgs[1] = "-noverify";
		for(int i=2; i < args.length + 1; i++) {
			newArgs[i] = args[i - 1];
		}
		
		System.out.println("Launching with " + Arrays.toString(newArgs));
		return newArgs;
	}
}