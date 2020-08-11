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
package com.alibaba.csp.sentinel.slots.block.flow.timeout;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A singleton holder of the timer for timeout.
 *
 * @author yunfeiyanggzq
 */
public class TimerHolder {

    private final static long DEFAULT_TICK_DURATION = 10;

    private static final HashedWheelTimer INSTANCE = new HashedWheelTimer(new NamedThreadFactory(
            "DefaultTimer" + DEFAULT_TICK_DURATION, true),
            DEFAULT_TICK_DURATION, TimeUnit.MILLISECONDS);

    private TimerHolder() {}

    /**
     * Get a singleton instance of {@link HashedWheelTimer}. <br>
     * The tick duration is {@link #DEFAULT_TICK_DURATION}.
     *
     * @return Timer
     */
    public static HashedWheelTimer getTimer() {
        return INSTANCE;
    }
}