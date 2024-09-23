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
package com.alibaba.csp.sentinel.event.factory;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.multicaster.SentinelEventMulticaster;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;

import java.util.Properties;

/**
 * Rule for multicaster created.
 *
 * @author Daydreamer-ia
 */
public interface SentinelEventMulticasterFactory {

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
     * Acquire multicaster.
     * It will return the default multicaster when no multicaster binding.
     *
     * @param event event
     * @return multicaster
     */
    SentinelEventMulticaster getSentinelEventMulticaster(Class<? extends SentinelEvent> event);

    /**
     * Add multicaster of event.
     *
     * @param clazz       event
     * @param multicaster multicaster
     * @return whether success
     */
    boolean addSentinelEventMulticaster(Class<? extends SentinelEvent> clazz,
                                        SentinelEventMulticaster multicaster);

    /**
     * Remote multicaster of event.
     *
     * @param clazz       event
     * @return whether success
     */
    boolean removeSentinelEventMulticaster(Class<? extends SentinelEvent> clazz);
}
