package com.alibaba.csp.util.function;

public class Tuple2<R1, R2> {
   public final R1 r1;
   public final R2 r2;

   public Tuple2(R1 r1, R2 r2) {
      this.r1 = r1;
      this.r2 = r2;
   }

   public static <C1, C2> Tuple2<C1, C2> of(C1 c1, C2 c2) {
      return new Tuple2(c1, c2);
   }

   public Tuple2<R2, R1> swap() {
      return new Tuple2(this.r2, this.r1);
   }

   public String toString() {
      return "Tuple2{r1=" + this.r1 + ", r2=" + this.r2 + '}';
   }
}
