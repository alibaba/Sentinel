package com.taobao.csp.ahas.transport.api;

public class Tuple<L, R> {
   private L l;
   private R r;

   public Tuple(L l, R r) {
      this.l = l;
      this.r = r;
   }

   public L getL() {
      return this.l;
   }

   public void setL(L l) {
      this.l = l;
   }

   public R getR() {
      return this.r;
   }

   public void setR(R r) {
      this.r = r;
   }
}
