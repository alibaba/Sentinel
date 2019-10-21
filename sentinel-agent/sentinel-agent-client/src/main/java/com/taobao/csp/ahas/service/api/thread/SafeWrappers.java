package com.taobao.csp.ahas.service.api.thread;

public class SafeWrappers {
   public static Runnable safeRunnable(final Runnable runnable) {
      return new Runnable() {
         public void run() {
            try {
               runnable.run();
            } catch (Throwable var2) {
            }

         }
      };
   }
}
