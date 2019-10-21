package com.alibaba.csp.sentinel;

public final class ResourceTypeConstants {
   public static final int COMMON = 0;
   public static final int COMMON_WEB = 1;
   public static final int COMMON_RPC = 2;
   public static final int COMMON_API_GATEWAY = 3;
   public static final int COMMON_DB_SQL = 4;
   public static final int COMMON_CACHE = 5;

   private ResourceTypeConstants() {
   }
}
