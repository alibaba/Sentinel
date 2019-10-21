package com.alibaba.csp.util;//package com.taobao.csp.ahas.util;
//
//import com.alibaba.csp.sentinel.log.RecordLog;
//import com.alibaba.csp.sentinel.spi.SpiOrder;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public final class SpiLoader {
//   private static final Map<String, ServiceLoader> SERVICE_LOADER_MAP = new ConcurrentHashMap();
//
//   public static <T> T loadFirstInstance(Class<T> clazz) {
//      try {
//         String key = clazz.getName();
//         ServiceLoader<T> serviceLoader = (ServiceLoader)SERVICE_LOADER_MAP.get(key);
//         if (serviceLoader == null) {
//            serviceLoader = ServiceLoader.load(clazz);
//            SERVICE_LOADER_MAP.put(key, serviceLoader);
//         }
//
//         Iterator<T> iterator = serviceLoader.iterator();
//         return iterator.hasNext() ? iterator.next() : null;
//      } catch (Throwable var4) {
//         RecordLog.warn("[SpiLoader] ERROR: loadFirstInstance failed", var4);
//         var4.printStackTrace();
//         return null;
//      }
//   }
//
//   public static <T> T loadHighestPriorityInstance(Class<T> clazz) {
//      try {
//         String key = clazz.getName();
//         ServiceLoader<T> serviceLoader = (ServiceLoader)SERVICE_LOADER_MAP.get(key);
//         if (serviceLoader == null) {
//            serviceLoader = ServiceLoader.load(clazz);
//            SERVICE_LOADER_MAP.put(key, serviceLoader);
//         }
//
//         SpiLoader.SpiOrderWrapper<T> w = null;
//         Iterator var4 = serviceLoader.iterator();
//
//         while(true) {
//            Object spi;
//            int order;
//            do {
//               if (!var4.hasNext()) {
//                  return w == null ? null : w.spi;
//               }
//
//               spi = var4.next();
//               order = SpiLoader.SpiOrderResolver.resolveOrder(spi);
//               RecordLog.info("[SpiLoader] Found {0} SPI: {1} with order " + order, clazz.getSimpleName(), spi.getClass().getCanonicalName());
//            } while(w != null && order >= w.order);
//
//            w = new SpiLoader.SpiOrderWrapper(order, spi);
//         }
//      } catch (Throwable var7) {
//         RecordLog.warn("[SpiLoader] ERROR: loadHighestPriorityInstance failed", var7);
//         var7.printStackTrace();
//         return null;
//      }
//   }
//
//   public static <T> List<T> loadInstanceList(Class<T> clazz) {
//      try {
//         String key = clazz.getName();
//         ServiceLoader<T> serviceLoader = (ServiceLoader)SERVICE_LOADER_MAP.get(key);
//         if (serviceLoader == null) {
//            serviceLoader = ServiceLoader.load(clazz);
//            SERVICE_LOADER_MAP.put(key, serviceLoader);
//         }
//
//         List<T> list = new ArrayList();
//         Iterator var4 = serviceLoader.iterator();
//
//         while(var4.hasNext()) {
//            T spi = (T)var4.next();
//            list.add(spi);
//         }
//
//         return list;
//      } catch (Throwable var6) {
//         RecordLog.warn("[SpiLoader] ERROR: loadInstanceListSorted failed", var6);
//         var6.printStackTrace();
//         return new ArrayList();
//      }
//   }
//
//   public static <T> List<T> loadInstanceListSorted(Class<T> clazz) {
//      try {
//         String key = clazz.getName();
//         ServiceLoader<T> serviceLoader = (ServiceLoader)SERVICE_LOADER_MAP.get(key);
//         if (serviceLoader == null) {
//            serviceLoader = ServiceLoader.load(clazz);
//            SERVICE_LOADER_MAP.put(key, serviceLoader);
//         }
//
//         List<SpiOrderWrapper<T>> orderWrappers = new ArrayList();
//         Iterator var4 = serviceLoader.iterator();
//
//         while(var4.hasNext()) {
//            T spi = (T)var4.next();
//            int order = SpiLoader.SpiOrderResolver.resolveOrder(spi);
//            SpiLoader.SpiOrderResolver.insertSorted(orderWrappers, spi, order);
//            RecordLog.info("[SpiLoader] Found {0} SPI: {1} with order " + order, clazz.getSimpleName(), spi.getClass().getCanonicalName());
//         }
//
//         List<T> list = new ArrayList();
//
//         for(int i = 0; i < orderWrappers.size(); ++i) {
//            list.add(i, ((T)orderWrappers.get(i)));
//         }
//
//         return list;
//      } catch (Throwable var7) {
//         RecordLog.warn("[SpiLoader] ERROR: loadInstanceListSorted failed", var7);
//         var7.printStackTrace();
//         return new ArrayList();
//      }
//   }
//
//   private SpiLoader() {
//   }
//
//   private static class SpiOrderWrapper<T> {
//      private final int order;
//      private final T spi;
//
//      SpiOrderWrapper(int order, T spi) {
//         this.order = order;
//         this.spi = spi;
//      }
//
//      int getOrder() {
//         return this.order;
//      }
//
//      T getSpi() {
//         return this.spi;
//      }
//   }
//
//   private static class SpiOrderResolver {
//      private static <T> void insertSorted(List<SpiOrderWrapper<T>> list, T spi, int order) {
//         int idx;
//         for(idx = 0; idx < list.size() && ((SpiLoader.SpiOrderWrapper)list.get(idx)).getOrder() <= order; ++idx) {
//         }
//
//         list.add(idx, new SpiLoader.SpiOrderWrapper(order, spi));
//      }
//
//      private static <T> int resolveOrder(T spi) {
//         return !spi.getClass().isAnnotationPresent(SpiOrder.class) ? Integer.MAX_VALUE : ((SpiOrder)spi.getClass().getAnnotation(SpiOrder.class)).value();
//      }
//   }
//}
