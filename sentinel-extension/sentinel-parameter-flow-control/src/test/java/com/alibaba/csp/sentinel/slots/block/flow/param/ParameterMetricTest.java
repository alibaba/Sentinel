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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;

/**
 * Test cases for {@link ParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetricTest {

    @Test
    public void testInitAndClearParameterMetric() {
        // Create a parameter metric for resource "abc".
        ParameterMetric metric = new ParameterMetric();

        ParamFlowRule rule = new ParamFlowRule("abc")
            .setParamIdx(1);
        metric.initialize(rule);
        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertNotNull(threadCountMap);
        CacheMap<Object, AtomicLong> timeRecordMap = metric.getRuleTimeCounter(rule);
        assertNotNull(timeRecordMap);
        metric.initialize(rule);
        assertSame(threadCountMap, metric.getThreadCountMap().get(rule.getParamIdx()));
        assertSame(timeRecordMap, metric.getRuleTimeCounter(rule));

        ParamFlowRule rule2 = new ParamFlowRule("abc")
            .setParamIdx(1);
        metric.initialize(rule2);
        CacheMap<Object, AtomicLong> timeRecordMap2 = metric.getRuleTimeCounter(rule2);
        assertSame(timeRecordMap, timeRecordMap2);

        rule2.setParamIdx(2);
        metric.initialize(rule2);
        assertNotSame(timeRecordMap2, metric.getRuleTimeCounter(rule2));

        ParamFlowRule rule3 = new ParamFlowRule("abc")
            .setParamIdx(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        metric.initialize(rule3);
        assertNotSame(timeRecordMap, metric.getRuleTimeCounter(rule3));

        metric.clear();
        assertEquals(0, metric.getThreadCountMap().size());
        assertEquals(0, metric.getRuleTimeCounterMap().size());
        assertEquals(0, metric.getRuleTokenCounterMap().size());
    }

    @Test
    public void testAddAndDecreaseThreadCountCommon() {
        testAddAndDecreaseThreadCount(PARAM_TYPE_NORMAL);
        testAddAndDecreaseThreadCount(PARAM_TYPE_ARRAY);
        testAddAndDecreaseThreadCount(PARAM_TYPE_COLLECTION);
    }

    private void testAddAndDecreaseThreadCount(int paramType) {

        ParamFlowRule rule = new ParamFlowRule();
        rule.setParamIdx(0);

        int n = 3;
        long[] v = new long[] {19L, 3L, 8L};
        ParameterMetric metric = new ParameterMetric();
        metric.initialize(rule);
        assertTrue(metric.getThreadCountMap().containsKey(rule.getParamIdx()));

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
        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(1, threadCountMap.get(vs).get());
        }

        for (int i = 1; i < n; i++) {
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
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(n, threadCountMap.get(vs).get());
        }

        for (int i = 1; i < n; i++) {
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
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
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
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(0, threadCountMap.size());
    }

    private static final int PARAM_TYPE_NORMAL = 0;
    private static final int PARAM_TYPE_ARRAY = 1;
    private static final int PARAM_TYPE_COLLECTION = 2;
}