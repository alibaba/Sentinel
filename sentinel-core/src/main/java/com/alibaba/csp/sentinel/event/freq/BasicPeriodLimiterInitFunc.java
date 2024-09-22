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

import com.alibaba.csp.sentinel.event.SentinelEventBus;
import com.alibaba.csp.sentinel.event.freq.impl.AuthorityEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.ClusterFallbackPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.FlowEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.SysEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.ClusterFallbackEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.AuthorityBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.SystemBlockEvent;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.event.listener.BasicExportListener;

/**
 * Used to declare event limiter.
 *
 * @author Daydreamer-ia
 */
public class BasicPeriodLimiterInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        // init freq limiter for block event.
        if (SentinelEventBus.getInstance().enableEvent()) {
            // by origin
            SentinelEventBus.getInstance().addFreqLimiter(AuthorityBlockEvent.class,
                    new AuthorityEventPeriodFreqLimiter(getLimitPeriodTimeMs(AuthorityEventPeriodFreqLimiter.EVENT_LIMITER_CONFIG)));
            // by rule id and resource
            SentinelEventBus.getInstance().addFreqLimiter(FlowBlockEvent.class,
                    new FlowEventPeriodFreqLimiter(getLimitPeriodTimeMs(FlowEventPeriodFreqLimiter.EVENT_LIMITER_CONFIG)));
            // by sys metric
            SentinelEventBus.getInstance().addFreqLimiter(SystemBlockEvent.class,
                    new SysEventPeriodFreqLimiter(getLimitPeriodTimeMs(SysEventPeriodFreqLimiter.EVENT_LIMITER_CONFIG)));
            // by rule id and resource
            SentinelEventBus.getInstance().addFreqLimiter(ClusterFallbackEvent.class,
                    new ClusterFallbackPeriodFreqLimiter(getLimitPeriodTimeMs(ClusterFallbackPeriodFreqLimiter.EVENT_LIMITER_CONFIG)));

            // add basic event listener
            SentinelEventBus.getInstance().addListener(new BasicExportListener());
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
