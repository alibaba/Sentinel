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
package com.alibaba.csp.sentinel.annotation.aspectj.integration.service;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author Eric Zhao
 */
public class FooUtil {

    public static final int BLOCK_FLAG = 88888;
    public static final String FALLBACK_DEFAULT_RESULT = "fallback";

    public static int globalBlockHandler(BlockException ex) {
        System.out.println("Oops: " + ex.getClass().getSimpleName());
        return BLOCK_FLAG;
    }

    public static String globalDefaultFallback(Throwable t) {
        System.out.println("Fallback caught: " + t.getClass().getSimpleName());
        return FALLBACK_DEFAULT_RESULT;
    }
}
