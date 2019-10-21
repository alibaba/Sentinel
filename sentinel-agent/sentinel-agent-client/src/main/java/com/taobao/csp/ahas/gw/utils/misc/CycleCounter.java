package com.taobao.csp.ahas.gw.utils.misc;

import java.util.concurrent.atomic.AtomicInteger;

public class CycleCounter {
   private AtomicInteger num = new AtomicInteger(0);
   private static final int RESET_NUM_THREADSHOLD = 10000000;
   private int cycleSize;

   public CycleCounter(int cycleSize) {
      this.cycleSize = cycleSize;
   }

   public boolean aRound() {
      int n = this.num.addAndGet(1);
      if (n > 10000000) {
         this.num.set(0);
         return false;
      } else {
         return n % this.cycleSize == 0;
      }
   }
}
