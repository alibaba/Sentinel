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
package com.alibaba.csp.sentinel.annotation.aspectj;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.MethodUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Some common functions for Sentinel annotation aspect.
 *
 * @author Eric Zhao
 * @author zhaoyuguang
 */
public abstract class AbstractSentinelAspectSupport {

    protected void traceException(Throwable ex) {
        Tracer.trace(ex);
    }

    protected void traceException(Throwable ex, SentinelResource annotation) {
        Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
        // The ignore list will be checked first.
        if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
            return;
        }
        if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
            traceException(ex);
        }
    }

    /**
     * Check whether the exception is in provided list of exception classes.
     *
     * @param ex         provided throwable
     * @param exceptions list of exceptions
     * @return true if it is in the list, otherwise false
     */
    protected boolean exceptionBelongsTo(Throwable ex, Class<? extends Throwable>[] exceptions) {
        if (exceptions == null) {
            return false;
        }
        for (Class<? extends Throwable> exceptionClass : exceptions) {
            if (exceptionClass.isAssignableFrom(ex.getClass())) {
                return true;
            }
        }
        return false;
    }

    protected String getResourceName(String resourceName, /*@NonNull*/ Method method) {
        // If resource name is present in annotation, use this value.
        if (StringUtil.isNotBlank(resourceName)) {
            return resourceName;
        }
        // Parse name of target method.
        return MethodUtil.resolveMethodName(method);
    }

    protected Object handleFallback(ProceedingJoinPoint pjp, SentinelResource annotation, Throwable ex)
        throws Throwable {
        return handleFallback(pjp, annotation.fallback(), annotation.defaultFallback(), annotation.fallbackClass(), ex);
    }

    protected Object handleFallback(ProceedingJoinPoint pjp, String fallback, String defaultFallback,
                                    Class<?>[] fallbackClass, Throwable ex) throws Throwable {
        Object[] originArgs = pjp.getArgs();

        // Execute fallback function if configured.
        Method fallbackMethod = extractFallbackMethod(pjp, fallback, fallbackClass);
        if (fallbackMethod != null) {
            // Construct args.
            int paramCount = fallbackMethod.getParameterTypes().length;
            Object[] args;
            if (paramCount == originArgs.length) {
                args = originArgs;
            } else {
                args = Arrays.copyOf(originArgs, originArgs.length + 1);
                args[args.length - 1] = ex;
            }

            return invoke(pjp, fallbackMethod, args);
        }
        // If fallback is absent, we'll try the defaultFallback if provided.
        return handleDefaultFallback(pjp, defaultFallback, fallbackClass, ex);
    }

    protected Object handleDefaultFallback(ProceedingJoinPoint pjp, String defaultFallback,
                                           Class<?>[] fallbackClass, Throwable ex) throws Throwable {
        // Execute the default fallback function if configured.
        Method fallbackMethod = extractDefaultFallbackMethod(pjp, defaultFallback, fallbackClass);
        if (fallbackMethod != null) {
            // Construct args.
            Object[] args = fallbackMethod.getParameterTypes().length == 0 ? new Object[0] : new Object[] {ex};
            return invoke(pjp, fallbackMethod, args);
        }

        // If no any fallback is present, then directly throw the exception.
        throw ex;
    }

    protected Object handleBlockException(ProceedingJoinPoint pjp, SentinelResource annotation, BlockException ex)
        throws Throwable {

        // Execute block handler if configured.
        Method blockHandlerMethod = extractBlockHandlerMethod(pjp, annotation.blockHandler(),
            annotation.blockHandlerClass());
        if (blockHandlerMethod != null) {
            Object[] originArgs = pjp.getArgs();
            // Construct args.
            Object[] args = Arrays.copyOf(originArgs, originArgs.length + 1);
            args[args.length - 1] = ex;
            return invoke(pjp, blockHandlerMethod, args);
        }

        // If no block handler is present, then go to fallback.
        return handleFallback(pjp, annotation, ex);
    }

    private Object invoke(ProceedingJoinPoint pjp, Method method, Object[] args) throws Throwable {
        try {
            if (!method.isAccessible()) {
                makeAccessible(method);
            }
            if (isStatic(method)) {
                return method.invoke(null, args);
            }
            return method.invoke(pjp.getTarget(), args);
        } catch (InvocationTargetException e) {
            // throw the actual exception
            throw e.getTargetException();
        }
    }

    /**
     * Make the given method accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     * @param method the method to make accessible
     * @see java.lang.reflect.Method#setAccessible
     */
    private static void makeAccessible(Method method) {
        boolean isNotPublic = !Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers());
        if (isNotPublic && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    private Method extractFallbackMethod(ProceedingJoinPoint pjp, String fallbackName, Class<?>[] locationClass) {
        if (StringUtil.isBlank(fallbackName)) {
            return null;
        }
        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz = mustStatic ? locationClass[0] : pjp.getTarget().getClass();
        MethodWrapper m = ResourceMetadataRegistry.lookupFallback(clazz, fallbackName);
        if (m == null) {
            // First time, resolve the fallback.
            Method method = resolveFallbackInternal(pjp, fallbackName, clazz, mustStatic);
            // Cache the method instance.
            ResourceMetadataRegistry.updateFallbackFor(clazz, fallbackName, method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method extractDefaultFallbackMethod(ProceedingJoinPoint pjp, String defaultFallback,
                                                Class<?>[] locationClass) {
        if (StringUtil.isBlank(defaultFallback)) {
            SentinelResource annotationClass = pjp.getTarget().getClass().getAnnotation(SentinelResource.class);
            if (annotationClass != null && StringUtil.isNotBlank(annotationClass.defaultFallback())) {
                defaultFallback = annotationClass.defaultFallback();
                if (locationClass == null || locationClass.length < 1) {
                    locationClass = annotationClass.fallbackClass();
                }
            } else {
                return null;
            }
        }
        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz = mustStatic ? locationClass[0] : pjp.getTarget().getClass();

        MethodWrapper m = ResourceMetadataRegistry.lookupDefaultFallback(clazz, defaultFallback);
        if (m == null) {
            // First time, resolve the default fallback.
            Class<?> originReturnType = resolveMethod(pjp).getReturnType();
            // Default fallback allows two kinds of parameter list.
            // One is empty parameter list.
            Class<?>[] defaultParamTypes = new Class<?>[0];
            // The other is a single parameter {@link Throwable} to get relevant exception info.
            Class<?>[] paramTypeWithException = new Class<?>[] {Throwable.class};
            // We first find the default fallback with empty parameter list.
            Method method = findMethod(mustStatic, clazz, defaultFallback, originReturnType, defaultParamTypes);
            // If default fallback with empty params is absent, we then try to find the other one.
            if (method == null) {
                method = findMethod(mustStatic, clazz, defaultFallback, originReturnType, paramTypeWithException);
            }
            // Cache the method instance.
            ResourceMetadataRegistry.updateDefaultFallbackFor(clazz, defaultFallback, method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method resolveFallbackInternal(ProceedingJoinPoint pjp, /*@NonNull*/ String name, Class<?> clazz,
                                           boolean mustStatic) {
        Method originMethod = resolveMethod(pjp);
        // Fallback function allows two kinds of parameter list.
        Class<?>[] defaultParamTypes = originMethod.getParameterTypes();
        Class<?>[] paramTypesWithException = Arrays.copyOf(defaultParamTypes, defaultParamTypes.length + 1);
        paramTypesWithException[paramTypesWithException.length - 1] = Throwable.class;
        // We first find the fallback matching the signature of origin method.
        Method method = findMethod(mustStatic, clazz, name, originMethod.getReturnType(), defaultParamTypes);
        // If fallback matching the origin method is absent, we then try to find the other one.
        if (method == null) {
            method = findMethod(mustStatic, clazz, name, originMethod.getReturnType(), paramTypesWithException);
        }
        return method;
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

                RecordLog.info("Resolved method [{}] in class [{}]", name, clazz.getCanonicalName());
                return method;
            }
        }
        // Current class not found, find in the super classes recursively.
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return findMethod(mustStatic, superClass, name, returnType, parameterTypes);
        } else {
            String methodType = mustStatic ? " static" : "";
            RecordLog.warn("Cannot find{} method [{}] in class [{}] with parameters {}",
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
