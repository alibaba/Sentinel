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
package com.alibaba.csp.sentinel.init;

import com.alibaba.csp.sentinel.event.SentinelEventBus;
import com.alibaba.csp.sentinel.event.freq.ParamFlowEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.slots.event.listener.ParamFlowExportListener;

/**
 * Init method for event.
 *
 * @author Daydreamer-ia
 */
public class ParamFlowPeriodLimiterInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        if (SentinelEventBus.getInstance().enableEvent()) {
            SentinelEventBus.getInstance().addFreqLimiter(ParamFlowBlockEvent.class,
                    new ParamFlowEventPeriodFreqLimiter(getLimitPeriodTimeMs(ParamFlowEventPeriodFreqLimiter.EVENT_LIMITER_CONFIG)));

            // add basic event listener
            SentinelEventBus.getInstance().addListener(new ParamFlowExportListener());
        }
    }

    /**
     * get specify config key for period limit.
     *
     * @param config config key
     * @return limit time
     */
    private long getLimitPeriodTimeMs(String config) {
        return Long.parseLong(System.getProperty(config, "60000"));
    }

}
