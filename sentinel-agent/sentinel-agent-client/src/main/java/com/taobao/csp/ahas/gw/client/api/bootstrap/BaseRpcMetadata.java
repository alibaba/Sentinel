package com.taobao.csp.ahas.gw.client.api.bootstrap;

public abstract class BaseRpcMetadata<C extends BaseRpcMetadata> {
   public static final int MIN_RPC_TIMEOUT_MS = 500;
   public static final int DEFAULT_RPC_TIMEOUT_MS = 4000;
   private int optionalTimeoutMs = 4000;
   private boolean requestCompress = false;
   private boolean responseCompress = false;

   public int getOptionalTimeoutMs() {
      return this.optionalTimeoutMs;
   }

   public C setOptionalTimeoutMs(int optionalTimeoutMs) {
      this.optionalTimeoutMs = optionalTimeoutMs;
      return (C)this;
   }

   public boolean isRequestCompress() {
      return this.requestCompress;
   }

   public C setRequestCompress(boolean requestCompress) {
      this.requestCompress = requestCompress;
      return (C)this;
   }

   public boolean isResponseCompress() {
      return this.responseCompress;
   }

   public C setResponseCompress(boolean responseCompress) {
      this.responseCompress = responseCompress;
      return (C)this;
   }

   public abstract void check();
}
