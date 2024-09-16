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

package com.alibaba.csp.sentinel.slots.event;

import com.alibaba.csp.sentinel.event.SentinelEventBus;
import com.alibaba.csp.sentinel.event.freq.impl.BaseRuleIdPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.BaseSysMetricPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.SystemBlockEvent;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 * Ops to publish event.
 *
 * @author Daydreamer-ia
 */
public class BlockEventPublisher {

    static {
        // init freq limiter for block event.
        if (SentinelEventBus.getInstance().enableEvent()) {
            SentinelEventBus.getInstance().addFreqLimiter(FlowBlockEvent.class, new BaseRuleIdPeriodFreqLimiter(10000));
            SentinelEventBus.getInstance().addFreqLimiter(SystemBlockEvent.class, new BaseSysMetricPeriodFreqLimiter(10000));
        }
    }

    /**
     * publish block event to listeners.
     *
     * @param blockException blockException
     */
    public static void publishBlockEvent(BlockException blockException) {
        if (blockException instanceof FlowException) {
            FlowBlockEvent flowBlockEvent = new FlowBlockEvent(blockException.getRule());
            SentinelEventBus.getInstance().publish(flowBlockEvent);
        }
        if (blockException instanceof SystemBlockException) {
            SystemBlockException systemBlockException = (SystemBlockException) blockException;
            SystemBlockEvent systemBlockEvent = new SystemBlockEvent(SystemRuleManager.getCurrentSysRule(), systemBlockException.getLimitType());
            SentinelEventBus.getInstance().publish(systemBlockEvent);
        }
    }

}
