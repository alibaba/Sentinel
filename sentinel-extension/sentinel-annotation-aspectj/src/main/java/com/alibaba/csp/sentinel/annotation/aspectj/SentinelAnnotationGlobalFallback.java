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

/**
 * Global fallback handler for {@link com.alibaba.csp.sentinel.annotation.SentinelResource} annotations.
 * <p>
 * When set on {@link SentinelResourceAspect}, this handler serves as the last-resort fallback
 * for all {@code @SentinelResource}-annotated methods, even when neither {@code fallback}
 * nor {@code defaultFallback} is specified in the annotation.
 * <p>
 * The global fallback is invoked only after both per-method fallback and default fallback
 * have been exhausted (i.e., not configured or not found).
 *
 * @author EvanYao826
 * @since 1.8.8
 */
public interface SentinelAnnotationGlobalFallback {

    /**
     * Handle the fallback when a {@code @SentinelResource}-annotated method is blocked
     * or throws a traced exception.
     *
     * @param originalMethod the original annotated method
     * @param args           the arguments passed to the original method
     * @param t              the exception that triggered the fallback (may be a {@link
     *                       com.alibaba.csp.sentinel.slots.block.BlockException} or a traced business exception)
     * @return the fallback result
     * @throws Throwable if the fallback itself fails
     */
    Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable;
}
