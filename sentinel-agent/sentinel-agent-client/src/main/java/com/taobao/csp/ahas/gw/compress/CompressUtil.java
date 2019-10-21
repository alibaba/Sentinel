package com.taobao.csp.ahas.gw.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtil {
   public static byte[] compress(String str) {
      if (str != null && str.length() != 0) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         GZIPOutputStream gzip = null;
         byte[] content = null;

         try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("UTF-8"));
            gzip.finish();
            gzip.flush();
            gzip.close();
            content = out.toByteArray();
         } catch (Exception var13) {
            var13.printStackTrace();
         } finally {
            try {
               out.close();
            } catch (IOException var12) {
               var12.printStackTrace();
            }

         }

         return content;
      } else {
         return null;
      }
   }

   public static String uncompress(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ByteArrayInputStream in = new ByteArrayInputStream(bytes);
         String content = null;

         try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];

            int n;
            while((n = ungzip.read(buffer)) >= 0) {
               out.write(buffer, 0, n);
            }

            content = new String(out.toByteArray(), "UTF-8");
         } catch (Exception var15) {
            var15.printStackTrace();
         } finally {
            try {
               out.close();
               in.close();
            } catch (IOException var14) {
               var14.printStackTrace();
            }

         }

         return content;
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      String s = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
      System.out.println("字符串长度：" + s.length());
      System.out.println("压缩后长度：" + compress(s).length);
      System.out.println("解压后：" + uncompress(compress(s)) + ",长度" + uncompress(compress(s)).length());
   }
}
