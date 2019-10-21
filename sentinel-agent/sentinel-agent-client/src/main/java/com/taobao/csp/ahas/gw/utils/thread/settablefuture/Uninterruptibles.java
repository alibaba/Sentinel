package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Uninterruptibles {
   private Uninterruptibles() {
   }

   public static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
      boolean interrupted = false;

      try {
         while(true) {
            try {
               Object var2 = future.get();
               return (V)var2;
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }

      }
   }
}
