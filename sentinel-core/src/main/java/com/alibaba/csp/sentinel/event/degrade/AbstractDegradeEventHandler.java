/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.degrade;

import com.alibaba.csp.sentinel.event.Event;
import com.alibaba.csp.sentinel.event.EventHandler;
import com.alibaba.csp.sentinel.event.EventType;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * @author lianglin
 * @since  1.7.1
 */
public abstract class AbstractDegradeEventHandler implements EventHandler<DegradeRule> {

    @Override
    public final void handle(Event<DegradeRule> event) {
        if (event == null) {
            return;
        }
        if (event.getType() == EventType.CIRCUIT_BREAK_OPEN.getType()) {
            onOpen(event);
        } else if (event.getType() == EventType.CIRCUIT_BREAKER_CLOSE.getType()) {
            onClose(event);
        }

    }

    /**
     * handle {@link EventType.CIRCUIT_BREAK_OPEN} event
     *
     * @param event
     */
    abstract void onOpen(Event<DegradeRule> event);

    /**
     * handle {@link EventType.CIRCUIT_BREAKER_CLOSE} event
     *
     * @param event
     */
    abstract void onClose(Event<DegradeRule> event);
}
