package com.taobao.csp.ahas.module.api.gateway;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.service.api.util.SpiBeanFactory;
import com.taobao.csp.ahas.service.bootstrap.Bootstrap;
import com.taobao.csp.ahas.service.bridge.aop.BeforeEnhancer;
import com.taobao.csp.ahas.module.util.LoggerInit;
import com.taobao.csp.ahas.module.api.gateway.ScGlobalFilter;
import com.taobao.csp.ahas.service.util.EmbeddedJarUtil;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

public class ScGatewayFilterEnhancer extends BeforeEnhancer {
   private static final Logger LOGGER;

   public Void beforeAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments) throws Exception {
      if (target != null && classLoader != null) {
         List<Object> filters = (List)((List)methodArguments[0]);
         Class<?> scGlobalFilterClazz = classLoader.loadClass("com.taobao.csp.ahas.service.bridge.sc.ScGlobalFilter");
         List<ScGlobalFilter> scGlobalFilters = null;
         if (classLoader instanceof URLClassLoader) {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, new URL("file:" + EmbeddedJarUtil.getJarFileInAgent("plugins/sentinel-spring-cloud-gateway-adapter-1.0.0", "ahas-plugin", Bootstrap.class.getClassLoader())));
            scGlobalFilters = (List<ScGlobalFilter>)SpiBeanFactory.getInstances(scGlobalFilterClazz, classLoader);
         }

         if (scGlobalFilters == null) {
            return null;
         } else {
            Iterator var10 = scGlobalFilters.iterator();

            while(var10.hasNext()) {
               ScGlobalFilter filter = (ScGlobalFilter)var10.next();
               filters.add(filter);
               LOGGER.info("Match ScGlobalFilter name: {}", filter.getClass().getName());
            }

            return null;
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
