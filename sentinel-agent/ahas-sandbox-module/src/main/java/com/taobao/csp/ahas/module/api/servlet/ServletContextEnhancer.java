package com.taobao.csp.ahas.module.api.servlet;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.module.api.PluginService;
import com.taobao.csp.ahas.service.api.util.SpiBeanFactory;
import com.taobao.csp.ahas.service.bootstrap.Bootstrap;
import com.taobao.csp.ahas.service.bridge.aop.AfterEnhancer;
import com.taobao.csp.ahas.module.util.LoggerInit;
import com.taobao.csp.ahas.module.api.servlet.ServletFilter;
import com.taobao.csp.ahas.module.util.ReflectUtil;
import com.taobao.csp.ahas.service.util.EmbeddedJarUtil;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class ServletContextEnhancer extends AfterEnhancer {
   private static final Logger LOGGER;

   public Void afterAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments, Object returnValue) throws Exception {
      List<PluginService> pluginServices = SpiBeanFactory.getInstances(PluginService.class);
      if (pluginServices.size() == 0) {
         return null;
      } else if (target == null) {
         return null;
      } else {
         String scClassName = "javax.servlet.ServletContext";
         String filterClassName = "javax.servlet.Filter";
         String fgClassName = "javax.servlet.FilterRegistration";

         Class fgClazz;
         try {
            fgClazz = classLoader.loadClass(fgClassName);
         } catch (ClassNotFoundException var22) {
            LOGGER.warn("Cannot find {}, so not servlet api 3.0. Skip web filters", fgClassName, new Object[]{var22});
            return null;
         }

         Class<?> scClazz = classLoader.loadClass(scClassName);
         Class<?> filterClazz = classLoader.loadClass(filterClassName);
         Method addFilterMethod = ReflectUtil.getMethod(scClazz, "addFilter", new Class[]{String.class, filterClazz});
         Method addMappingForUrlPatterns = ReflectUtil.getMethod(fgClazz, "addMappingForUrlPatterns", new Class[]{EnumSet.class, Boolean.TYPE, String[].class});
         String dtClassName = "javax.servlet.DispatcherType";
         Class<Enum> dtClazz = (Class<Enum>)classLoader.loadClass(dtClassName);
         Class<?> servletFilterClazz = classLoader.loadClass("com.taobao.csp.ahas.service.bridge.servlet.ServletFilter");
         List<ServletFilter> servletFilters = null;
         if (classLoader instanceof URLClassLoader) {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, new URL("file:" + EmbeddedJarUtil.getJarFileInAgent("plugins/sentinel-web-servlet-1.0.0", "ahas-plugin", Bootstrap.class.getClassLoader())));
            servletFilters = (List<ServletFilter>)SpiBeanFactory.getInstances(servletFilterClazz, classLoader);
         }

         if (servletFilters == null) {
            return null;
         } else {
            addFilterMethod.setAccessible(true);
            addMappingForUrlPatterns.setAccessible(true);

            try {
               Iterator var24 = servletFilters.iterator();

               while(var24.hasNext()) {
                  ServletFilter servletFilter = (ServletFilter)var24.next();
                  if (!filterClazz.isInstance(servletFilter)) {
                     LOGGER.warn("{} not instance of javax.servlet.Filter", servletFilter.getClass().getName());
                  } else {
                     Object filterRegistration = addFilterMethod.invoke(target, servletFilter.getFilterName(), filterClazz.cast(servletFilter));
                     addMappingForUrlPatterns.invoke(filterRegistration, EnumSet.allOf(dtClazz), true, servletFilter.getUrlPatterns());
                     LOGGER.info("Add {} filter to servlet successfully.", servletFilter.getFilterName());
                  }
               }
            } catch (Exception var23) {
               LOGGER.warn("Add filter exception, {}", var23.getMessage());
            }

            return null;
         }
      }
   }

   static {
      LOGGER = LoggerInit.LOGGER;
   }
}
