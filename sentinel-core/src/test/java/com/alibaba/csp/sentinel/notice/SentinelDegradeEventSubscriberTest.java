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
package com.alibaba.csp.sentinel.notice;

import com.alibaba.csp.sentinel.notice.degrade.SentinelDegradeEventSubscriber;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class SentinelDegradeEventSubscriberTest {


    @Test
    public void testOnOpen() {
        EventSubscriber subscriber = new SentinelDegradeEventSubscriber();
        Event<DegradeRule> degradeRuleEvent = new Event<>(new DegradeRule(), "test", NoticeType.CIRCUIT_BREAK_OPEN.getType());
        //check sentinel-record.log
        subscriber.listen(degradeRuleEvent);
    }

    @Test
    public void testOnClose() {
        EventSubscriber subscriber = new SentinelDegradeEventSubscriber();
        Event<DegradeRule> degradeRuleEvent = new Event<>(new DegradeRule(), "test", NoticeType.CIRCUIT_BREAKER_CLOSE.getType());
        //check sentinel-record.log.
        subscriber.listen(degradeRuleEvent);
    }
}
