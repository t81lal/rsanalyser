package org.nullbool.byeallatori;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class MiscHelper {
	
    public static String md5_file( File file) {
    	
    	/// C:/Users/Bibl/Desktop/osbots shit nigga/test.jar
    	file = new File("/C:/Users/Bibl/Desktop/osbots shit nigga/osbclean.jar");
        try {
            final MessageDigest instance = MessageDigest.getInstance("MD5");
            final FileInputStream fileInputStream = new FileInputStream(file);
            final DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, instance);
            final byte[] array = new byte[8192];
            int read;
            while ((read = fileInputStream.read(array)) > 0) {
                instance.update(array, 0, read);
            }
            digestInputStream.close();
            final MessageDigest messageDigest = instance;
            fileInputStream.close();
            return new BigInteger(1, messageDigest.digest()).toString(16);
        }
        catch (NoSuchAlgorithmException ex2) {
        	ex2.printStackTrace();
        }
        catch (IOException ex) {
        	ex.printStackTrace();
            if (ex instanceof FileNotFoundException && ex.getMessage().contains("(Is a directory)")) {}
        }
        return null;
    }

    public static String add() {
        StringBuilder sb = new StringBuilder();
        String jarmd5;
        if((jarmd5 = jarmd5()) != null) {
           sb.append("jh: ").append(jarmd5);
           
           Iterator<String> it = ManagementFactory.getRuntimeMXBean().getInputArguments().iterator();
           while(it.hasNext()) {
              String arg = (String)it.next();
              if(arg.contains("-Xbootclasspath")) {
            	  
                 String legitxboot = "-Xbootclasspath/p:";
                 if(!arg.contains(legitxboot) || !arg.contains("filter_")) {
                    arg = arg.substring(legitxboot.length());
                    sb.append("\n").append("User trying to xboot file ").append(arg);
                 }
                 
              }
           }
        }

        return sb.toString();
     }


    public static boolean vm_has_args() {
        Iterator<String> iterator = ManagementFactory.getRuntimeMXBean().getInputArguments().iterator();
        do {
            if (!iterator.hasNext()) return false;
        } while (!iterator.next().contains((CharSequence)"-Xbootclasspath"));
        return true;
    }

    public MiscHelper() {
    }

    public String running_file() {
        try {
        	// launcher dir?
            String string = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            if (string != null) return URLDecoder.decode(string, "UTF-8");
            return null;
        }
        catch (UnsupportedEncodingException var1_2) {
        	var1_2.printStackTrace();
            return null;
        }
    }

    public static String md5_file(String file) {
        return MiscHelper.md5_file(new File(file));
    }

    public static String jarmd5() {
        try {
            MiscHelper helper = new MiscHelper();
            return MiscHelper.md5_file(helper.running_file());
        }
        catch (Exception var0_1) {
            return null;
        }
    }
}
