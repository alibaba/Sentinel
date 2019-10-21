package com.taobao.csp.ahas.module.api.zuul;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.service.api.util.SpiBeanFactory;
import com.taobao.csp.ahas.service.bootstrap.Bootstrap;
import com.taobao.csp.ahas.service.bridge.aop.BeforeEnhancer;
import com.taobao.csp.ahas.module.util.LoggerInit;
import com.taobao.csp.ahas.module.api.zuul.SentinelZuulFilter;
import com.taobao.csp.ahas.service.util.EmbeddedJarUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZuulFilterEnhancer extends BeforeEnhancer {
   private static final Logger LOGGER;

   public Void beforeAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments) throws Exception {
      if (target != null && classLoader != null) {
         Field field = target.getClass().getDeclaredField("filters");
         field.setAccessible(true);
         Map<String, Object> filters = (Map)field.get(target);
         Class<?> filterClazz = classLoader.loadClass("com.taobao.csp.ahas.service.bridge.zuul.SentinelZuulFilter");
         List<SentinelZuulFilter> zuulFilters = null;
         if (classLoader instanceof URLClassLoader) {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, new URL("file:" + EmbeddedJarUtil.getJarFileInAgent("plugins/sentinel-zuul-adapter-1.0.0", "ahas-plugin", Bootstrap.class.getClassLoader())));
            zuulFilters = (List<SentinelZuulFilter>)SpiBeanFactory.getInstances(filterClazz, classLoader);
         }

         if (zuulFilters == null) {
            return null;
         } else {
            Iterator var12 = zuulFilters.iterator();

            while(var12.hasNext()) {
               SentinelZuulFilter filter = (SentinelZuulFilter)var12.next();
               String name = filter.getClass().getSimpleName();
               name = this.firstLetterToLower(name);
               filters.put(name, filter);
               LOGGER.info("Match ZuulFilter name: {}", filter.getClass().getName());
            }

            return null;
         }
      } else {
         LOGGER.warn("Target or classloader is null");
         return null;
      }
   }

   private String firstLetterToLower(String str) {
      return str.substring(0, 1).toLowerCase() + str.substring(1);
   }

   static {
      LOGGER = LoggerInit.LOGGER;
   }
}
