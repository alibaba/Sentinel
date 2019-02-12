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
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.flow.param.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.ParamMapBucket;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link HotParameterLeapArray}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParameterLeapArrayTest {

    @Test
    public void testAddValueToBucket() {
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        String paramA = "paramA";
        int initialCountA = 3;
        RollingParamEvent passEvent = RollingParamEvent.REQUEST_PASSED;
        final ParamMapBucket bucket = new ParamMapBucket();
        bucket.add(passEvent, initialCountA, paramA);

        doCallRealMethod().when(leapArray).addValue(any(RollingParamEvent.class), anyInt(), any(Object.class));
        when(leapArray.currentWindow()).thenReturn(new WindowWrap<ParamMapBucket>(0, 0, bucket));
        assertEquals(initialCountA, leapArray.currentWindow().value().get(passEvent, paramA));

        int delta = 2;
        leapArray.addValue(passEvent, delta, paramA);
        assertEquals(initialCountA + delta, leapArray.currentWindow().value().get(passEvent, paramA));
    }

    @Test
    public void testGetTopValues() {
        int intervalInSec = 2;
        int a1 = 3, a2 = 5;
        String paramPrefix = "param-";
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        when(leapArray.getIntervalInSecond()).thenReturn((double) intervalInSec);

        final ParamMapBucket b1 = generateBucket(a1, paramPrefix);
        final ParamMapBucket b2 = generateBucket(a2, paramPrefix);
        List<ParamMapBucket> buckets = new ArrayList<ParamMapBucket>() {{
            add(b1);
            add(b2);
        }};
        when(leapArray.values()).thenReturn(buckets);
        when(leapArray.getTopValues(any(RollingParamEvent.class), any(int.class))).thenCallRealMethod();

        Map<Object, Double> top2Values = leapArray.getTopValues(RollingParamEvent.REQUEST_PASSED, a1 - 1);
        // Top 2 should be 5 and 3
        assertEquals((double)5 * 10 / intervalInSec, top2Values.get(paramPrefix + 5), 0.01);
        assertEquals((double)3 * 20 / intervalInSec, top2Values.get(paramPrefix + 3), 0.01);

        Map<Object, Double> top4Values = leapArray.getTopValues(RollingParamEvent.REQUEST_PASSED, a2 - 1);
        assertEquals(a2 - 1, top4Values.size());
        assertFalse(top4Values.containsKey(paramPrefix + 1));

        Map<Object, Double> topMoreValues = leapArray.getTopValues(RollingParamEvent.REQUEST_PASSED, a2 + 1);
        assertEquals("This should contain all parameters but no more than " + a2, a2, topMoreValues.size());
    }

    private ParamMapBucket generateBucket(int amount, String prefix) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bad amount");
        }
        ParamMapBucket bucket = new ParamMapBucket();
        for (int i = 1; i <= amount; i++) {
            bucket.add(RollingParamEvent.REQUEST_PASSED, i * 10, prefix + i);
            bucket.add(RollingParamEvent.REQUEST_BLOCKED, i, prefix + i);
        }
        return bucket;
    }

    @Test
    public void testGetRollingSum() {
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        String v1 = "a", v2 = "B", v3 = "Cc";
        int p1a = 19, p1b = 3;
        int p2a = 6, p2c = 17;
        RollingParamEvent passEvent = RollingParamEvent.REQUEST_PASSED;
        final ParamMapBucket b1 = new ParamMapBucket()
            .add(passEvent, p1a, v1)
            .add(passEvent, p1b, v2);
        final ParamMapBucket b2 = new ParamMapBucket()
            .add(passEvent, p2a, v1)
            .add(passEvent, p2c, v3);
        List<ParamMapBucket> buckets = new ArrayList<ParamMapBucket>() {{ add(b1); add(b2); }};
        when(leapArray.values()).thenReturn(buckets);
        when(leapArray.getRollingSum(any(RollingParamEvent.class), any(Object.class))).thenCallRealMethod();

        assertEquals(p1a + p2a, leapArray.getRollingSum(passEvent, v1));
        assertEquals(p1b, leapArray.getRollingSum(passEvent, v2));
        assertEquals(p2c, leapArray.getRollingSum(passEvent, v3));
    }

    @Test
    public void testGetRollingAvg() {
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        when(leapArray.getRollingSum(any(RollingParamEvent.class), any(Object.class))).thenReturn(15L);
        when(leapArray.getIntervalInSecond()).thenReturn(1d)
            .thenReturn(2d);
        when(leapArray.getRollingAvg(any(RollingParamEvent.class), any(Object.class))).thenCallRealMethod();

        assertEquals(15.0d, leapArray.getRollingAvg(RollingParamEvent.REQUEST_PASSED, "abc"), 0.001);
        assertEquals(15.0d / 2, leapArray.getRollingAvg(RollingParamEvent.REQUEST_PASSED, "abc"), 0.001);
    }
}