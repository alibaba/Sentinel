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
import com.alibaba.csp.sentinel.event.multicaster.DefaultSentinelEventMulticaster;
import com.alibaba.csp.sentinel.event.multicaster.SentinelEventMulticaster;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;
import com.alibaba.csp.sentinel.spi.Spi;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default impl for SentinelEventMulticasterFactory.
 *
 * @author Daydreamer-ia
 */
@Spi(order=Spi.ORDER_LOWEST, isDefault = true)
public class DefaultSentinelEventMulticasterFactory implements SentinelEventMulticasterFactory {

    /**
     * mapping for multicaster.
     */
    private final Map<Class<? extends SentinelEvent>, SentinelEventMulticaster> multicasterMap = new ConcurrentHashMap<>();

    /**
     * global multicaster for default.
     */
    private final DefaultSentinelEventMulticaster globalSentinelEventMulticaster = new DefaultSentinelEventMulticaster();

    private Properties properties;

    private SentinelEventListenerRegistry sentinelEventListenerRegistry;

    @Override
    public void init(Properties properties, SentinelEventListenerRegistry registry) {
        this.properties = properties;
        this.sentinelEventListenerRegistry = registry;
        this.globalSentinelEventMulticaster.init(properties, registry);
    }

    @Override
    public void destroy() {
        // close for all custom multicaster
        for (SentinelEventMulticaster value : multicasterMap.values()) {
            if (value != null) {
                value.destroy();
            }
        }
        // for global multicaster
        globalSentinelEventMulticaster.destroy();
    }

    @Override
    public SentinelEventMulticaster getSentinelEventMulticaster(Class<? extends SentinelEvent> event) {
        return multicasterMap.getOrDefault(event, globalSentinelEventMulticaster);
    }

    @Override
    public boolean addSentinelEventMulticaster(Class<? extends SentinelEvent> clazz, SentinelEventMulticaster multicaster) {
        multicaster.init(properties, this.sentinelEventListenerRegistry);
        multicasterMap.put(clazz, multicaster);
        return true;
    }

    @Override
    public boolean removeSentinelEventMulticaster(Class<? extends SentinelEvent> clazz) {
        multicasterMap.remove(clazz);
        return true;
    }

}
