package com.taobao.csp.ahas.gw.utils.thread;

public interface SettableFuture<V> extends ListenableFuture<V> {
   boolean set(V var1);

   boolean setException(Throwable var1);
}
