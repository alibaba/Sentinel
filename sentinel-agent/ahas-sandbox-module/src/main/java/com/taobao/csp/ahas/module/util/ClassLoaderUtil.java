package com.taobao.csp.ahas.module.util;

public class ClassLoaderUtil {
   public static void switchContextLoader(ClassLoader loader) {
      try {
         Thread.currentThread().setContextClassLoader(loader);
      } catch (SecurityException var2) {
      }

   }
}
