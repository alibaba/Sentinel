/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * <p>Registry for circuit breaker event observers.</p>
 *
 * @author Eric Zhao
 * @since 1.8.0
 */
public class EventObserverRegistry {

    private final Map<String, ObserverEntity> stateChangeObserverMap = new HashMap<>();

    /**
     * Register a circuit breaker state change observer.
     * <p> Notes. the observer will observe all the state change</p>
     * @param name observer name
     * @param observer a valid observer
     */
    public void addStateChangeObserver(String name, CircuitBreakerStateChangeObserver observer) {
        addStateChangeObserver(name, null, observer);
    }
    
    /**
     * Register a circuit breaker state change observer which observe the resource.
     *
     * @param name observer name
     * @param resource  resource name
     * @param observer a valid observer
     */
    public void addStateChangeObserver(String name, String resource, CircuitBreakerStateChangeObserver observer) {
        AssertUtil.notNull(name, "name cannot be null");
        AssertUtil.notNull(observer, "observer cannot be null");
        ObserverEntity observerEntity = new ObserverEntity(name, resource, observer);
        stateChangeObserverMap.put(name, observerEntity);
    }

    public boolean removeStateChangeObserver(String name) {
        AssertUtil.notNull(name, "name cannot be null");
        return stateChangeObserverMap.remove(name) != null;
    }

    /**
     * Get all registered state chane observers which observe the resource.
     *
     * @return all registered state chane observers which observe the resource
     */
    public List<CircuitBreakerStateChangeObserver> getStateChangeObservers(String resource) {
        Collection<ObserverEntity> observerEntities = stateChangeObserverMap.values();
        if (observerEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<CircuitBreakerStateChangeObserver> observers = new ArrayList<>();
        Iterator<ObserverEntity> iterator = observerEntities.iterator();
        while (iterator.hasNext()) {
            ObserverEntity observerEntity = iterator.next();
            if (observerEntity.isObserve(resource)) {
                observers.add(observerEntity.observer);
            }
        }
        return observers;
    }

    public static EventObserverRegistry getInstance() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {
        private static EventObserverRegistry instance = new EventObserverRegistry();
    }

    EventObserverRegistry() {}
    
    /**
     * the entity of {@link CircuitBreakerStateChangeObserver}
     */
    private final class ObserverEntity {
    
        /**
         * observer name
         */
        private final String name;
    
        /**
         * the resource name which the observer focus on
         */
        private final String resource;
    
        /**
         * the observer
         */
        private final CircuitBreakerStateChangeObserver observer;
    
        public ObserverEntity(String name, String resource, CircuitBreakerStateChangeObserver observer) {
            this.name = name;
            this.resource = resource;
            this.observer = observer;
        }
    
        /**
         * check if the observer entity is observe the resource.
         * if the entity's resource is null, mean it will observe all the changes.
         * @param resource the resource
         * @return the observe result
         */
        public boolean isObserve(String resource) {
            return this.resource == null || resource.equals(this.resource);
        }
    }
}
