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
package com.alibaba.csp.sentinel.annotation.aspectj;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Registry for resource configuration metadata (e.g. fallback method)
 *
 * @author Eric Zhao
 * @author dowenliu-xyz(hawkdowen@hotmail.com)
 */
final class ResourceMetadataRegistry {
    private static final Map<String, MethodWrapper> FALLBACK_MAP = new ConcurrentHashMap<>();
    private static final Map<HandlerMeta, MethodWrapper> FALLBACK_METAMAP = new ConcurrentHashMap<>();
    private static final Map<String, MethodWrapper> DEFAULT_FALLBACK_MAP = new ConcurrentHashMap<>();
    private static final Map<HandlerMeta, MethodWrapper> DEFAULT_FALLBACK_METAMAP = new ConcurrentHashMap<>();
    private static final Map<String, MethodWrapper> BLOCK_HANDLER_MAP = new ConcurrentHashMap<>();
    private static final Map<HandlerMeta, MethodWrapper> BLOCK_HANDLER_METAMAP = new ConcurrentHashMap<>();

    /**
     * @deprecated use {@link #lookupFallback(Method, Class, String)}
     */
    @Deprecated
    static MethodWrapper lookupFallback(Class<?> clazz, String name) {
        return FALLBACK_MAP.get(getKey(clazz, name));
    }

    /**
     * @deprecated use {@link #lookupFallback(Method, Class, String)}
     */
    @Deprecated
    static MethodWrapper lookupFallback(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return FALLBACK_MAP.get(getKey(clazz, name, parameterTypes));
    }

    /**
     * Lookup the fallback handler method of the origin method, with specified name in the specified class.
     *
     * @param originMethod the origin method which is annotated by `@SentinelResource` with the fallback handler
     *                     name value.
     * @param handlerClass the class of the defaultFallback handler method.
     * @param handlerName  the name of expected fallback handler method.
     * @return the found method wrapper. If currently the method is not registered, return {@code null}. Even if the
     * returned wrapper is not {@code null}, the method it contains may still be {@code null}, which means that the
     * expected handler method does not exist.
     */
    static MethodWrapper lookupFallback(Method originMethod, Class<?> handlerClass, String handlerName) {
        return FALLBACK_METAMAP.get(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName));
    }

    /**
     * @deprecated use {@link #lookupDefaultFallback(Method, Class, String)}
     */
    static MethodWrapper lookupDefaultFallback(Class<?> clazz, String name) {
        return DEFAULT_FALLBACK_MAP.get(getKey(clazz, name));
    }

    /**
     * Lookup the defaultFallback handler method of the origin method, with specified name in the specified class.
     *
     * @param originMethod the origin method which is annotated by `@SentinelResource` with the defaultFallback
     *                     handler name value.
     * @param handlerClass the class of the defaultFallback handler method.
     * @param handlerName  the name of expected defaultFallback handler method.
     * @return the found method wrapper. If currently the method is not registered, return {@code null}. Even if the
     * returned wrapper is not {@code null}, the method it contains may still be {@code null}, which means that the
     * expected handler method does not exist.
     */
    static MethodWrapper lookupDefaultFallback(Method originMethod, Class<?> handlerClass, String handlerName) {
        return DEFAULT_FALLBACK_METAMAP.get(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName));
    }

    /**
     * @deprecated use {@link #lookupBlockHandler(Method, Class, String)}
     */
    @Deprecated
    static MethodWrapper lookupBlockHandler(Class<?> clazz, String name) {
        return BLOCK_HANDLER_MAP.get(getKey(clazz, name));
    }

    /**
     * @deprecated use {@link #lookupBlockHandler(Method, Class, String)}
     */
    static MethodWrapper lookupBlockHandler(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return BLOCK_HANDLER_MAP.get(getKey(clazz, name, parameterTypes));
    }

    /**
     * Lookup the blockHandler handler method of the origin method, with specified name in the specified class.
     * @param originMethod the origin method which is annotated by `@SentinelResource` with the blockHandler handler
     *                    name value.
     * @param handlerClass the class of the blockHandler handler method.
     * @param handlerName the name of expected blockHandler handler method.
     * @return the found method wrapper. If currently the method is not registered, return {@code null}. Even if the
     * returned wrapper is not {@code null}, the method it contains may still be {@code null}, which means that the
     * expected handler method does not exist.
     */
    static MethodWrapper lookupBlockHandler(Method originMethod, Class<?> handlerClass, String handlerName) {
        return BLOCK_HANDLER_METAMAP.get(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName));
    }

    /**
     * @deprecated use {@link #updateFallbackFor(Method, Class, String, Method)}
     */
    @Deprecated
    static void updateFallbackFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        FALLBACK_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }

    /**
     * @deprecated use {@link #updateFallbackFor(Method, Class, String, Method)}
     */
    static void updateFallbackFor(Class<?> clazz, String handlerName, Class<?>[] parameterTypes, Method handlerMethod) {
        if (clazz == null || StringUtil.isBlank(handlerName)) {
            throw new IllegalArgumentException("Bad argument");
        }
        FALLBACK_MAP.put(getKey(clazz, handlerName, parameterTypes), MethodWrapper.wrap(handlerMethod));
    }

    static void updateFallbackFor(Method originMethod, Class<?> handlerClass, String handlerName,
                                  Method handlerMethod) {
        FALLBACK_METAMAP.put(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName),
                MethodWrapper.wrap(handlerMethod));
    }

    /**
     * @deprecated use {@link #updateDefaultFallbackFor(Method, Class, String, Method)}
     */
    static void updateDefaultFallbackFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        DEFAULT_FALLBACK_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }

    static void updateDefaultFallbackFor(Method originMethod, Class<?> handlerClass, String handlerName,
                                         Method handlerMethod) {
        DEFAULT_FALLBACK_METAMAP.put(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName),
                MethodWrapper.wrap(handlerMethod));
    }


    /**
     * @deprecated use {@link #updateBlockHandlerFor(Method, Class, String, Method)}
     */
    @Deprecated
    static void updateBlockHandlerFor(Class<?> clazz, String name, Method method) {
        if (clazz == null || StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Bad argument");
        }
        BLOCK_HANDLER_MAP.put(getKey(clazz, name), MethodWrapper.wrap(method));
    }

    /**
     * @deprecated use {@link #updateBlockHandlerFor(Method, Class, String, Method)}
     */
    static void updateBlockHandlerFor(Class<?> clazz, String handlerName, Class<?>[] parameterTypes,
                                      Method handlerMethod) {
        if (clazz == null || StringUtil.isBlank(handlerName)) {
            throw new IllegalArgumentException("Bad argument");
        }
        BLOCK_HANDLER_MAP.put(getKey(clazz, handlerName, parameterTypes), MethodWrapper.wrap(handlerMethod));
    }

    static void updateBlockHandlerFor(Method originMethod, Class<?> handlerClass, String handlerName,
                                      Method handlerMethod) {
        BLOCK_HANDLER_METAMAP.put(HandlerMeta.handlerMetaOf(originMethod, handlerClass, handlerName),
                MethodWrapper.wrap(handlerMethod));
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
        FALLBACK_METAMAP.clear();
    }

    /**
     * Only for internal test.
     */
    static void clearBlockHandlerMap() {
        BLOCK_HANDLER_MAP.clear();
        BLOCK_HANDLER_METAMAP.clear();
    }

    /**
     * Only for internal test.
     */
    static void clearDefaultFallbackMap() {
        DEFAULT_FALLBACK_METAMAP.clear();
    }
}
