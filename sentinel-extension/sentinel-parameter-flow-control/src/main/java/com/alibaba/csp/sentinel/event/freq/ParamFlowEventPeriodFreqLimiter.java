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

import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;

/**
 * Init func for event.
 *
 * @author Daydreamer-ia
 */
public class ParamFlowEventPeriodFreqLimiter extends PeriodFreqLimiter {

    /**
     * period limit config.
     */
    public static final String EVENT_LIMITER_CONFIG = PeriodFreqLimiter.EVENT_LIMITER_CONFIG_PREFIX + "param-flow";

    public ParamFlowEventPeriodFreqLimiter(long limitPeriod) {
        super(limitPeriod);
    }

    @Override
    protected String getLimitDimensionKey(SentinelEvent event) {
        if (event instanceof ParamFlowBlockEvent) {
            ParamFlowBlockEvent paramFlowBlockEvent = (ParamFlowBlockEvent) event;
            return paramFlowBlockEvent.getResourceName() + ":" + paramFlowBlockEvent.getRule().getId();
        }
        // degrade to global event.
        return "unknown";
    }
}
