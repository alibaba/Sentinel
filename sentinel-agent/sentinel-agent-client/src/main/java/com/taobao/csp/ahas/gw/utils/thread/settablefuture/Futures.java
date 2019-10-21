package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Futures {
   public static <V> V getDone(Future<V> future) throws ExecutionException {
      Preconditions.checkState(future.isDone(), "Future was expected to be done: %s", future);
      return Uninterruptibles.getUninterruptibly(future);
   }
}
