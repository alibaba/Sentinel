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

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class DefaultRuleStatusSubscriberTest {


    @Test
    public void testDefaultRuleStatusSubscriber() {
        DefaultRuleStatusSubscriber subscriber1 = new DefaultRuleStatusSubscriber();
        DegradeRule degradeRule = new DegradeRule("test-event-subscriber");
        //check sentinel-record.log
        Event<RuleStatusContentWrapper<DegradeRule>> event = new Event<>(new RuleStatusContentWrapper<>(RuleStatus.CIRCUIT_BREAK_START, degradeRule), DegradeRuleManager.class, System.currentTimeMillis());
        subscriber1.listen(event);
    }


}
