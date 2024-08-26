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

package com.alibaba.csp.sentinel.event;

import com.alibaba.csp.sentinel.event.factory.SentinelEventMulticasterFactory;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.multicaster.SentinelEventMulticaster;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.Properties;

/**
 * Ops of sentinel event.
 *
 * @author Daydreamer-ia
 */
public class SentinelEventBus {

    /**
     * INSTANCE.
     */
    private static final SentinelEventBus INSTANCE = new SentinelEventBus();

    /**
     * mapping of event and multicaster.
     */
    private final SentinelEventMulticasterFactory eventMulticasterFactory;

    /**
     * event registry.
     */
    private final SentinelEventListenerRegistry sentinelEventListenerRegistry;

    /**
     * whether to enabled event support.
     */
    private volatile boolean enabledEvent = false;


    private SentinelEventBus() {
        // init
        SentinelEventMulticasterFactory factory = SpiLoader.of(SentinelEventMulticasterFactory.class).loadHighestPriorityInstance();
        SentinelEventListenerRegistry registry = SpiLoader.of(SentinelEventListenerRegistry.class).loadHighestPriorityInstance();
        this.eventMulticasterFactory = factory;
        this.sentinelEventListenerRegistry = registry;
        // only when both of registry and factory are not null.
        enabledEvent = (factory != null && registry != null);
        init();
        RecordLog.info("EnabledEventFeature: {} | SentinelEventMulticasterFactory: {} | SentinelEventListenerRegistry: {}",
                this.enabledEvent, this.eventMulticasterFactory, this.sentinelEventListenerRegistry);
        // register hook for destroy
        if (this.enabledEvent) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
        }
    }

    /**
     * Init ops for registry and factory
     */
    private void init() {
        this.sentinelEventListenerRegistry.init(getRegistryProperties());
        this.eventMulticasterFactory.init(getFactoryProperties(), this.sentinelEventListenerRegistry);
    }

    /**
     * Close ops for components.
     */
    private void destroy() {
        this.sentinelEventListenerRegistry.destroy();
        this.eventMulticasterFactory.destroy();
    }

    /**
     * Get factory properties.
     *
     * @return properties
     */
    private Properties getFactoryProperties() {
        // load properties from sys properties if necessary
        return new Properties(System.getProperties());
    }

    /**
     * Get registry properties.
     *
     * @return properties
     */
    private Properties getRegistryProperties() {
        // load properties from sys properties if necessary
        return new Properties(System.getProperties());
    }

    /**
     * Get instance of bus.
     *
     * @return event bus.
     */
    public static SentinelEventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Register multicaster of event.
     *
     * @param clazz       event
     * @param multicaster multicaster
     * @return whether success to add
     */
    public boolean registerMulticaster(Class<? extends SentinelEvent> clazz, SentinelEventMulticaster multicaster) {
        return eventMulticasterFactory.addSentinelEventMulticaster(clazz, multicaster);
    }

    /**
     * Remove multicaster of event.
     *
     * @param clazz event
     * @return whether success to remove
     */
    public boolean removerMulticaster(Class<? extends SentinelEvent> clazz) {
        return eventMulticasterFactory.removeSentinelEventMulticaster(clazz);
    }

    /**
     * Get multicaster of event.
     *
     * @param clazz event
     * @return multicaster
     */
    public SentinelEventMulticaster getMulticaster(Class<? extends SentinelEvent> clazz) {
        return eventMulticasterFactory.getSentinelEventMulticaster(clazz);
    }

    /**
     * Publish event.
     *
     * @param event event
     * @return whether success to publish
     */
    public boolean publish(SentinelEvent event) {
        return eventMulticasterFactory.getSentinelEventMulticaster(event.getClass()).publish(event);
    }

    /**
     * Add listener.
     *
     * @param listener listener
     */
    public void addListener(SentinelEventListener<? extends SentinelEvent> listener) {
        sentinelEventListenerRegistry.addSubscriber(listener);
    }

    /**
     * Remove listener.
     *
     * @param listener listener
     */
    public void removeListener(SentinelEventListener<? extends SentinelEvent> listener) {
        sentinelEventListenerRegistry.removeSubscriber(listener);
    }

}
