/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface SpiOrder {

    /**
     * Represents the lowest precedence.
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    /**
     * Represents the highest precedence.
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * The SPI precedence value. Lowest precedence by default.
     *
     * @return the precedence value
     */
    int value() default LOWEST_PRECEDENCE;
}
