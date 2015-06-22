package org.nullbool.byeallatori;

import java.io.File;

public class Constants {
	public static String DATA_DIR;
	public static String BASE_DIR;
	public static String SCRIPTS_DIR;
	public static  String SITE_URL;
	public static  String AGENT;
	public static  String CALLBACK_CLASS;
	public static  String ACCESSOR_BASE;
	
	public static final int field_284 = 200;
	public static final boolean field_285 = false;
	public static final int field_287 = 409;
	public static final boolean field_289 = false;
	public static final int field_291 = 443;
	public static final int field_293 = 3;
	public static final int field_295 = 75;
	public static final int field_296 = 2;

	public static void method_2() {
		SITE_URL = "osbot.org";
		AGENT = "OSBot Comms";
		ACCESSOR_BASE = "org/osbot/rs07/accessor";
		CALLBACK_CLASS = "org/osbot/BotCallback";
		DATA_DIR = "./Data/";
		BASE_DIR = "";
		SCRIPTS_DIR = "./Scripts/";
		String var0;
		if((var0 = System.getProperty("user.home").replace(File.separator, "/")) == null) {
			var0 = "~";
		}

		BASE_DIR = (new StringBuilder()).insert(0, var0).append("/OSBot/").toString();
		File var1;
		if(!(var1 = new File(BASE_DIR)).exists()) {
			var1.mkdir();
		}

		DATA_DIR = (new StringBuilder()).insert(0, BASE_DIR).append("Data/").toString();
		SCRIPTS_DIR = (new StringBuilder()).insert(0, BASE_DIR).append("Scripts/").toString();
		if(!(var1 = new File(DATA_DIR)).exists()) {
			var1.mkdir();
		}

		if(!(var1 = new File(SCRIPTS_DIR)).exists()) {
			var1.mkdir();
		}

	}
}
