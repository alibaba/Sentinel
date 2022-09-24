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
import java.util.Arrays;
import java.util.List;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.MetricEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.util.function.Predicate;

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
        final WindowWrap<MetricBucket> windowWrap = new WindowWrap<MetricBucket>(windowLengthInMs, 0,
            new MetricBucket());
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

    @Test
    public void testGetMetricDetailsOnCondition() {
        BucketLeapArray leapArray = mock(BucketLeapArray.class);
        // Mock interval=2s, sampleCount=2
        final WindowWrap<MetricBucket> w1 = new WindowWrap<>(windowLengthInMs, 500,
            new MetricBucket().add(MetricEvent.PASS, 1));
        final WindowWrap<MetricBucket> w2 = new WindowWrap<>(windowLengthInMs, 1000,
            new MetricBucket().add(MetricEvent.PASS, 2));
        final WindowWrap<MetricBucket> w3 = new WindowWrap<>(windowLengthInMs, 1500,
            new MetricBucket().add(MetricEvent.PASS, 3));
        final WindowWrap<MetricBucket> w4 = new WindowWrap<>(windowLengthInMs, 2000,
            new MetricBucket().add(MetricEvent.PASS, 4));
        List<WindowWrap<MetricBucket>> buckets = Arrays.asList(w1, w2, w3, w4);
        when(leapArray.currentWindow()).thenReturn(w4);
        when(leapArray.list()).thenReturn(buckets);

        ArrayMetric metric = new ArrayMetric(leapArray);

        // Empty condition -> retrieve all
        assertEquals(4, metric.detailsOnCondition(null).size());
        // Normal condition
        List<MetricNode> metricNodes = metric.detailsOnCondition(new Predicate<Long>() {
            @Override
            public boolean test(Long t) {
                return t >= 1500;
            }
        });
        assertEquals(2, metricNodes.size());
        assertEquals(3, metricNodes.get(0).getPassQps());
        assertEquals(4, metricNodes.get(1).getPassQps());

        // Future condition
        metricNodes = metric.detailsOnCondition(new Predicate<Long>() {
            @Override
            public boolean test(Long t) {
                return t >= 2500;
            }
        });
        assertEquals(0, metricNodes.size());
    }
}
