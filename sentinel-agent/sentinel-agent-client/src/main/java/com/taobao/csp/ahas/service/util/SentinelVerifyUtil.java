package com.taobao.csp.ahas.service.util;

public class SentinelVerifyUtil {
   public static final String SENTINEL_KEY = "ahas.sentinel";

   public static synchronized boolean sentinelHasInit() {
      try {
         Class var0 = Class.forName("com.taobao.csp.ahas.sentinel.DefaultSentinelSdkService");
      } catch (ClassNotFoundException var2) {
         return false;
      }

      String str = System.getProperty("ahas.sentinel");
      boolean inited = str != null && !str.isEmpty();
      if (!inited) {
         System.setProperty("ahas.sentinel", "true");
      }

      return inited;
   }
}
