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

import com.alibaba.csp.sentinel.event.freq.impl.SysEventPeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.impl.block.SystemBlockEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Daydreamer-ia
 */
public class SysEventPeriodFreqLimiterTest {

    private SysEventPeriodFreqLimiter limiter;

    @Before
    public void setUp() {
        // 设置一个限制周期，例如1000毫秒
        long limitPeriod = 1000L;
        limiter = new SysEventPeriodFreqLimiter(limitPeriod);
    }

    @Test
    public void testGetLimitDimensionKey_WithSystemBlockEvent() throws InterruptedException {
        // 创建一个 SystemBlockEvent 模拟对象
        SystemBlockEvent systemBlockEvent = Mockito.mock(SystemBlockEvent.class);
        // 设置模拟返回值
        Mockito.when(systemBlockEvent.getSysMetricKey()).thenReturn("cpu");

        boolean b = limiter.shouldHandle(systemBlockEvent);
        assertTrue(b);

        b = limiter.shouldHandle(systemBlockEvent);
        assertFalse(b);

        // 设置模拟返回值
        Mockito.when(systemBlockEvent.getSysMetricKey()).thenReturn("rt");
        b = limiter.shouldHandle(systemBlockEvent);
        assertTrue(b);

        b = limiter.shouldHandle(systemBlockEvent);
        assertFalse(b);

        // 等待1s
        Thread.sleep(1000);
        b = limiter.shouldHandle(systemBlockEvent);
        assertTrue(b);

        limiter.shouldHandle(systemBlockEvent);
        b = limiter.shouldHandle(systemBlockEvent);
        assertFalse(b);
    }
}
