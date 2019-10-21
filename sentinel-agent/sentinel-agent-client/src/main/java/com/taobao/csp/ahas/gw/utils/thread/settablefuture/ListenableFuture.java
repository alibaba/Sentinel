package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<V> extends Future<V> {
   void addListener(Runnable var1, Executor var2);
}
