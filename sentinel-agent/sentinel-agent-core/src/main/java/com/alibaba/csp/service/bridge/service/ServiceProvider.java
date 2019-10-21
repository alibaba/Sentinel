package com.alibaba.csp.service.bridge.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceProvider {
   private static Map<String, Service> serviceMap = new HashMap();
   private static boolean isInitialized = false;

   public static synchronized void cache(List<Service> services) {
      if (!isInitialized) {
         if (services != null) {
            Iterator var1 = services.iterator();

            while(var1.hasNext()) {
               Service service = (Service)var1.next();
               serviceMap.put(service.getName(), service);
            }

            isInitialized = true;
         }
      }
   }

   public static synchronized void clear() {
      serviceMap.clear();
   }

   public static Service getService(String serviceName) {
      if (!isInitialized) {
         throw new RuntimeException("service has not been initialized");
      } else {
         Service service = (Service)serviceMap.get(serviceName);
         return (Service)(service != null ? service : NullService.INSTANCE);
      }
   }
}
