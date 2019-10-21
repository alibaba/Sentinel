package com.alibaba.csp.service.bridge.service;

public class ServiceInterceptor {
   public static boolean isEnabled(String serviceName) {
      return ServiceProvider.getService(serviceName).isEnabled();
   }
}
