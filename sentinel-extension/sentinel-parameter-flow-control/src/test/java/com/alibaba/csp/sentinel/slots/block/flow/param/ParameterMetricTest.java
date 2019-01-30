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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.metric.HotParameterLeapArray;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link ParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetricTest {

    @Test
    public void testGetTopParamCount() {
        ParameterMetric metric = new ParameterMetric();
        int index = 1;
        int n = 10;
        RollingParamEvent event = RollingParamEvent.REQUEST_PASSED;
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        Map<Object, Double> topValues = new HashMap<Object, Double>() {{
            put("a", 3d);
            put("b", 7d);
        }};
        when(leapArray.getTopValues(event, n)).thenReturn(topValues);

        // Get when not initialized.
        assertEquals(0, metric.getTopPassParamCount(index, n).size());

        metric.getRollingParameters().put(index, leapArray);
        assertEquals(topValues, metric.getTopPassParamCount(index, n));
    }

    @Test
    public void testInitAndClearHotParameterMetric() {
        ParameterMetric metric = new ParameterMetric();
        int index = 1;
        metric.initializeForIndex(index);
        HotParameterLeapArray leapArray = metric.getRollingParameters().get(index);
        CacheMap cacheMap = metric.getThreadCountMap().get(index);
        assertNotNull(leapArray);
        assertNotNull(cacheMap);

        metric.initializeForIndex(index);
        assertSame(leapArray, metric.getRollingParameters().get(index));
        assertSame(cacheMap, metric.getThreadCountMap().get(index));

        metric.clear();
        assertEquals(0, metric.getRollingParameters().size());
        assertEquals(0, metric.getThreadCountMap().size());
    }

    @Test
    public void testAddAndDecreaseThreadCountCommon() {
        testAddAndDecreaseThreadCount(PARAM_TYPE_NORMAL);
        testAddAndDecreaseThreadCount(PARAM_TYPE_ARRAY);
        testAddAndDecreaseThreadCount(PARAM_TYPE_COLLECTION);
    }

    private void testAddAndDecreaseThreadCount(int paramType) {
        int paramIdx = 0;
        int n = 3;
        long[] v = new long[] {19L, 3L, 8L};
        ParameterMetric metric = new ParameterMetric();
        metric.initializeForIndex(paramIdx);
        assertTrue(metric.getThreadCountMap().containsKey(paramIdx));

        switch (paramType) {
            case PARAM_TYPE_ARRAY:
                metric.addThreadCount((Object)v);
                break;
            case PARAM_TYPE_COLLECTION:
                metric.addThreadCount(Arrays.asList(v[0], v[1], v[2]));
                break;
            case PARAM_TYPE_NORMAL:
            default:
                metric.addThreadCount(v[0]);
                metric.addThreadCount(v[1]);
                metric.addThreadCount(v[2]);
                break;
        }

        assertEquals(1, metric.getThreadCountMap().size());
        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(paramIdx);
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(1, threadCountMap.get(vs).get());
        }

        for (int i = 1 ; i < n; i++) {
            switch (paramType) {
                case PARAM_TYPE_ARRAY:
                    metric.addThreadCount((Object)v);
                    break;
                case PARAM_TYPE_COLLECTION:
                    metric.addThreadCount(Arrays.asList(v[0], v[1], v[2]));
                    break;
                case PARAM_TYPE_NORMAL:
                default:
                    metric.addThreadCount(v[0]);
                    metric.addThreadCount(v[1]);
                    metric.addThreadCount(v[2]);
                    break;
            }
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(paramIdx);
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(n, threadCountMap.get(vs).get());
        }

        for (int i = 1 ; i < n; i++) {
            switch (paramType) {
                case PARAM_TYPE_ARRAY:
                    metric.decreaseThreadCount((Object)v);
                    break;
                case PARAM_TYPE_COLLECTION:
                    metric.decreaseThreadCount(Arrays.asList(v[0], v[1], v[2]));
                    break;
                case PARAM_TYPE_NORMAL:
                default:
                    metric.decreaseThreadCount(v[0]);
                    metric.decreaseThreadCount(v[1]);
                    metric.decreaseThreadCount(v[2]);
                    break;
            }
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(paramIdx);
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(1, threadCountMap.get(vs).get());
        }

        switch (paramType) {
            case PARAM_TYPE_ARRAY:
                metric.decreaseThreadCount((Object)v);
                break;
            case PARAM_TYPE_COLLECTION:
                metric.decreaseThreadCount(Arrays.asList(v[0], v[1], v[2]));
                break;
            case PARAM_TYPE_NORMAL:
            default:
                metric.decreaseThreadCount(v[0]);
                metric.decreaseThreadCount(v[1]);
                metric.decreaseThreadCount(v[2]);
                break;
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(paramIdx);
        assertEquals(0, threadCountMap.size());
    }

    private static final int PARAM_TYPE_NORMAL = 0;
    private static final int PARAM_TYPE_ARRAY = 1;
    private static final int PARAM_TYPE_COLLECTION = 2;
}