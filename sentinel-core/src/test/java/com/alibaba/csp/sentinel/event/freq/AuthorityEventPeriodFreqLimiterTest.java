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

import com.alibaba.csp.sentinel.event.freq.impl.AuthorityEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.SentinelRuleEvent;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import org.junit.*;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Daydreamer-ia
 */
public class AuthorityEventPeriodFreqLimiterTest {

    private AuthorityEventPeriodFreqLimiter limiter;

    @Before
    public void setUp() {
        // 设置一个限制周期，例如1000毫秒
        long limitPeriod = 1000L;
        limiter = new AuthorityEventPeriodFreqLimiter(limitPeriod);
    }

    @Test
    public void testGetLimitDimensionKey_WithSentinelRuleEvent() throws InterruptedException {
        // 设定一个规则事件
        AbstractRule rule = Mockito.mock(AbstractRule.class);
        Mockito.when(rule.getLimitApp()).thenReturn("testApp");

        SentinelRuleEvent event = Mockito.mock(SentinelRuleEvent.class);
        Mockito.when(event.getRule()).thenReturn(rule);

        boolean b = limiter.shouldHandle(event);
        assertTrue(b);

        b = limiter.shouldHandle(event);
        assertFalse(b);

        // 等待1s
        Thread.sleep(1000);
        b = limiter.shouldHandle(event);
        assertTrue(b);

        limiter.shouldHandle(event);
        b = limiter.shouldHandle(event);
        assertFalse(b);

    }

}
