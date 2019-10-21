package com.alibaba.csp.util;

import java.util.concurrent.TimeUnit;

public final class TimeUtil {
   private static volatile long currentTimeMillis = System.currentTimeMillis();

   public static long currentTimeMillis() {
      return currentTimeMillis;
   }

   static {
      Thread daemon = new Thread(new Runnable() {
         public void run() {
            while(true) {
               TimeUtil.currentTimeMillis = System.currentTimeMillis();

               try {
                  TimeUnit.MILLISECONDS.sleep(1L);
               } catch (Throwable var2) {
               }
            }
         }
      });
      daemon.setDaemon(true);
      daemon.setName("sentinel-time-tick-thread");
      daemon.start();
   }
}
