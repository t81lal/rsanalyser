package org.nullbool.byeallatori;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BufferUtilities {


   public static int method_1(ByteBuffer var0) {
      int var1;
      if((var1 = var0.get() & 255) < 128) {
         return var1;
      } else {
         int var2 = var0.get() & 255;
         return (var1 << 8 | var2) - 32768;
      }
   }

   public static String method_2(ByteBuffer var0) {
      StringBuffer var1 = new StringBuffer();
      ByteBuffer var10000 = var0;

      int var2;
      while((var2 = var10000.get() & 255) != 0) {
         var10000 = var0;
         var1.append((char)var2);
      }

      return var1.toString();
   }

   public static void method_3(int var0, int var1, byte[] var2) {
      int var10002 = var1;
      int var10003 = var0 >> 8;
      ++var1;
      var2[var10002] = (byte)var10003;
      int var10001 = var1;
      byte var3 = (byte)var0;
      ++var1;
      var2[var10001] = var3;
   }

   public static int method_4(int var0, byte[] var1) {
      int var10000 = var1[var0] & 255;
      ++var0;
      var10000 <<= 24;
      int var10001 = var1[var0] & 255;
      ++var0;
      var10000 |= var10001 << 16;
      var10001 = var1[var0] & 255;
      ++var0;
      var10000 |= var10001 << 8;
      var10001 = var1[var0] & 255;
      ++var0;
      return var10000 | var10001;
   }

   public static int method_5(InputStream var0) throws IOException {
      int var1;
      if((var1 = var0.read()) == -1) {
         throw new IOException("EOF");
      } else if(var1 < 128) {
         return var1;
      } else {
         int var2 = var0.read();
         if(var1 == -1) {
            throw new IOException("EOF");
         } else {
            return var1 << 8 | var2 - 32768;
         }
      }
   }

   public static void writeShort(int val, OutputStream os) throws IOException {
      os.write(val >> 8);
      os.write(val);
   }

   public static int method_7(ByteBuffer var0) {
      int var1;
      return var0.get(var0.position()) < 0?var0.getInt() & Integer.MAX_VALUE:((var1 = var0.getShort() & '\uffff') == 32767?-1:var1);
   }

   public static void method_9(int var0, int var1, byte[] var2) {
      int var10001 = var1;
      byte var10002 = (byte)var0;
      ++var1;
      var2[var10001] = var10002;
   }

   public static void method_10(int var0, int var1, byte[] var2) {
      int var10004 = var1;
      int var10005 = var0 >> 24;
      ++var1;
      var2[var10004] = (byte)var10005;
      int var10003 = var1;
      var10004 = var0 >> 16;
      ++var1;
      var2[var10003] = (byte)var10004;
      int var10002 = var1;
      var10003 = var0 >> 8;
      ++var1;
      var2[var10002] = (byte)var10003;
      int var10001 = var1;
      byte var3 = (byte)var0;
      ++var1;
      var2[var10001] = var3;
   }

   public static void method_11(String var0, OutputStream var1) throws IOException {
      var1.write(var0.getBytes());
      var1.write(0);
   }

   public static String method_12(ByteBuffer var0) {
      StringBuffer var1 = new StringBuffer();
      ByteBuffer var10000 = var0;

      int var2;
      while((var2 = var10000.get() & 255) != 10) {
         var10000 = var0;
         var1.append((char)var2);
      }

      return var1.toString();
   }

   public static int method_13(ByteBuffer var0) {
      int var1 = 0;

      int var2;
      for(int var10000 = var2 = method_1(var0); var10000 == 32767; var2 = var10000) {
         var10000 = method_1(var0);
         var1 += 32767;
      }

      return var1 + var2;
   }

   public static int method_14(ByteBuffer var0) {
      int var1;
      if((var1 = (var0.get() & 255) << 8 + var0.get() & 255) > 32767) {
         var1 -= 65536;
      }

      return var1;
   }

   public static String method_15(InputStream var0) throws IOException {
      StringBuffer var1 = new StringBuffer();
      InputStream var10000 = var0;

      int var2;
      while((var2 = var10000.read()) != 10 && var2 != -1) {
         var10000 = var0;
         var1.append((char)var2);
      }

      return var1.toString();
   }

   public static String method_16(InputStream var0) throws IOException {
      StringBuffer var1 = new StringBuffer();
      InputStream var10000 = var0;

      int var2;
      while((var2 = var10000.read()) != 0 && var2 != -1) {
         var10000 = var0;
         var1.append((char)var2);
      }

      return var1.toString();
   }

   public static void writeSmart(int var0, int var1, byte[] var2) {
      if(var0 >= 0 && var0 < 128) {
         int var10001 = var1;
         byte var10002 = (byte)var0;
         ++var1;
         var2[var10001] = var10002;
      } else if(var0 >= 0 && var0 < 32768) {
         method_3(var0 + 32768, var1, var2);
      } else {
         throw new IllegalArgumentException((new StringBuilder()).insert(0, "Invalid smart value : ").append(var0).toString());
      }
   }

   public static void writeSmart(int val, OutputStream os) throws IOException {
      if(val >= 0 && val < 128) {
         os.write(val);
      } else if(val >= 0 && val < 32768) {
         writeShort(val + 32768, os);
      } else {
         throw new IllegalArgumentException((new StringBuilder()).insert(0, "Invalid smart value : ").append(val).toString());
      }
   }

   public static int method_19(ByteBuffer var0) {
      return (var0.get() & 255) << 16 | (var0.get() & 255) << 8 | var0.get() & 255;
   }
}
