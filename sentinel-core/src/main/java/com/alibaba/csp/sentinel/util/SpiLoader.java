/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.ServiceLoaderUtil;
import com.alibaba.csp.sentinel.spi.SpiOrder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class SpiLoader {

    private static final Map<String, ServiceLoader> SERVICE_LOADER_MAP = new ConcurrentHashMap<String, ServiceLoader>();

    /**
     * Load the first-found specific SPI instance
     *
     * @param clazz class of the SPI interface
     * @param <T>   SPI type
     * @return the first specific SPI instance if exists, or else return null
     * @since 1.7.0
     */
    public static <T> T loadFirstInstance(Class<T> clazz) {
        AssertUtil.notNull(clazz, "SPI class cannot be null");
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            Iterator<T> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadFirstInstance failed", t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Load the first-found specific SPI instance (excluding provided default SPI class).
     * If no other SPI implementation found, then create a default SPI instance.
     *
     * @param clazz        class of the SPI interface
     * @param defaultClass class of the default SPI implementation (if no other implementation found)
     * @param <T>          SPI type
     * @return the first specific SPI instance if exists, or else the default SPI instance
     * @since 1.7.0
     */
    public static <T> T loadFirstInstanceOrDefault(Class<T> clazz, Class<? extends T> defaultClass) {
        AssertUtil.notNull(clazz, "SPI class cannot be null");
        AssertUtil.notNull(defaultClass, "default SPI class cannot be null");
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            for (T instance : serviceLoader) {
                if (instance.getClass() != defaultClass) {
                    return instance;
                }
            }
            return defaultClass.newInstance();
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadFirstInstanceOrDefault failed", t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Load the SPI instance with highest priority.
     *
     * Note: each call return same instances.
     *
     * @param clazz class of the SPI
     * @param <T>   SPI type
     * @return the SPI instance with highest priority if exists, or else false
     * @since 1.6.0
     */
    public static <T> T loadHighestPriorityInstance(Class<T> clazz) {
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            SpiOrderWrapper<T> w = null;
            for (T spi : serviceLoader) {
                int order = SpiOrderResolver.resolveOrder(spi);
                RecordLog.info("[SpiLoader] Found {} SPI: {} with order {}", clazz.getSimpleName(),
                    spi.getClass().getCanonicalName(), order);
                if (w == null || order < w.order) {
                    w = new SpiOrderWrapper<>(order, spi);
                }
            }
            return w == null ? null : w.spi;
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadHighestPriorityInstance failed", t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Load and sorted SPI instance list.
     * Load the SPI instance list for provided SPI interface.
     *
     * Note: each call return same instances.
     *
     * @param clazz class of the SPI
     * @param <T>   SPI type
     * @return sorted SPI instance list
     * @since 1.6.0
     */
    public static <T> List<T> loadInstanceList(Class<T> clazz) {
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            List<T> list = new ArrayList<>();
            for (T spi : serviceLoader) {
                RecordLog.info("[SpiLoader] Found {} SPI: {}", clazz.getSimpleName(),
                        spi.getClass().getCanonicalName());
                list.add(spi);
            }
            return list;
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadInstanceList failed", t);
            t.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Load the sorted SPI instance list for provided SPI interface.
     *
     * Note: each call return same instances.
     *
     * @param clazz class of the SPI
     * @param <T>   SPI type
     * @return sorted SPI instance list
     * @since 1.6.0
     */
    public static <T> List<T> loadInstanceListSorted(Class<T> clazz) {
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            List<SpiOrderWrapper<T>> orderWrappers = new ArrayList<>();
            for (T spi : serviceLoader) {
                int order = SpiOrderResolver.resolveOrder(spi);
                // Since SPI is lazy initialized in ServiceLoader, we use online sort algorithm here.
                SpiOrderResolver.insertSorted(orderWrappers, spi, order);
                RecordLog.info("[SpiLoader] Found {} SPI: {} with order {}", clazz.getSimpleName(),
                    spi.getClass().getCanonicalName(), order);
            }
            List<T> list = new ArrayList<>(orderWrappers.size());
            for (int i = 0; i < orderWrappers.size(); i++) {
                list.add(orderWrappers.get(i).spi);
            }
            return list;
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadInstanceListSorted failed", t);
            t.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Load the sorted and prototype SPI instance list for provided SPI interface.
     *
     * Note: each call return different instances, i.e. prototype instance, not singleton instance.
     *
     * @param clazz class of the SPI
     * @param <T>   SPI type
     * @return sorted and different SPI instance list
     * @since 1.7.2
     */
    public static <T> List<T> loadPrototypeInstanceListSorted(Class<T> clazz) {
        try {
            // Not use SERVICE_LOADER_MAP, to make sure the instances loaded are different.
            ServiceLoader<T> serviceLoader = ServiceLoaderUtil.getServiceLoader(clazz);

            List<SpiOrderWrapper<T>> orderWrappers = new ArrayList<>();
            for (T spi : serviceLoader) {
                int order = SpiOrderResolver.resolveOrder(spi);
                // Since SPI is lazy initialized in ServiceLoader, we use online sort algorithm here.
                SpiOrderResolver.insertSorted(orderWrappers, spi, order);
                RecordLog.debug("[SpiLoader] Found {} SPI: {} with order {}", clazz.getSimpleName(),
                        spi.getClass().getCanonicalName(), order);
            }
            List<T> list = new ArrayList<>(orderWrappers.size());
            for (int i = 0; i < orderWrappers.size(); i++) {
                list.add(orderWrappers.get(i).spi);
            }
            return list;
        } catch (Throwable t) {
            RecordLog.error("[SpiLoader] ERROR: loadPrototypeInstanceListSorted failed", t);
            t.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static class SpiOrderResolver {
        private static <T> void insertSorted(List<SpiOrderWrapper<T>> list, T spi, int order) {
            int idx = 0;
            for (; idx < list.size(); idx++) {
                if (list.get(idx).getOrder() > order) {
                    break;
                }
            }
            list.add(idx, new SpiOrderWrapper<>(order, spi));
        }

        private static <T> int resolveOrder(T spi) {
            if (!spi.getClass().isAnnotationPresent(SpiOrder.class)) {
                // Lowest precedence by default.
                return SpiOrder.LOWEST_PRECEDENCE;
            } else {
                return spi.getClass().getAnnotation(SpiOrder.class).value();
            }
        }
    }

    private static class SpiOrderWrapper<T> {
        private final int order;
        private final T spi;

        SpiOrderWrapper(int order, T spi) {
            this.order = order;
            this.spi = spi;
        }

        int getOrder() {
            return order;
        }

        T getSpi() {
            return spi;
        }
    }

    private SpiLoader() {}
}
