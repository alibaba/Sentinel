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

package com.alibaba.csp.sentinel.event.registry;

import com.alibaba.csp.sentinel.event.SentinelEventListener;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;

import java.util.List;
import java.util.Properties;

/**
 * Base registry for listeners.
 *
 * @author Daydreamer-ia
 */
public interface SentinelEventListenerRegistry {

    /**
     * Init method.
     *
     * @param properties properties
     */
    void init(Properties properties);

    /**
     * Callback for close.
     */
    void destroy();

    /**
     * Add listener.
     *
     * @param listener listener
     */
    void addSubscriber(SentinelEventListener<? extends SentinelEvent> listener);

    /**
     * Remove listener.
     *
     * @param listener listener
     */
    void removeSubscriber(SentinelEventListener<? extends SentinelEvent> listener);

    /**
     * Acquire all listeners of specify event type.
     *
     * @param event event
     * @return listeners
     */
    List<SentinelEventListener<? extends SentinelEvent>> getSentinelEventListener(Class<? extends SentinelEvent> event);

}
