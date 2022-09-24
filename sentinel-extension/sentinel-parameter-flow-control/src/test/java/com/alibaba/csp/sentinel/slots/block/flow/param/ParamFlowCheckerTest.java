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
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void testSingleValueCheckQpsWithExceptionItems() throws InterruptedException {
        final String resourceName = "testSingleValueCheckQpsWithExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        TimeUtil.currentTimeMillis();
        int paramIdx = 0;

        long globalThreshold = 5L;
        int thresholdB = 0;
        int thresholdD = 7;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(globalThreshold);
        rule.setParamIdx(paramIdx);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

        String valueA = "valueA";
        String valueB = "valueB";
        String valueC = "valueC";
        String valueD = "valueD";

        // Directly set parsed map for test.
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        map.put(valueB, thresholdB);
        map.put(valueD, thresholdD);
        rule.setParsedHotItems(map);

        ParameterMetric metric = new ParameterMetric();
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testSingleValueCheckThreadCountWithExceptionItems() {
        final String resourceName = "testSingleValueCheckThreadCountWithExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long globalThreshold = 5L;
        int thresholdB = 3;
        int thresholdD = 7;

        ParamFlowRule rule = new ParamFlowRule(resourceName).setCount(globalThreshold).setParamIdx(paramIdx)
                .setGrade(RuleConstant.FLOW_GRADE_THREAD);

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
        when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueB)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold + 1);
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));

        when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold);
        when(metric.getThreadCount(paramIdx, valueB)).thenReturn(thresholdB - 1L);
        when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold + 1);
        when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold - 1).thenReturn((long) thresholdD);

        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
    }

    @Test
    public void testPassLocalCheckForCollection() throws InterruptedException {
        final String resourceName = "testPassLocalCheckForCollection";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        double globalThreshold = 1;

        ParamFlowRule rule = new ParamFlowRule(resourceName).setParamIdx(paramIdx).setCount(globalThreshold);

        String v1 = "a", v2 = "B", v3 = "Cc";
        List<String> list = Arrays.asList(v1, v2, v3);
        ParameterMetric metric = new ParameterMetric();
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));
        assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));
    }

    @Test
    public void testPassLocalCheckForArray() throws InterruptedException {
        final String resourceName = "testPassLocalCheckForArray";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        double globalThreshold = 1;

        ParamFlowRule rule = new ParamFlowRule(resourceName).setParamIdx(paramIdx)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER).setCount(globalThreshold);

        TimeUtil.currentTimeMillis();

        String v1 = "a", v2 = "B", v3 = "Cc";
        Object arr = new String[]{v1, v2, v3};
        ParameterMetric metric = new ParameterMetric();
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));
        assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));
    }

    @Test
    public void testPassLocalCheckForComplexParam() throws InterruptedException {
        class User implements ParamFlowArgument {
            Integer id;
            String name;
            String address;

            public User(Integer id, String name, String address) {
                this.id = id;
                this.name = name;
                this.address = address;
            }

            @Override
            public Object paramFlowKey() {
                return name;
            }
        }
        final String resourceName = "testPassLocalCheckForComplexParam";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        double globalThreshold = 1;

        ParamFlowRule rule = new ParamFlowRule(resourceName).setParamIdx(paramIdx).setCount(globalThreshold);

        Object[] args = new Object[]{new User(1, "Bob", "Hangzhou"), 10, "Demo"};
        ParameterMetric metric = new ParameterMetric();
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, args));
        assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, args));
    }

    @Before
    public void setUp() throws Exception {
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        ParameterMetricStorage.getMetricsMap().clear();
    }
}