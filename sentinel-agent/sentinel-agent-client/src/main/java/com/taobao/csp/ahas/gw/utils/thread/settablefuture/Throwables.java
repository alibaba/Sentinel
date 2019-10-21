package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

public final class Throwables {
   private Throwables() {
   }

   public static void throwIfUnchecked(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      } else if (throwable instanceof Error) {
         throw (Error)throwable;
      }
   }
}
