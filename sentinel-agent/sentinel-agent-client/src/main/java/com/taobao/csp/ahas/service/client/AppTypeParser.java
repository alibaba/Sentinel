package com.taobao.csp.ahas.service.client;

import com.taobao.csp.ahas.service.util.AppNameUtil;

class AppTypeParser {
   public static final int APP_TYPE_GATEWAY = 1;
   public static final int APP_TYPE_ZUUL = 12;

   public static int parseAppType() {
      String type = System.getProperty("csp.sentinel.app.type");
      if (type != null && !type.isEmpty()) {
         return AppNameUtil.getAppType();
      } else {
         String scName = "org.springframework.cloud.gateway.config.GatewayAutoConfiguration";
         String zuulName = "org.springframework.cloud.netflix.zuul.ZuulFilterInitializer";
         ClassLoader classLoader = ClassLoader.getSystemClassLoader();

         Class clazz;
         try {
            clazz = classLoader.loadClass(scName);
            if (clazz != null) {
               return 1;
            }
         } catch (Throwable var6) {
         }

         try {
            clazz = classLoader.loadClass(zuulName);
            if (clazz != null) {
               return 12;
            }
         } catch (Throwable var5) {
         }

         return AppNameUtil.getAppType();
      }
   }
}
