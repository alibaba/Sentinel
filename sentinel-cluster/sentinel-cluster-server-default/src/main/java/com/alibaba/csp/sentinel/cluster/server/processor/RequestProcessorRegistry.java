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
package com.alibaba.csp.sentinel.cluster.server.processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class RequestProcessorRegistry {

    private static final Map<Integer, RequestProcessor> PROCESSOR_MAP = new ConcurrentHashMap<>();

    public static RequestProcessor getProcessor(int type) {
        return PROCESSOR_MAP.get(type);
    }

    public static void addProcessorIfAbsent(int type, RequestProcessor processor) {
        // TBD: use putIfAbsent in JDK 1.8.
        if (PROCESSOR_MAP.containsKey(type)) {
            return;
        }
        PROCESSOR_MAP.put(type, processor);
    }

    public static void addProcessor(int type, RequestProcessor processor) {
        AssertUtil.notNull(processor, "processor cannot be null");
        PROCESSOR_MAP.put(type, processor);
    }

    private RequestProcessorRegistry() {}
}
