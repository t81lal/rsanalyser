package org.nullbool.byeallatori;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamHelper {
	
   public static byte[] isToByteArray(InputStream is) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      transfer8k(is, out);
      return out.toByteArray();
   }

   public static long transfer8k(InputStream is, OutputStream os) throws IOException {
      return transfer(is, os, 8024);
   }

   public static long transfer(InputStream is, OutputStream os, int len) throws IOException {
      byte[] buffer = new byte[len];
      
      long pos;
      int read;
      for(pos = 0L; -1 != (read = is.read(buffer)); pos += (long)read) {
         os.write(buffer, 0, read);
      }

      return pos;
   }
}
