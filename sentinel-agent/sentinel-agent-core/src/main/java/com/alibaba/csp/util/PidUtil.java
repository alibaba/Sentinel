package com.alibaba.csp.util;

import java.lang.management.ManagementFactory;

public final class PidUtil {
   public static int getPid() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      return Integer.parseInt(name.split("@")[0]);
   }

   private PidUtil() {
   }
}
