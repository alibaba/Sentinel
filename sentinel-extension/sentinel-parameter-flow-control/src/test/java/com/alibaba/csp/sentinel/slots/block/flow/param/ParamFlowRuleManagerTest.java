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
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ParamFlowRuleManager}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowRuleManagerTest {

    @Before
    public void setUp() {
        ParamFlowRuleManager.loadRules(null);
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @After
    public void tearDown() {
        ParamFlowRuleManager.loadRules(null);
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @Test
    public void testLoadParamRulesClearingUnusedMetrics() {
        final String resA = "resA";
        ParamFlowRule ruleA = new ParamFlowRule(resA)
            .setCount(1)
            .setParamIdx(0);
        ParamFlowRuleManager.loadRules(Collections.singletonList(ruleA));
        ParameterMetricStorage.getMetricsMap().put(resA, new ParameterMetric());
        assertNotNull(ParameterMetricStorage.getParamMetricForResource(resA));

        final String resB = "resB";
        ParamFlowRule ruleB = new ParamFlowRule(resB)
            .setCount(2)
            .setParamIdx(1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(ruleB));
        assertNull("The unused hot param metric should be cleared",
            ParameterMetricStorage.getParamMetricForResource(resA));
    }

    @Test
    public void testLoadParamRulesClearingUnusedMetricsForRule() {
        final String resA = "resA";
        ParamFlowRule ruleA1 = new ParamFlowRule(resA)
                .setCount(1)
                .setParamIdx(0);
        ParamFlowRule ruleA2 = new ParamFlowRule(resA)
                .setCount(2)
                .setParamIdx(1);

        ParamFlowRuleManager.loadRules(Arrays.asList(ruleA1, ruleA2));
        ParameterMetric metric = new ParameterMetric();
        metric.initialize(ruleA1);
        metric.initialize(ruleA2);
        ParameterMetricStorage.getMetricsMap().put(resA, metric);

        ParameterMetric metric1 = ParameterMetricStorage.getParamMetricForResource(resA);
        assertNotNull(metric1);
        assertNotNull(metric1.getRuleTimeCounter(ruleA1));
        assertNotNull(metric1.getRuleTimeCounter(ruleA2));

        ParamFlowRuleManager.loadRules(Arrays.asList(ruleA1));

        ParameterMetric metric2 = ParameterMetricStorage.getParamMetricForResource(resA);
        assertNotNull(metric2);
        assertNotNull(metric2.getRuleTimeCounter(ruleA1));
        assertNull(metric2.getRuleTimeCounter(ruleA2));
    }


    @Test
    public void testLoadParamRulesAndGet() {
        final String resA = "abc";
        final String resB = "foo";
        final String resC = "baz";
        // Rule A to C is for resource A.
        // Rule A is invalid.
        ParamFlowRule ruleA = new ParamFlowRule(resA).setCount(10);
        ParamFlowRule ruleB = new ParamFlowRule(resA)
            .setCount(28)
            .setParamIdx(1);
        ParamFlowRule ruleC = new ParamFlowRule(resA)
            .setCount(8)
            .setParamIdx(1)
            .setGrade(RuleConstant.FLOW_GRADE_THREAD);
        // Rule D is for resource B.
        ParamFlowRule ruleD = new ParamFlowRule(resB)
            .setCount(9)
            .setParamIdx(0)
            .setParamFlowItemList(Arrays.asList(ParamFlowItem.newItem(7L, 6), ParamFlowItem.newItem(9L, 4)));
        ParamFlowRuleManager.loadRules(Arrays.asList(ruleA, ruleB, ruleC, ruleD));

        // Test for ParamFlowRuleManager#hasRules
        assertTrue(ParamFlowRuleManager.hasRules(resA));
        assertTrue(ParamFlowRuleManager.hasRules(resB));
        assertFalse(ParamFlowRuleManager.hasRules(resC));
        // Test for ParamFlowRuleManager#getRulesOfResource
        List<ParamFlowRule> rulesForResA = ParamFlowRuleManager.getRulesOfResource(resA);
        assertEquals(2, rulesForResA.size());
        assertFalse(rulesForResA.contains(ruleA));
        assertTrue(rulesForResA.contains(ruleB));
        assertTrue(rulesForResA.contains(ruleC));
        List<ParamFlowRule> rulesForResB = ParamFlowRuleManager.getRulesOfResource(resB);
        assertEquals(1, rulesForResB.size());
        assertEquals(ruleD, rulesForResB.get(0));
        // Test for ParamFlowRuleManager#getRules
        List<ParamFlowRule> allRules = ParamFlowRuleManager.getRules();
        assertFalse(allRules.contains(ruleA));
        assertTrue(allRules.contains(ruleB));
        assertTrue(allRules.contains(ruleC));
        assertTrue(allRules.contains(ruleD));
    }

    @Test
    public void testLoadParamRulesWithNoMetric() {
        String resource = "test";
        ParamFlowRule paramFlowRule = new ParamFlowRule(resource)
                .setDurationInSec(1).setParamIdx(1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(paramFlowRule));
        ParamFlowRule newParamFlowRule = new ParamFlowRule(resource)
                .setDurationInSec(2).setParamIdx(1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(newParamFlowRule));
        List<ParamFlowRule> result = ParamFlowRuleManager.getRulesOfResource(resource);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getDurationInSec());
    }
}
