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
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * Just Log the Degrade Event
 *
 * @author lianglin
 * @since 1.7.0
 */
public class SentinelDegradeEventSubscriber extends AbstractDegradeEventSubscriber {

    @Override
    public void onOpen(Event<DegradeRule> event) {
        RecordLog.info("[SentinelDegradeEventSubscriber] receive Circuit Breaker Open event, the trigger rule is {0}", event.getData());

    }

    @Override
    public void onClose(Event<DegradeRule> event) {
        RecordLog.info("[SentinelDegradeEventSubscriber] receive Circuit Breaker Closed event, the trigger rule is {0}", event.getData());

    }
}
