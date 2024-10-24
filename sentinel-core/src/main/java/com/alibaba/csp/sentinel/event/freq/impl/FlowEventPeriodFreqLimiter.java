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
package com.alibaba.csp.sentinel.event.freq.impl;

import com.alibaba.csp.sentinel.event.freq.PeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.SentinelRuleEvent;

/**
 * Base on rule id to finish frequency limit, event should be class or subclass of {@link SentinelRuleEvent}.
 *
 * @author Daydreamer-ia
 */
public class FlowEventPeriodFreqLimiter extends PeriodFreqLimiter {

    /**
     * period limit config.
     */
    public static final String EVENT_LIMITER_CONFIG = PeriodFreqLimiter.EVENT_LIMITER_CONFIG_PREFIX + "flow";

    public FlowEventPeriodFreqLimiter(long limitPeriod) {
        super(limitPeriod);
    }

    @Override
    protected String getLimitDimensionKey(SentinelEvent event) {
        if (event instanceof FlowBlockEvent) {
            FlowBlockEvent flowBlockEvent = (FlowBlockEvent) event;
            return flowBlockEvent.getResourceName() + ":" + flowBlockEvent.getRule().getId();
        }
        // degrade to limit global event.
        return "unknown";
    }

}
