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
import com.alibaba.csp.sentinel.spi.SpiOrder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class SpiLoader {

    private static final Map<String, ServiceLoader> SERVICE_LOADER_MAP = new ConcurrentHashMap<String, ServiceLoader>();

    public static <T> T loadFirstInstance(Class<T> clazz) {
        try {
            String key = clazz.getName();
            // Not thread-safe, as it's expected to be resolved in a thread-safe context.
            ServiceLoader<T> serviceLoader = SERVICE_LOADER_MAP.get(key);
            if (serviceLoader == null) {
                serviceLoader = ServiceLoader.load(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            Iterator<T> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        } catch (Throwable t) {
            RecordLog.warn("[SpiLoader] ERROR: loadFirstInstance failed", t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Load the SPI instance with highest priority.
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
                serviceLoader = ServiceLoader.load(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            SpiOrderWrapper<T> w = null;
            for (T spi : serviceLoader) {
                int order = SpiOrderResolver.resolveOrder(spi);
                RecordLog.info("[SpiLoader] Found {0} SPI: {1} with order " + order, clazz.getSimpleName(),
                    spi.getClass().getCanonicalName());
                if (w == null || order < w.order) {
                    w = new SpiOrderWrapper<>(order, spi);
                }
            }
            return w == null ? null : w.spi;
        } catch (Throwable t) {
            RecordLog.warn("[SpiLoader] ERROR: loadHighestPriorityInstance failed", t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Load the SPI instance list for provided SPI interface.
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
                serviceLoader = ServiceLoader.load(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            List<T> list = new ArrayList<>();
            for (T spi : serviceLoader) {
                list.add(spi);
            }
            return list;
        } catch (Throwable t) {
            RecordLog.warn("[SpiLoader] ERROR: loadInstanceListSorted failed", t);
            t.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Load the sorted SPI instance list for provided SPI interface.
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
                serviceLoader = ServiceLoader.load(clazz);
                SERVICE_LOADER_MAP.put(key, serviceLoader);
            }

            List<SpiOrderWrapper<T>> orderWrappers = new ArrayList<>();
            for (T spi : serviceLoader) {
                int order = SpiOrderResolver.resolveOrder(spi);
                // Since SPI is lazy initialized in ServiceLoader, we use online sort algorithm here.
                SpiOrderResolver.insertSorted(orderWrappers, spi, order);
                RecordLog.info("[SpiLoader] Found {0} SPI: {1} with order " + order, clazz.getSimpleName(),
                    spi.getClass().getCanonicalName());
            }
            List<T> list = new ArrayList<>();
            for (int i = 0; i < orderWrappers.size(); i++) {
                list.add(i, orderWrappers.get(i).spi);
            }
            return list;
        } catch (Throwable t) {
            RecordLog.warn("[SpiLoader] ERROR: loadInstanceListSorted failed", t);
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
