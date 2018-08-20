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
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for methods with {@link SentinelResource} annotation.
 *
 * @author Eric Zhao
 */
@Aspect
public class SentinelResourceAspect {

    private final Logger logger = LoggerFactory.getLogger(SentinelResourceAspect.class);

    @Pointcut("@annotation(com.alibaba.csp.sentinel.annotation.SentinelResource)")
    public void sentinelResourceAnnotationPointcut() {
    }

    @Around("sentinelResourceAnnotationPointcut()")
    public Object invokeResourceWithSentinel(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = getMethod(pjp);

        SentinelResource annotation = originMethod.getAnnotation(SentinelResource.class);
        if (annotation == null) {
            // Should not go through here.
            throw new IllegalStateException("Wrong state for SentinelResource annotation");
        }
        String resourceName = annotation.value();
        EntryType entryType = annotation.entryType();
        Entry entry = null;
        try {
            ContextUtil.enter(resourceName);
            entry = SphU.entry(resourceName, entryType);
            Object result = pjp.proceed();
            return result;
        } catch (BlockException ex) {
            return handleBlockException(pjp, annotation, ex);
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    private Object handleBlockException(ProceedingJoinPoint pjp, SentinelResource annotation, BlockException ex)
        throws Exception {
        // Execute fallback for degrading if configured.
        Object[] originArgs = pjp.getArgs();
        if (isDegradeFailure(ex)) {
            Method method = extractFallbackMethod(pjp, annotation.fallback());
            if (method != null) {
                return method.invoke(pjp.getTarget(), originArgs);
            }
        }
        // Execute block handler if configured.
        Method blockHandler = extractBlockHandlerMethod(pjp, annotation.blockHandler(), annotation.blockHandlerClass());
        if (blockHandler != null) {
            // Construct args.
            Object[] args = Arrays.copyOf(originArgs, originArgs.length + 1);
            args[args.length - 1] = ex;
            if (isStatic(blockHandler)) {
                return blockHandler.invoke(null, args);
            }
            return blockHandler.invoke(pjp.getTarget(), args);
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
        Method originMethod = getMethod(pjp);
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
        Method originMethod = getMethod(pjp);
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

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        return signature.getMethod();
    }
}
