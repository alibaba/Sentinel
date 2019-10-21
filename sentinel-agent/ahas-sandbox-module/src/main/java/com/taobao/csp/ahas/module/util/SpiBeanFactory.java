package com.taobao.csp.ahas.module.util;

import com.google.inject.Injector;
import com.taobao.csp.ahas.module.annotation.annotation.Name;
import com.taobao.csp.ahas.module.annotation.annotation.Optional;
import com.taobao.csp.ahas.module.annotation.annotation.Scope;
import com.taobao.csp.ahas.module.annotation.annotation.Scope.Option;
import com.taobao.csp.ahas.module.annotation.annotation.Tag;
import com.taobao.csp.ahas.module.util.StringUtil;

import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SpiBeanFactory {
   private static final String[] ALL_NAME = new String[0];
   private static final String ALL_NAME_ABBR = "%%";
   public static Injector INJECTOR = null;
   private static ConcurrentHashMap<Class<?>, Map<String, Class<?>>> spiRepository = new ConcurrentHashMap();
   private static ConcurrentHashMap<Class<?>, Object> singletonCache = new ConcurrentHashMap();
   private static ClassLoader loader = SpiBeanFactory.class.getClassLoader();

   private static boolean isSingletonType(Class<?> interfaceClazz, Class<?> concreteClazz) {
      boolean result = false;
      if (interfaceClazz != null && concreteClazz != null) {
         Option option = Option.SINGLETON;
         Scope scope;
         if (concreteClazz.isAnnotationPresent(Scope.class)) {
            scope = (Scope)concreteClazz.getAnnotation(Scope.class);
            option = scope.value();
         } else if (interfaceClazz.isAnnotationPresent(Scope.class)) {
            scope = (Scope)interfaceClazz.getAnnotation(Scope.class);
            option = scope.value();
         }

         result = option == Option.SINGLETON;
      }

      return result;
   }

   private static void fillSpiConcreteClazz(Class<?> type, Map<String, Class<?>> src, Map<String, Class<?>> dest) {
      if (dest != null && src != null) {
         Iterator var3 = src.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, Class<?>> entry = (Entry)var3.next();
            if (entry.getValue() != null) {
               dest.put(entry.getKey(), entry.getValue());
               Name instanceKey = (Name)((Class)entry.getValue()).getAnnotation(Name.class);
               if (instanceKey != null && StringUtil.isNotBlank(instanceKey.value())) {
                  dest.put(instanceKey.value(), entry.getValue());
               }
            }
         }
      }

   }

   public static <T> T getInstance(Class<T> classType, String name) {
      return getInstance(classType, name, new String[0], loader);
   }

   public static <T> T getInstance(Class<T> classType, String name, String[] tags, ClassLoader classLoader) {
      T instance = null;
      String[] names;
      if (name == null) {
         names = ALL_NAME;
      } else {
         names = StringUtil.equals(name, "%%") ? ALL_NAME : new String[]{name};
      }

      List<T> instanceList = findInstances(classType, names, tags, false, classLoader);
      if (instanceList != null && !instanceList.isEmpty()) {
         instance = instanceList.get(0);
      }

      return instance;
   }

   private static <T> List<T> findInstances(Class<T> classType, String[] names, String[] tags, boolean withDefault, ClassLoader loader) {
      if (classType == null) {
         return Collections.emptyList();
      } else {
         loadSpi(classType, loader);
         List<T> instanceList = new ArrayList();
         Map<String, Class<?>> spiTypes = (Map)spiRepository.get(classType);
         Iterator iterator;
         if (spiTypes != null && !spiTypes.isEmpty()) {
            if (ALL_NAME != names && !withDefault) {
               Set<Class<?>> clazzSet = new LinkedHashSet();
               String[] var17 = names;
               int var20 = names.length;

               for(int var10 = 0; var10 < var20; ++var10) {
                  String name = var17[var10];
                  Class<?> clazz = (Class)spiTypes.get(name);
                  if (clazz != null) {
                     clazzSet.add(clazz);
                  }
               }

               if (!clazzSet.isEmpty()) {
                  Iterator var18 = clazzSet.iterator();

                  while(var18.hasNext()) {
                     Class<?> clazz = (Class)var18.next();
                     T instance = createInstance(classType, clazz);
                     if (instance != null) {
                        instanceList.add(instance);
                     }
                  }
               }
            } else {
               iterator = (new LinkedHashSet(spiTypes.values())).iterator();

               while(iterator.hasNext()) {
                  Class<?> clazz = (Class)iterator.next();
                  if (isPermit(classType, clazz, names)) {
                     T instance = createInstance(classType, clazz);
                     if (instance != null) {
                        instanceList.add(instance);
                     }
                  }
               }
            }
         }

         if (tags != null && tags.length > 0) {
            iterator = instanceList.iterator();

            label66:
            while(true) {
               Tag tag;
               do {
                  do {
                     if (!iterator.hasNext()) {
                        break label66;
                     }

                     T next = (T)iterator.next();
                     tag = (Tag)next.getClass().getAnnotation(Tag.class);
                  } while(tag == null);
               } while(tag.value().length <= 0);

               Set<String> tagSet = new HashSet();
               Collections.addAll(tagSet, tags);
               int defineTagNum = tagSet.size();
               String[] var26 = tag.value();
               int var13 = var26.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  String classTag = var26[var14];
                  tagSet.remove(classTag);
               }

               if (defineTagNum == tagSet.size()) {
                  iterator.remove();
               }
            }
         }

         OrderSortUtil.sortByOrder(instanceList);
         return instanceList;
      }
   }

   private static synchronized void loadSpi(Class<?> type, ClassLoader classLoader) {
      if (!spiRepository.containsKey(type)) {
         Map<String, Class<?>> spiImplsMap = new LinkedHashMap();
         Map<URL, Map<String, Class<?>>> extension = ServiceLoaderUtils.load(type, classLoader);
         Iterator var4 = extension.values().iterator();

         while(var4.hasNext()) {
            Map<String, Class<?>> extensionMap = (Map)var4.next();
            fillSpiConcreteClazz(type, extensionMap, spiImplsMap);
         }

         spiRepository.put(type, spiImplsMap);
      }

   }

   private static boolean isPermit(Class<?> interfaceClazz, Class<?> clazz, String[] names) {
      boolean result = false;
      if (clazz.isAnnotationPresent(Optional.class)) {
         Map<String, Class<?>> spiTypes = (Map)spiRepository.get(interfaceClazz);
         String[] var5 = names;
         int var6 = names.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String name = var5[var7];
            Class<?> implClazz = (Class)spiTypes.get(name);
            if (implClazz == clazz) {
               result = true;
            }
         }
      } else {
         result = true;
      }

      return result;
   }

   private static <T> T createInstance(Class<T> intfClazz, Class<?> clazz) {
      Object result;
      if (isSingletonType(intfClazz, clazz)) {
         result = getSingleton(clazz);
      } else {
         result = getPrototype(clazz);
      }

      return (T)result;
   }

   private static Object getSingleton(Class<?> clazz) {
      Object result = null;
      if (clazz != null) {
         result = singletonCache.get(clazz);
         if (result == null) {
            result = getPrototype(clazz);
            if (result != null) {
               Object previous = singletonCache.putIfAbsent(clazz, result);
               if (previous != null) {
                  result = previous;
               }
            }
         }
      }

      return result;
   }

   private static Object getPrototype(Class<?> clazz) {
      Object instance = ServiceLoaderUtils.newSpiInstance(clazz);
      awareInstance(instance);
      return instance;
   }

   private static void awareInstance(Object instance) {
      if (INJECTOR == null) {
         throw new RuntimeException("SpiBeanFactory not initialized!");
      } else {
         INJECTOR.injectMembers(instance);
      }
   }

   public static <T> T getInstance(Class<T> classType, ClassLoader classLoader) {
      return getInstance(classType, "%%", new String[0], classLoader);
   }

   public static <T> T getInstance(Class<T> classType) {
      return getInstance(classType, "%%", new String[0], loader);
   }

   public static <T> List<T> getInstances(Class<T> classType, String[] tags, ClassLoader classLoader) {
      return getInstances(classType, ALL_NAME, tags, classLoader);
   }

   public static <T> List<T> getInstances(Class<T> classType, String[] names, String[] tags) {
      return getInstances(classType, names, tags, false, loader);
   }

   public static <T> List<T> getInstances(Class<T> classType, String[] names, String[] tags, ClassLoader classLoader) {
      return getInstances(classType, names, tags, false, classLoader);
   }

   public static <T> List<T> getInstances(Class<T> classType, String[] names, String[] tags, boolean withDefault, ClassLoader classLoader) {
      if (names == null || names.length == 0) {
         names = ALL_NAME;
      }

      return findInstances(classType, names, tags, withDefault, classLoader);
   }

   public static <T> List<T> getInstances(Class<T> classType) {
      return getInstances(classType, new String[0], loader);
   }

   public static <T> List<T> getInstances(Class<T> classType, ClassLoader classLoader) {
      return getInstances(classType, new String[0], classLoader);
   }

   public static Set<Class<?>> getExtensionClass(Class<?> classType) {
      return getExtensionClass(classType, new String[0]);
   }

   public static Set<Class<?>> getExtensionClass(Class<?> classType, String[] names) {
      if (classType == null) {
         return Collections.emptySet();
      } else {
         loadSpi(classType, loader);
         Set<Class<?>> clazzSet = Collections.emptySet();
         Map<String, Class<?>> spiTypes = (Map)spiRepository.get(classType);
         if (spiTypes != null && !spiTypes.isEmpty()) {
            List<Class<?>> classList = new ArrayList();
            Iterator var5 = spiTypes.values().iterator();

            while(var5.hasNext()) {
               Class<?> clazz = (Class)var5.next();
               if (isPermit(classType, clazz, names)) {
                  classList.add(clazz);
               }
            }

            clazzSet = new LinkedHashSet();
            OrderSortUtil.sortByOrder(classList);
            ((Set)clazzSet).addAll(classList);
         }

         return (Set)clazzSet;
      }
   }

   public static ClassLoader getLoader() {
      return loader;
   }

   public static void setClassLoader(ClassLoader classLoader) {
      loader = classLoader;
   }
}
