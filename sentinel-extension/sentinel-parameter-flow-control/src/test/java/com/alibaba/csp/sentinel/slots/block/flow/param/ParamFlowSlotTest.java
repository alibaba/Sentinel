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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Test cases for {@link ParamFlowSlot}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowSlotTest {

    private final ParamFlowSlot paramFlowSlot = new ParamFlowSlot();

    @Test
    public void testNegativeParamIdx() throws Throwable {
        String resourceName = "testNegativeParamIdx";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setCount(1)
            .setParamIdx(-1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, "abc", "def", "ghi");
        assertEquals(2, rule.getParamIdx().longValue());

        rule.setParamIdx(-1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, null);
        // Null args will not trigger conversion.
        assertEquals(-1, rule.getParamIdx().intValue());

        rule.setParamIdx(-100);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, "abc", "def", "ghi");
        assertEquals(100, rule.getParamIdx().longValue());

        rule.setParamIdx(0);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, "abc", "def", "ghi");
        assertEquals(0, rule.getParamIdx().longValue());
    }

    @Test
    public void testEntryWhenParamFlowRuleNotExists() throws Throwable {
        String resourceName = "testEntryWhenParamFlowRuleNotExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, "abc");
        // The parameter metric instance will not be created.
        assertNull(ParameterMetricStorage.getParamMetric(resourceWrapper));
    }

    @Test
    public void testEntryWhenParamFlowExists() throws Throwable {
        String resourceName = "testEntryWhenParamFlowExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        long argToGo = 1L;
        double count = 1;
        ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setCount(count)
            .setBurstCount(0)
            .setParamIdx(0);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));

        ParameterMetric metric = mock(ParameterMetric.class);

        CacheMap<Object, AtomicLong> map = new ConcurrentLinkedHashMapWrapper<>(4000);
        CacheMap<Object, AtomicLong> map2 = new ConcurrentLinkedHashMapWrapper<>(4000);
        when(metric.getRuleTimeCounter(rule)).thenReturn(map);
        when(metric.getRuleTokenCounter(rule)).thenReturn(map2);
        map.put(argToGo, new AtomicLong(TimeUtil.currentTimeMillis()));

        // Insert the mock metric to control pass or block.
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);

        // The first entry will pass.
        paramFlowSlot.entry(null, resourceWrapper, null, 1, false, argToGo);
        // The second entry will be blocked.
        try {
            paramFlowSlot.entry(null, resourceWrapper, null, 1, false, argToGo);
        } catch (ParamFlowException ex) {
            assertEquals(String.valueOf(argToGo), ex.getMessage());
            assertEquals(resourceName, ex.getResourceName());
            return;
        }
        fail("The second entry should be blocked");
    }

    @Before
    public void setUp() {
        ParamFlowRuleManager.loadRules(null);
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @After
    public void tearDown() {
        // Clean the metrics map.
        ParamFlowRuleManager.loadRules(null);
        ParameterMetricStorage.getMetricsMap().clear();
    }
}
