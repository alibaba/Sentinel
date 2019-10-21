package com.alibaba.csp.service.bridge.service;

public class NullService implements Service {
   public static final NullService INSTANCE = new NullService();

   public void start() throws Exception {
      throw new IllegalAccessException("null service");
   }

   public void restart() throws Exception {
      throw new IllegalAccessException("null service");
   }

   public void pause() throws Exception {
      throw new IllegalAccessException("null service");
   }

   public void destroy() {
   }

   public boolean isEnabled() {
      return false;
   }

   public ServiceState getState() {
      return ServiceState.EXITED;
   }

   public String getName() {
      return "null";
   }
}
