/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
import java.util.Objects;

/**
 * `blockHandler`, `fallback` and `defaultFallback` handler metadata, describing the features of a handler method.
 * It helps locate the handler method in the registry.
 *
 * @author dowenliu-xyz(hawkdowen@hotmail.com)
 */
final class HandlerMeta {
    private final Class<?> handlerClass;
    private final Class<?> returnType;
    private final String handlerName;
    private final Class<?>[] parameterTypes;

    /**
     * Create a handler metadata.
     *
     * @param originMethod the origin method which is annotated by `@SentinelResource`
     * @param handlerClass the class of the handler method
     * @param handlerName  the name of the handler method
     * @return the handler metadata
     * @throws IllegalArgumentException if `originMethod` is null
     */
    static HandlerMeta handlerMetaOf(Method originMethod, Class<?> handlerClass, String handlerName) {
        if (originMethod == null) {
            throw new IllegalArgumentException("originMethod should not be null");
        }
        return new HandlerMeta(
                handlerClass, originMethod.getReturnType(), handlerName, originMethod.getParameterTypes());
    }

    private HandlerMeta(Class<?> handlerClass, Class<?> returnType, String handlerName, Class<?>[] parameterTypes) {
        this.handlerClass = handlerClass;
        this.returnType = returnType;
        this.handlerName = handlerName;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerMeta that = (HandlerMeta) o;
        return Objects.equals(handlerClass, that.handlerClass) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(handlerName, that.handlerName) &&
                Objects.deepEquals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handlerClass, returnType, handlerName, Arrays.hashCode(parameterTypes));
    }
}
