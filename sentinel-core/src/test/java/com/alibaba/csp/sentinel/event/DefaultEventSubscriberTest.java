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
package com.alibaba.csp.sentinel.event;

import com.alibaba.csp.sentinel.event.subscriber.DefaultEventSubscriber;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class DefaultEventSubscriberTest {


    @Test
    public void testDefaultRuleStatusSubscriber() {
        DefaultEventSubscriber subscriber1 = new DefaultEventSubscriber();
        DegradeRule degradeRule = new DegradeRule("test-event-subscriber");
        //check sentinel-record.log
        Event<DegradeRule> event = new Event<>(degradeRule, "DefaultEventSubscriberTest", EventType.CIRCUIT_BREAK_OPEN.getType());
        subscriber1.listen(event);
    }


}
