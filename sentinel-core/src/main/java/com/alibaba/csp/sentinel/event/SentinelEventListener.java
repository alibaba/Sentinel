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

import com.alibaba.csp.sentinel.event.model.SentinelEvent;

import java.util.concurrent.Executor;

/**
 * Base listener.
 *
 * @author Daydreamer-ia
 */
public abstract class SentinelEventListener<T extends SentinelEvent> {

    /**
     * Min priority.
     */
    public static final Integer MIN_PRIORITY = Integer.MAX_VALUE;

    /**
     * Max priority.
     */
    public static final Integer MAX_PRIORITY = Integer.MIN_VALUE;

    /**
     * Callback when event published.
     *
     * @param event event msg.
     */
    public abstract void onEvent(T event);

    /**
     * Events of interest to the listener
     */
    public abstract Class<T> eventType();

    /**
     * Whether current listener handle the event asynchronously.
     */
    public Executor executor() {
        return null;
    }

    /**
     * Should it always handle the latest events and ignore expired ones.
     */
    public boolean alwaysLast() {
        return false;
    }

    /**
     * Priority, the less num, the more priority.
     */
    public int order() {
        return MIN_PRIORITY;
    }
}

