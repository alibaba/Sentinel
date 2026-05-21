/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.util.StringUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for resource configuration metadata (e.g. fallback method)
 *
 * @author Eric Zhao
 * @author dowenliu-xyz(hawkdowen@hotmail.com)
 */
final class ResourceMetadataRegistry {

    private static final Map<String, MethodWrapper> FALLBACK_MAP = new ConcurrentHashMap<>();
    private static final Map<String, MethodWrapper> DEFAULT_FALLBACK_MAP = new ConcurrentHashMap<>();
    private static final Map<String, MethodWrapper> BLOCK_HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * @deprecated use {@link #lookupFallback(Class, String, Class[])}
     */
    @Deprecated
    static MethodWrapper lookupFallback(Class<?> clazz, String name) {
        return FALLBACK_MAP.get(getKey(clazz, name));
    }

    static MethodWrapper lookupFallback(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return FALLBACK_MAP.get(getKey(clazz, name, parameterTypes));
    }

    static MethodWrapper lookupDefaultFallback(Class<?> clazz, String name) {
        return DEFAULT_FALLBACK_MAP.get(getKey(clazz, name));
    }

    /**
     * @deprecated use {@link #lookupBlockHandler(Class, String, Class[])}
     */
    @Deprecated
    static MethodWrapper lookupBlockHandler(Class<?> clazz, String name) {
        return BLOCK_HANDLER_MAP.get(getKey(clazz, name));
    }

    static MethodWrapper lookupBlockHandler(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return BLOCK_HANDLER_MAP.get(getKey(clazz, name, parameterTypes));
    }

    /**
     * @deprecated use {@link #updateFallbackFor(Class, String, Class[], Method)}
     */
    @Deprecated
    static void updateFallbackFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        FALLBACK_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }

    static void updateFallbackFor(Class<?> clazz, String handlerName, Class<?>[] parameterTypes, Method handlerMethod) {
        if (clazz == null || StringUtil.isBlank(handlerName)) {
            throw new IllegalArgumentException("Bad argument");
        }
        FALLBACK_MAP.put(getKey(clazz, handlerName, parameterTypes), MethodWrapper.wrap(handlerMethod));
    }

    static void updateDefaultFallbackFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        DEFAULT_FALLBACK_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }


    /**
     * @deprecated use {@link #updateBlockHandlerFor(Class, String, Class[], Method)}
     */
    @Deprecated
    static void updateBlockHandlerFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        BLOCK_HANDLER_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }

    static void updateBlockHandlerFor(Class<?> clazz, String handlerName, Class<?>[] parameterTypes,
                                      Method handlerMethod) {
        if (clazz == null || StringUtil.isBlank(handlerName)) {
            throw new IllegalArgumentException("Bad argument");
        }
        BLOCK_HANDLER_MAP.put(getKey(clazz, handlerName, parameterTypes), MethodWrapper.wrap(handlerMethod));
    }

    private static String getKey(Class<?> clazz, String name) {
        return String.format("%s:%s", clazz.getCanonicalName(), name);
    }

    private static String getKey(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return String.format(
                "%s:%s;%s",
                clazz.getCanonicalName(),
                name,
                Arrays.stream(parameterTypes).map(Class::getCanonicalName).collect(Collectors.joining(","))
        );
    }

    /**
     * Only for internal test.
     */
    static void clearFallbackMap() {
        FALLBACK_MAP.clear();
    }

    /**
     * Only for internal test.
     */
    static void clearBlockHandlerMap() {
        BLOCK_HANDLER_MAP.clear();
    }
}
