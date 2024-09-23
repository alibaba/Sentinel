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

import com.alibaba.csp.sentinel.event.freq.impl.ClusterFallbackPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.ClusterFallbackEvent;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Daydreamer-ia
 */
public class ClusterFallbackPeriodFreqLimiterTest {

    private ClusterFallbackPeriodFreqLimiter limiter;

    @Before
    public void setUp() {
        // 设置一个限制周期，例如1000毫秒
        long limitPeriod = 1000L;
        limiter = new ClusterFallbackPeriodFreqLimiter(limitPeriod);
    }

    @Test
    public void testGetLimitDimensionKey_WithClusterFallbackEvent() throws InterruptedException {
        // 创建一个 ClusterFallbackEvent 模拟对象
        ClusterFallbackEvent fallbackEvent = Mockito.mock(ClusterFallbackEvent.class);
        AbstractRule rule = Mockito.mock(AbstractRule.class);

        // 设置模拟返回值
        Mockito.when(fallbackEvent.getResourceName()).thenReturn("testResource");
        Mockito.when(fallbackEvent.getRule()).thenReturn(rule);
        Mockito.when(rule.getId()).thenReturn(123L);

        boolean b = limiter.shouldHandle(fallbackEvent);
        assertTrue(b);

        b = limiter.shouldHandle(fallbackEvent);
        assertFalse(b);

        // 等待1s
        Thread.sleep(1000);
        b = limiter.shouldHandle(fallbackEvent);
        assertTrue(b);

        limiter.shouldHandle(fallbackEvent);
        b = limiter.shouldHandle(fallbackEvent);
        assertFalse(b);

    }
}
