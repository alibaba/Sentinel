package com.taobao.csp.ahas.gw.utils.thread;

import com.taobao.csp.ahas.gw.utils.thread.settablefuture.MoreExecutors;

import java.util.concurrent.Executor;

public class Futures {
   public static <T> DefaultListenableFuture<T> createSettableFuture() {
      return new DefaultListenableFuture(MoreExecutors.directExecutor());
   }

   public static <T> DefaultListenableFuture<T> createSettableFuture(Executor executor) {
      return new DefaultListenableFuture(executor);
   }
}
