package com.taobao.csp.ahas.module.util;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.service.api.util.SpiBeanFactory;
import com.taobao.csp.ahas.module.util.LoggerInit;
import com.taobao.csp.ahas.module.util.ClassLoaderUtil;
import com.taobao.csp.ahas.module.util.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

final class ServiceLoaderUtils {
   private static final Logger LOGGER;
   private static final String PREFIX = "META-INF/services/";

   public static Map<URL, Map<String, Class<?>>> load(Class<?> type, ClassLoader loader, String... excludePackages) {
      if (loader == null) {
         loader = SpiBeanFactory.getLoader();
      }

      if (type != null && loader != null) {
         LinkedHashMap repo = new LinkedHashMap();

         try {
            Enumeration urlEnums = findResource(loader, "META-INF/services/" + type.getName());

            label172:
            while(urlEnums.hasMoreElements()) {
               BufferedReader br = null;
               URL currentUrl = (URL)urlEnums.nextElement();
               Map<String, Class<?>> extension = new LinkedHashMap();
               repo.put(currentUrl, extension);

               try {
                  br = new BufferedReader(new InputStreamReader(currentUrl.openStream()));

                  while(true) {
                     String classNameStr;
                     do {
                        do {
                           do {
                              if ((classNameStr = br.readLine()) == null) {
                                 continue label172;
                              }
                           } while(!StringUtil.containsNone(classNameStr, "#"));
                        } while(!StringUtil.isNotBlank(classNameStr));
                     } while(!StringUtil.containsNone(classNameStr, "="));

                     String className = classNameStr.trim();
                     boolean exclude = false;
                     if (excludePackages != null) {
                        String[] var11 = excludePackages;
                        int var12 = excludePackages.length;

                        for(int var13 = 0; var13 < var12; ++var13) {
                           String excludePack = var11[var13];
                           if (className.startsWith(excludePack)) {
                              exclude = true;
                              break;
                           }
                        }
                     }

                     if (exclude) {
                        LOGGER.debug("ServiceLoaderUtils", "provider-class:{} for spi-interface:{} is exclude by package exclusion", new Object[]{className, type.getName()});
                     } else {
                        extension.put(className, null);

                        try {
                           Class<?> impl = loader.loadClass(className);
                           if (type.isAssignableFrom(impl)) {
                              extension.put(className, impl);
                           } else {
                              LOGGER.warn("class: {} does not implement.", className);
                           }
                        } catch (Throwable var19) {
                           LOGGER.warn("Load spi class exception. class: {}", className);
                        }
                     }
                  }
               } finally {
                  if (br != null) {
                     br.close();
                  }

               }
            }

            return repo;
         } catch (IOException var21) {
            throw new RuntimeException(var21);
         }
      } else {
         throw new IllegalArgumentException("class type and classloader can not be null");
      }
   }

   public static <T> T newSpiInstance(Class<T> clazz) {
      T result = null;
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();

      try {
         ClassLoaderUtil.switchContextLoader(clazz.getClassLoader());
         result = clazz.newInstance();
      } catch (Exception var7) {
      } finally {
         ClassLoaderUtil.switchContextLoader(tccl);
      }

      return result;
   }

   private static Enumeration<URL> findResource(ClassLoader classLoader, String resourceName) throws IOException {
      Enumeration<URL> result = null;
      if (resourceName != null && classLoader != null) {
         result = classLoader.getResources(resourceName);
      }

      return (Enumeration)(result != null ? result : new com.taobao.csp.ahas.module.util.ServiceLoaderUtils.EmptyEnumeration());
   }

   static {
      LOGGER = LoggerInit.LOGGER;
   }

   private static class EmptyEnumeration<E> implements Enumeration<E> {
      private EmptyEnumeration() {
      }

      public boolean hasMoreElements() {
         return false;
      }

      public E nextElement() {
         throw new NoSuchElementException();
      }

      // $FF: synthetic method
      EmptyEnumeration(Object x0) {
         this();
      }
   }
}
