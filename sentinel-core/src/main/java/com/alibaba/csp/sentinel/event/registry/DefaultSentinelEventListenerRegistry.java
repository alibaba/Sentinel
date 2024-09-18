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
import com.alibaba.csp.sentinel.spi.Spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default impl for SentinelEventListenerRegistry.
 *
 * @author Daydreamer-ia
 */
@Spi(order=Spi.ORDER_LOWEST, isDefault = true)
public class DefaultSentinelEventListenerRegistry implements SentinelEventListenerRegistry {

    /**
     * mapping of event
     */
    private final Map<Class<? extends SentinelEvent>, List<SentinelEventListener>> listenerMap = new ConcurrentHashMap<>();

    @Override
    public void init(Properties properties) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void addSubscriber(SentinelEventListener listener) {
        List<Class<? extends SentinelEvent>> classes = listener.eventType();
        if (classes == null || classes.isEmpty()) {
            return;
        }
        for (Class<? extends SentinelEvent> eventClazz : classes) {
            List<SentinelEventListener> val = listenerMap.compute(eventClazz, (eventType, listeners) -> {
                if (listeners != null) {
                    return listeners;
                }
                return Collections.synchronizedList(new ArrayList<>());
            });
            val.add(listener);
        }
    }

    @Override
    public void removeSubscriber(Class<? extends SentinelEvent> eventType, SentinelEventListener listener) {
        List<SentinelEventListener> listeners = listenerMap.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public List<SentinelEventListener> getSentinelEventListener(Class<? extends SentinelEvent> event) {
        return listenerMap.getOrDefault(event, new ArrayList<>());
    }

}
