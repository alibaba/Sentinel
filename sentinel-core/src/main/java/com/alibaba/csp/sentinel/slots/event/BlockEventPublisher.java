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
import com.alibaba.csp.sentinel.event.freq.impl.AuthorityEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.FlowEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.freq.impl.SysEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.block.AuthorityBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.SystemBlockEvent;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 * Ops to publish event.
 *
 * @author Daydreamer-ia
 */
public class BlockEventPublisher {

    /**
     * publish block event to listeners.
     *
     * @param resourceWrapper resource
     * @param blockException blockException
     */
    public static void publishBlockEvent(ResourceWrapper resourceWrapper, BlockException blockException) {
        if (blockException instanceof FlowException) {
            FlowBlockEvent flowBlockEvent = new FlowBlockEvent(resourceWrapper.getName(), blockException.getRule());
            SentinelEventBus.getInstance().publish(flowBlockEvent);
        }
        if (blockException instanceof SystemBlockException) {
            SystemBlockException systemBlockException = (SystemBlockException) blockException;
            SystemBlockEvent systemBlockEvent = new SystemBlockEvent(SystemRuleManager.getCurrentSysRule(), systemBlockException.getLimitType());
            SentinelEventBus.getInstance().publish(systemBlockEvent);
        }
        if (blockException instanceof AuthorityException) {
            AuthorityException authorityException = (AuthorityException) blockException;
            AuthorityBlockEvent authorityBlockEvent = new AuthorityBlockEvent(authorityException.getRuleLimitApp(), authorityException.getRule());
            SentinelEventBus.getInstance().publish(authorityBlockEvent);
        }
    }

}
