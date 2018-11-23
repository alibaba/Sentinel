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

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class SpiLoader {

    private static final Map<String, ServiceLoader> SERVICE_LOADER_MAP = new ConcurrentHashMap<String, ServiceLoader>();

    public static <T> T loadFirstInstance(Class<T> clazz) {
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
    }

    private SpiLoader() {}
}
