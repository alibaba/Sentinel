package com.taobao.csp.ahas.module.api.dubbo;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.service.bootstrap.Bootstrap;
import com.taobao.csp.ahas.service.bridge.aop.BeforeEnhancer;
import com.taobao.csp.ahas.module.util.LoggerInit;
import com.taobao.csp.ahas.module.util.ReflectUtil;
import com.taobao.csp.ahas.service.util.EmbeddedJarUtil;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DubboFilterEnhancer extends BeforeEnhancer {
   public static final String DUBBO_RPC_FILTER_NAME = "META-INF/dubbo/com.alibaba.dubbo.rpc.Filter";
   private static final Logger LOGGER;

   public Void beforeAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments) throws Exception {
      if (target != null && classLoader != null) {
         Object value = ReflectUtil.getFieldValue(target, "type", false);
         if (value == null) {
            LOGGER.warn("Cannot get type field value");
            return null;
         } else {
            String typeName = ((Class)value).getName();
            String fileName = methodArguments[1] + typeName;
            if (!"META-INF/dubbo/com.alibaba.dubbo.rpc.Filter".equals(fileName)) {
               return null;
            } else {
               LOGGER.info("Match filter name: {}", "META-INF/dubbo/com.alibaba.dubbo.rpc.Filter");
               Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
               addURL.setAccessible(true);
               addURL.invoke(classLoader, new URL("file:" + EmbeddedJarUtil.getJarFileInAgent("plugins/sentinel-dubbo-adapter-1.0.0", "ahas-plugin", Bootstrap.class.getClassLoader())));
               return null;
            }
         }
      } else {
         LOGGER.warn("Target or classloader is null");
         return null;
      }
   }

   static {
      LOGGER = LoggerInit.LOGGER;
   }
}
