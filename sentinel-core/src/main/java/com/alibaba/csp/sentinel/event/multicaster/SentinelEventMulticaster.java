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

package com.alibaba.csp.sentinel.event.multicaster;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;

import java.util.Properties;

/**
 * Base multicaster for event.
 *
 * @author Daydreamer-ia
 */
public interface SentinelEventMulticaster {

    /**
     * Init method.
     *
     * @param properties properties
     * @param registry   registry
     */
    void init(Properties properties, SentinelEventListenerRegistry registry);

    /**
     * Callback for close.
     */
    void destroy();

    /**
     * Publish event.
     *
     * @param event event
     * @return whether success.
     */
    boolean publish(SentinelEvent event);

}
