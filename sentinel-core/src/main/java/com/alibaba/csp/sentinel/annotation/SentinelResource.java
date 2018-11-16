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
package com.alibaba.csp.sentinel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.csp.sentinel.EntryType;

/**
 * The annotation indicates a definition of Sentinel resource.
 *
 * @author Eric Zhao
 * @since 0.1.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SentinelResource {

    /**
     * @return name of the Sentinel resource
     */
    String value() default "";;

    /**
     * @return the entry type (inbound or outbound), outbound by default
     */
    EntryType entryType() default EntryType.OUT;

    /**
     * @return name of the block exception function, empty by default
     */
    String blockHandler() default "";

    /**
     * The {@code blockHandler} is located in the same class with the original method by default.
     * However, if some methods share the same signature and intend to set the same block handler,
     * then users can set the class where the block handler exists. Note that the block handler method
     * must be static.
     *
     * @return the class where the block handler exists, should not provide more than one classes
     */
    Class<?>[] blockHandlerClass() default {};

    /**
     * @return name of the fallback function, empty by default
     */
    String fallback() default "";
}
