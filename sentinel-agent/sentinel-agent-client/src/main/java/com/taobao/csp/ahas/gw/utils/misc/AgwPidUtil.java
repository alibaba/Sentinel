package com.taobao.csp.ahas.gw.utils.misc;

import java.lang.management.ManagementFactory;

public final class AgwPidUtil {
   public static int getPid() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      return Integer.parseInt(name.split("@")[0]);
   }

   private AgwPidUtil() {
   }
}
