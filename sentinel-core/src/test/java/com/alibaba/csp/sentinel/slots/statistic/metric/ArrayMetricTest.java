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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.ArrayList;

import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link ArrayMetric}.
 *
 * @author Eric Zhao
 */
public class ArrayMetricTest {

    private final int windowLengthInMs = 500;

    @Test
    public void testOperateArrayMetric() {
        BucketLeapArray leapArray = mock(BucketLeapArray.class);
        final WindowWrap<MetricBucket> windowWrap = new WindowWrap<MetricBucket>(windowLengthInMs, 0, new MetricBucket());
        when(leapArray.currentWindow()).thenReturn(windowWrap);
        when(leapArray.values()).thenReturn(new ArrayList<MetricBucket>() {{ add(windowWrap.value()); }});

        ArrayMetric metric = new ArrayMetric(leapArray);

        final int expectedPass = 9;
        final int expectedBlock = 2;
        final int expectedSuccess = 9;
        final int expectedException = 6;
        final int expectedRt = 21;

        metric.addRT(expectedRt);
        for (int i = 0; i < expectedPass; i++) {
            metric.addPass(1);
        }
        for (int i = 0; i < expectedBlock; i++) {
            metric.addBlock(1);
        }
        for (int i = 0; i < expectedSuccess; i++) {
            metric.addSuccess(1);
        }
        for (int i = 0; i < expectedException; i++) {
            metric.addException(1);
        }

        assertEquals(expectedPass, metric.pass());
        assertEquals(expectedBlock, metric.block());
        assertEquals(expectedSuccess, metric.success());
        assertEquals(expectedException, metric.exception());
        assertEquals(expectedRt, metric.rt());
    }
}