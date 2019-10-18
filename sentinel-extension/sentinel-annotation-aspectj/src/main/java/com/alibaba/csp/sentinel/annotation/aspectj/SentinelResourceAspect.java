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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;

/**
 * Aspect for methods with {@link SentinelResource} annotation.
 * SentinelResource Aop切面类
 *
 * @author Eric Zhao
 */
@Aspect
public class SentinelResourceAspect extends AbstractSentinelAspectSupport {

    @Pointcut("@annotation(com.alibaba.csp.sentinel.annotation.SentinelResource)")
    public void sentinelResourceAnnotationPointcut() {
    }

    @Around("sentinelResourceAnnotationPointcut()")
    public Object invokeResourceWithSentinel(ProceedingJoinPoint pjp) throws Throwable {
        // 获取到方法
        Method originMethod = resolveMethod(pjp);
        // 获取方法上的注解对象
        SentinelResource annotation = originMethod.getAnnotation(SentinelResource.class);
        if (annotation == null) {
            // Should not go through here.
            throw new IllegalStateException("Wrong state for SentinelResource annotation");
        }
        // 获取资源名，如果没设置资源名则使用"类全限定名:方法名()"作为资源名
        String resourceName = getResourceName(annotation.value(), originMethod);
        // 进入类型IN or OUT
        EntryType entryType = annotation.entryType();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, entryType, 1, pjp.getArgs());
            Object result = pjp.proceed();
            System.out.println("-------------------------------------------------------------");
            System.out.println(resourceName);
            if (resourceName.equalsIgnoreCase("test")) {
                throw new FlowException("block error");
            }
            return result;
        } catch (BlockException ex) {
            // 如果出现BlockException异常，是否有配置blockHandler方法，没有则查看是否有配置fallback方法，没有则查看是否有配置defaultFallback，没有则抛出异常
            return handleBlockException(pjp, annotation, ex);
        } catch (Throwable ex) {
            Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
            // The ignore list will be checked first.
            // 这个异常是否是属于被忽略异常，属于则直接抛出异常
            if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
                throw ex;
            }
            // 则查看是否有配置fallback方法，没有则查看是否有配置defaultFallback，没有则抛出异常
            if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
                traceException(ex);
                return handleFallback(pjp, annotation, ex);
            }

            // No fallback function can handle the exception, so throw it out.
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, pjp.getArgs());
            }
        }
    }
}
