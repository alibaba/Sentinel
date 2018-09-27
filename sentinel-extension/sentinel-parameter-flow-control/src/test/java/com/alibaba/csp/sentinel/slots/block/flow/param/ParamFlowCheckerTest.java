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
import java.util.List;
import java.util.Map;

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
 * Test cases for {@link ParamFlowChecker}.
 *
 * @author Eric Zhao
 */
public class ParamFlowCheckerTest {

    @Test
    public void testHotParamCheckerPassCheckExceedArgs() {
        final String resourceName = "testHotParamCheckerPassCheckExceedArgs";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 1;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(10);
        rule.setParamIdx(paramIdx);

        assertTrue("The rule will pass if the paramIdx exceeds provided args",
            ParamFlowChecker.passCheck(resourceWrapper, rule, 1, "abc"));
    }

    @Test
    public void testSingleValueCheckQpsWithoutExceptionItems() {
        final String resourceName = "testSingleValueCheckQpsWithoutExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);

        String valueA = "valueA";
        String valueB = "valueB";
        ParameterMetric metric = mock(ParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn((double)threshold - 1);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn((double)threshold + 1);
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
    }

    @Test
    public void testSingleValueCheckQpsWithExceptionItems() {
        final String resourceName = "testSingleValueCheckQpsWithExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long globalThreshold = 5L;
        int thresholdB = 3;
        int thresholdD = 7;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(globalThreshold);
        rule.setParamIdx(paramIdx);

        String valueA = "valueA";
        String valueB = "valueB";
        String valueC = "valueC";
        String valueD = "valueD";

        // Directly set parsed map for test.
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        map.put(valueB, thresholdB);
        map.put(valueD, thresholdD);
        rule.setParsedHotItems(map);

        ParameterMetric metric = mock(ParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn((double)globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn((double)globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueC)).thenReturn((double)globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueD)).thenReturn((double)globalThreshold + 1);
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));

        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn((double)globalThreshold);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn((double)thresholdB - 1L);
        when(metric.getPassParamQps(paramIdx, valueC)).thenReturn((double)globalThreshold + 1);
        when(metric.getPassParamQps(paramIdx, valueD)).thenReturn((double)globalThreshold - 1)
            .thenReturn((double)thresholdD);

        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
    }

    @Test
    public void testPassLocalCheckForCollection() {
        final String resourceName = "testPassLocalCheckForCollection";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        double globalThreshold = 10;

        ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setParamIdx(paramIdx)
            .setCount(globalThreshold);

        String v1 = "a", v2 = "B", v3 = "Cc";
        List<String> list = Arrays.asList(v1, v2, v3);
        ParameterMetric metric = mock(ParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, v1)).thenReturn(globalThreshold - 2)
            .thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, v2)).thenReturn(globalThreshold - 2)
            .thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, v3)).thenReturn(globalThreshold - 1)
            .thenReturn(globalThreshold);
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));
        assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));
    }

    @Test
    public void testPassLocalCheckForArray() {
        final String resourceName = "testPassLocalCheckForArray";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        double globalThreshold = 10;

        ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setParamIdx(paramIdx)
            .setCount(globalThreshold);

        String v1 = "a", v2 = "B", v3 = "Cc";
        Object arr = new String[] {v1, v2, v3};
        ParameterMetric metric = mock(ParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, v1)).thenReturn(globalThreshold - 2)
            .thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, v2)).thenReturn(globalThreshold - 2)
            .thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, v3)).thenReturn(globalThreshold - 1)
            .thenReturn(globalThreshold);
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));
        assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));
    }

    @Before
    public void setUp() throws Exception {
        ParamFlowSlot.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        ParamFlowSlot.getMetricsMap().clear();
    }
}