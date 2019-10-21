package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

import java.util.concurrent.Executor;

public final class MoreExecutors {
   private MoreExecutors() {
   }

   public static Executor directExecutor() {
      return DirectExecutor.INSTANCE;
   }

   private static enum DirectExecutor implements Executor {
      INSTANCE;

      public void execute(Runnable command) {
         command.run();
      }

      public String toString() {
         return "MoreExecutors.directExecutor()";
      }
   }
}
