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

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link ParamFlowSlot}.
 *
 * @author Eric Zhao
 * @author cdfive
 * @since 0.2.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ParamFlowRuleManager.class)
public class ParamFlowSlotTest {

    private final ParamFlowSlot paramFlowSlot = new ParamFlowSlot();

    @Test
    public void testFireEntry() throws Throwable {
        ParamFlowSlot slot = mock(ParamFlowSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getName()).thenReturn("");
        DefaultNode node = mock(DefaultNode.class);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        verify(slot).entry(context, resourceWrapper, node, 1, false);
        // Verify fireEntry method has been called, and only once
        verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testFireExit() throws Throwable {
        ParamFlowSlot slot = mock(ParamFlowSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);

        doCallRealMethod().when(slot).exit(context, resourceWrapper, 1);
        slot.exit(context, resourceWrapper, 1);

        verify(slot).exit(context, resourceWrapper, 1);
        // Verify fireExit method has been called, and only once
        verify(slot).fireExit(context, resourceWrapper, 1);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testEntryParamFlowRule() throws Throwable {
        PowerMockito.mockStatic(ParamFlowRuleManager.class);

        ParamFlowSlot slot = mock(ParamFlowSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getName()).thenReturn("resourceA");
        DefaultNode node = mock(DefaultNode.class);

        PowerMockito.when(ParamFlowRuleManager.hasRules("resourceA")).thenReturn(true);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        // Verify checkFlow firstly, then fireEntry, and both are called, and only once
        InOrder inOrder = inOrder(slot);
        inOrder.verify(slot).checkFlow(resourceWrapper, 1);
        inOrder.verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        inOrder.verifyNoMoreInteractions();
    }

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
