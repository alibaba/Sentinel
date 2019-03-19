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

import java.util.Collections;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
        assertNull(ParamFlowSlot.getParamMetric(resourceWrapper));
    }

    @Test
    public void testEntryWhenParamFlowExists() throws Throwable {
        String resourceName = "testEntryWhenParamFlowExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        long argToGo = 1L;
        double count = 10;
        ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setCount(count)
            .setParamIdx(0);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));

        ParameterMetric metric = mock(ParameterMetric.class);
        // First pass, then blocked.
        when(metric.getPassParamQps(rule.getParamIdx(), argToGo))
            .thenReturn(count - 1)
            .thenReturn(count);
        // Insert the mock metric to control pass or block.
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

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

    @Test
    public void testGetNullParamMetric() {
        assertNull(ParamFlowSlot.getParamMetric(null));
    }

    @Test
    public void testInitParamMetrics() {
        int index = 1;
        String resourceName = "res-" + System.currentTimeMillis();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        assertNull(ParamFlowSlot.getParamMetric(resourceWrapper));

        paramFlowSlot.initHotParamMetricsFor(resourceWrapper, index);
        ParameterMetric metric = ParamFlowSlot.getParamMetric(resourceWrapper);
        assertNotNull(metric);
        assertNotNull(metric.getRollingParameters().get(index));
        assertNotNull(metric.getThreadCountMap().get(index));

        // Duplicate init.
        paramFlowSlot.initHotParamMetricsFor(resourceWrapper, index);
        assertSame(metric, ParamFlowSlot.getParamMetric(resourceWrapper));
    }

    @Before
    public void setUp() throws Exception {
        ParamFlowRuleManager.loadRules(null);
        ParamFlowSlot.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        // Clean the metrics map.
        ParamFlowSlot.getMetricsMap().clear();
        ParamFlowRuleManager.loadRules(null);
    }
}