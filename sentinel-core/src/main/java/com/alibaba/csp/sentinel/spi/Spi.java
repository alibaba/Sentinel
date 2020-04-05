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
package com.alibaba.csp.sentinel.spi;

import java.lang.annotation.*;

/**
 * Annotation for Provider class of SPI.
 *
 * @see SpiLoader
 * @author cdfive
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Spi {

    /**
     * Alias name of Provider class
     */
    String value() default "";

    /**
     * Whether create singleton instance
     */
    boolean isSingleton() default true;

    /**
     * Whether is the default Provider
     */
    boolean isDefault() default false;

    /**
     * Order priority of Provider class
     */
    int order() default 0;

    int ORDER_HIGHEST = Integer.MIN_VALUE;

    int ORDER_LOWEST = Integer.MAX_VALUE;
}
