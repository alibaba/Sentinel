package com.taobao.csp.ahas.gw.io.protocol.util;

import java.util.concurrent.atomic.AtomicLong;

public class UUIDGenerator {
   private static AtomicLong opaque = new AtomicLong();

   public static long next() {
      return opaque.getAndIncrement();
   }
}
