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
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link ParamFlowSlot}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowSlotTest {

    private final ParamFlowSlot paramFlowSlot = new ParamFlowSlot();

    @Test
    public void testEntryWhenParamFlowRuleNotExists() throws Throwable {
        String resourceName = "testEntryWhenParamFlowRuleNotExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        paramFlowSlot.entry(null, resourceWrapper, null, 1, "abc");
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
        paramFlowSlot.entry(null, resourceWrapper, null, 1, argToGo);
        // The second entry will be blocked.
        try {
            paramFlowSlot.entry(null, resourceWrapper, null, 1, argToGo);
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