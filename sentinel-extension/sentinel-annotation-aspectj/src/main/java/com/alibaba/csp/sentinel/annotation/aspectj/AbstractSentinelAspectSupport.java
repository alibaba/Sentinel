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

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.util.MethodUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Some common functions for Sentinel annotation aspect.
 */
public abstract class AbstractSentinelAspectSupport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String getResourceName(String resourceName, Method method) {
        // If resource name is present in annotation, use this value.
        if (StringUtil.isNotBlank(resourceName)) {
            return resourceName;
        }
        // Parse name of target method.
        return MethodUtil.resolveMethodName(method);
    }

    protected Object handleBlockException(ProceedingJoinPoint pjp, SentinelResource annotation, BlockException ex)
        throws Exception {
        return handleBlockException(pjp, annotation.fallback(), annotation.blockHandler(),
            annotation.blockHandlerClass(), ex);
    }

    protected Object handleBlockException(ProceedingJoinPoint pjp, String fallback, String blockHandler,
                                          Class<?>[] blockHandlerClass, BlockException ex) throws Exception {
        // Execute fallback for degrading if configured.
        Object[] originArgs = pjp.getArgs();
        if (isDegradeFailure(ex)) {
            Method method = extractFallbackMethod(pjp, fallback);
            if (method != null) {
                return method.invoke(pjp.getTarget(), originArgs);
            }
        }
        // Execute block handler if configured.
        Method blockHandlerMethod = extractBlockHandlerMethod(pjp, blockHandler, blockHandlerClass);
        if (blockHandlerMethod != null) {
            // Construct args.
            Object[] args = Arrays.copyOf(originArgs, originArgs.length + 1);
            args[args.length - 1] = ex;
            if (isStatic(blockHandlerMethod)) {
                return blockHandlerMethod.invoke(null, args);
            }
            return blockHandlerMethod.invoke(pjp.getTarget(), args);
        }
        // If no block handler is present, then directly throw the exception.
        throw ex;
    }

    private boolean isDegradeFailure(/*@NonNull*/ BlockException ex) {
        return ex instanceof DegradeException;
    }

    private Method extractFallbackMethod(ProceedingJoinPoint pjp, String fallbackName) {
        if (StringUtil.isBlank(fallbackName)) {
            return null;
        }
        Class<?> clazz = pjp.getTarget().getClass();
        MethodWrapper m = ResourceMetadataRegistry.lookupFallback(clazz, fallbackName);
        if (m == null) {
            // First time, resolve the fallback.
            Method method = resolveFallbackInternal(pjp, fallbackName);
            // Cache the method instance.
            ResourceMetadataRegistry.updateFallbackFor(clazz, fallbackName, method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method resolveFallbackInternal(ProceedingJoinPoint pjp, /*@NonNull*/ String name) {
        Method originMethod = resolveMethod(pjp);
        Class<?>[] parameterTypes = originMethod.getParameterTypes();
        return findMethod(false, pjp.getTarget().getClass(), name, originMethod.getReturnType(), parameterTypes);
    }

    private Method extractBlockHandlerMethod(ProceedingJoinPoint pjp, String name, Class<?>[] locationClass) {
        if (StringUtil.isBlank(name)) {
            return null;
        }

        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz;
        if (mustStatic) {
            clazz = locationClass[0];
        } else {
            // By default current class.
            clazz = pjp.getTarget().getClass();
        }
        MethodWrapper m = ResourceMetadataRegistry.lookupBlockHandler(clazz, name);
        if (m == null) {
            // First time, resolve the block handler.
            Method method = resolveBlockHandlerInternal(pjp, name, clazz, mustStatic);
            // Cache the method instance.
            ResourceMetadataRegistry.updateBlockHandlerFor(clazz, name, method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method resolveBlockHandlerInternal(ProceedingJoinPoint pjp, /*@NonNull*/ String name, Class<?> clazz,
                                               boolean mustStatic) {
        Method originMethod = resolveMethod(pjp);
        Class<?>[] originList = originMethod.getParameterTypes();
        Class<?>[] parameterTypes = Arrays.copyOf(originList, originList.length + 1);
        parameterTypes[parameterTypes.length - 1] = BlockException.class;
        return findMethod(mustStatic, clazz, name, originMethod.getReturnType(), parameterTypes);
    }

    private boolean checkStatic(boolean mustStatic, Method method) {
        return !mustStatic || isStatic(method);
    }

    private Method findMethod(boolean mustStatic, Class<?> clazz, String name, Class<?> returnType,
                              Class<?>... parameterTypes) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (name.equals(method.getName()) && checkStatic(mustStatic, method)
                && returnType.isAssignableFrom(method.getReturnType())
                && Arrays.equals(parameterTypes, method.getParameterTypes())) {

                logger.info("Resolved method [{}] in class [{}]", name, clazz.getCanonicalName());
                return method;
            }
        }
        // Current class not found, find in the super classes recursively.
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return findMethod(mustStatic, superClass, name, returnType, parameterTypes);
        } else {
            String methodType = mustStatic ? " static" : "";
            logger.error("Cannot find{} method [{}] in class [{}] with parameters {}",
                methodType, name, clazz.getCanonicalName(), Arrays.toString(parameterTypes));
            return null;
        }
    }

    private boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    protected Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        Method method = getDeclaredMethodFor(targetClass, signature.getName(),
            signature.getMethod().getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("Cannot resolve target method: " + signature.getMethod().getName());
        }
        return method;
    }

    /**
     * Get declared method with provided name and parameterTypes in given class and its super classes.
     * All parameters should be valid.
     *
     * @param clazz          class where the method is located
     * @param name           method name
     * @param parameterTypes method parameter type list
     * @return resolved method, null if not found
     */
    private Method getDeclaredMethodFor(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethodFor(superClass, name, parameterTypes);
            }
        }
        return null;
    }
}
