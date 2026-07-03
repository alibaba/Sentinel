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
import java.util.HashMap;
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

    private final Map<String, CircuitBreakerStateChangeObserver> stateChangeObserverMap = new HashMap<>();

    /**
     * Register a circuit breaker state change observer.
     *
     * @param name observer name
     * @param observer a valid observer
     */
    public void addStateChangeObserver(String name, CircuitBreakerStateChangeObserver observer) {
        AssertUtil.notNull(name, "name cannot be null");
        AssertUtil.notNull(observer, "observer cannot be null");
        stateChangeObserverMap.put(name, observer);
    }

    public boolean removeStateChangeObserver(String name) {
        AssertUtil.notNull(name, "name cannot be null");
        return stateChangeObserverMap.remove(name) != null;
    }

    /**
     * Get all registered state chane observers.
     *
     * @return all registered state chane observers
     */
    public List<CircuitBreakerStateChangeObserver> getStateChangeObservers() {
        return new ArrayList<>(stateChangeObserverMap.values());
    }

    public static EventObserverRegistry getInstance() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {
        private static EventObserverRegistry instance = new EventObserverRegistry();
    }

    EventObserverRegistry() {}
}
