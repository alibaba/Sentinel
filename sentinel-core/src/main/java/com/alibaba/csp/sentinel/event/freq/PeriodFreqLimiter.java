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

package com.alibaba.csp.sentinel.event.freq;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stop event publish a period of time after publishing event.
 *
 * @author Daydreamer-ia
 */
public abstract class PeriodFreqLimiter implements SentinelEventFreqLimiter {

    /**
     * last trigger time.
     */
    private final Map<String, AtomicLong> lastTriggerTime = new ConcurrentHashMap<>();

    /**
     * limit period.
     */
    private final long limitPeriod;

    public PeriodFreqLimiter(long limitPeriod) {
        this.limitPeriod = limitPeriod;
    }

    @Override
    public boolean shouldHandle(SentinelEvent event) {
        AtomicLong dimensionLastTriggerTime = getLastTriggerTime(getLimitDimensionKey(event));
        long lastTriggerVal = dimensionLastTriggerTime.get();
        long period = System.currentTimeMillis() - lastTriggerVal;
        if (period > 0 && period > limitPeriod) {
            return dimensionLastTriggerTime.compareAndSet(lastTriggerVal, Math.max(dimensionLastTriggerTime.get(), System.currentTimeMillis()));
        }
        return false;
    }

    /**
     * Return dimension last trigger time.
     *
     * @param limitDimensionKey dimension key.
     * @return last trigger time
     */
    private AtomicLong getLastTriggerTime(String limitDimensionKey) {
        return lastTriggerTime.compute(limitDimensionKey, (dimension, lastTriggerTime) -> {
            if (lastTriggerTime != null) {
                return lastTriggerTime;
            }
            return new AtomicLong(System.currentTimeMillis() - limitPeriod);
        });
    }

    /**
     * Get the dimension which need to limit.
     *
     * @param event event
     * @return dimension
     */
    protected abstract String getLimitDimensionKey(SentinelEvent event);

}
