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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default impl for SentinelEventListenerRegistry.
 *
 * @author Daydreamer-ia
 */
public class DefaultSentinelEventListenerRegistry implements SentinelEventListenerRegistry {

    /**
     * mapping of event
     */
    private final Map<Class<? extends SentinelEvent>, List<SentinelEventListener<? extends SentinelEvent>>> listenerMap = new ConcurrentHashMap<>();

    @Override
    public void init(Properties properties) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void addSubscriber(SentinelEventListener<? extends SentinelEvent> listener) {
        List<SentinelEventListener<? extends SentinelEvent>> val = listenerMap.compute(listener.eventType(), (eventType, listeners) -> {
            if (listeners != null) {
                return listeners;
            }
            return Collections.synchronizedList(new ArrayList<>());
        });
        val.add(listener);
    }

    @Override
    public void removeSubscriber(SentinelEventListener<? extends SentinelEvent> listener) {
        List<SentinelEventListener<? extends SentinelEvent>> listeners = listenerMap.get(listener.eventType());
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public List<SentinelEventListener<? extends SentinelEvent>> getSentinelEventListener(Class<? extends SentinelEvent> event) {
        return listenerMap.getOrDefault(event, new ArrayList<>());
    }

}
