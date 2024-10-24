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
package com.alibaba.csp.sentinel.event.model.impl;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;

/**
 * Event published when circuit breaker state changed.
 *
 * @author Daydreamer-ia
 */
public class CircuitBreakerStateEvent extends SentinelRuleEvent {

    /**
     * old state.
     */
    private final CircuitBreaker.State orginState;

    /**
     * new state.
     */
    private final CircuitBreaker.State nowState;

    public CircuitBreakerStateEvent(CircuitBreaker.State oldState, CircuitBreaker.State nowState, AbstractRule rule) {
        super(rule);
        this.nowState = nowState;
        this.orginState = oldState;
    }

    public CircuitBreaker.State getOrginState() {
        return orginState;
    }

    public CircuitBreaker.State getNowState() {
        return nowState;
    }
}
