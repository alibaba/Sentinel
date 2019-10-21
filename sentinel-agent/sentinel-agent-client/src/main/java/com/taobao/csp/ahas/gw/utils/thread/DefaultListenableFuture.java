package com.taobao.csp.ahas.gw.utils.thread;

import com.taobao.csp.ahas.gw.utils.thread.settablefuture.AbstractFuture;

import java.util.concurrent.Executor;

public class DefaultListenableFuture<V> extends AbstractFuture<V> implements SettableFuture<V> {
   private final Executor executor;

   public DefaultListenableFuture(Executor executor) {
      this.executor = executor;
   }

   public void addListener(Runnable listener) {
      super.addListener(listener, this.executor);
   }

   public boolean set(V value) {
      boolean result = super.set(value);
      if (!result) {
         throw new IllegalStateException("weird! failed to set result");
      } else {
         return result;
      }
   }

   public boolean setException(Throwable throwable) {
      return super.setException(throwable);
   }
}
