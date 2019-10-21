package com.taobao.csp.ahas.gw.client.api.bootstrap;

public class BaseAgwConfig<B extends BaseAgwConfig> {
   public static final int MIN_RPC_TIMEOUT_MS = 500;
   public static final int DEFAULT_RPC_TIMEOUT_MS = 4000;
   private int optionalTimeoutMs = 4000;

   public int getOptionalTimeoutMs() {
      return this.optionalTimeoutMs;
   }

   public void setOptionalTimeoutMs(int optionalTimeoutMs) {
      this.optionalTimeoutMs = optionalTimeoutMs;
   }
}
