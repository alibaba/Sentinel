/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.degrade;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Eric Zhao
 */
public class DefaultCircuitBreakerRuleManagerTest {

    private final String RESOURCE_NAME = "method_";

    @Before
    public void setUp() throws Exception {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule(DefaultCircuitBreakerRuleManager.DEFAULT_KEY)
            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
            .setCount(50)
            .setTimeWindow(10)
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        assertTrue(DegradeRuleManager.isValidRule(rule));
        rules.add(rule);
        DefaultCircuitBreakerRuleManager.loadRules(rules);
    }

    @After
    public void tearDown() throws Exception {
        DefaultCircuitBreakerRuleManager.loadRules(new ArrayList<DegradeRule>());
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testIsValidRule() {
        DegradeRule rule1 = new DegradeRule("xx")
            .setCount(0.1d)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setTimeWindow(2);

        DegradeRule rule2 = new DegradeRule(DefaultCircuitBreakerRuleManager.DEFAULT_KEY)
            .setCount(0.1d)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setTimeWindow(2);

        assertFalse(DefaultCircuitBreakerRuleManager.isValidDefaultRule(rule1));
        assertTrue(DefaultCircuitBreakerRuleManager.isValidDefaultRule(rule2));
    }

    @Test
    public void testGetDefaultCircuitBreakers() {
        String resourceName = RESOURCE_NAME + "I";

        assertFalse(DegradeRuleManager.hasConfig(resourceName));

        List<CircuitBreaker> defaultCircuitBreakers1 = DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(
            resourceName);
        assertNotNull(defaultCircuitBreakers1);

        List<CircuitBreaker> defaultCircuitBreakers2 = DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(
            resourceName);
        assertSame(defaultCircuitBreakers1, defaultCircuitBreakers2);
    }

    @Test
    public void testGetDefaultCircuitBreakersWhileAddingCustomizedRule() {
        String resourceNameI = RESOURCE_NAME + "I";
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule(resourceNameI)
            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
            .setCount(50)
            .setTimeWindow(10)
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        assertTrue(DegradeRuleManager.hasConfig(resourceNameI));

        String resourceNameII = RESOURCE_NAME + "II";
        List<DegradeRule> rules2 = new ArrayList<DegradeRule>();
        DegradeRule rule2 = new DegradeRule(resourceNameII)
            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
            .setCount(50)
            .setTimeWindow(10)
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        rules2.add(rule2);
        DegradeRuleManager.loadRules(rules2);
        assertFalse(DegradeRuleManager.hasConfig(resourceNameI));
        assertNotNull(DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resourceNameI));

        DegradeRuleManager.loadRules(rules);
        DefaultCircuitBreakerRuleManager.addExcludedResource(resourceNameII);
        assertFalse(DegradeRuleManager.hasConfig(resourceNameII));
        assertNull(DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resourceNameII));

    }

    @Test
    public void testGetDefaultCircuitBreakersWhileRemovingCustomizedRule() {
        String resourceNameI = RESOURCE_NAME + "I";
        DefaultCircuitBreakerRuleManager.addExcludedResource(resourceNameI);
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule(resourceNameI)
            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
            .setCount(50)
            .setTimeWindow(10)
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        assertTrue(DegradeRuleManager.hasConfig(resourceNameI));
        assertNull(DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resourceNameI));

        //remove customized rule and do not recover default rule
        List<DegradeRule> rules2 = new ArrayList<DegradeRule>();
        DegradeRuleManager.loadRules(rules2);
        assertFalse(DegradeRuleManager.hasConfig(resourceNameI));
        assertNull(DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resourceNameI));

        //recover default rule
        DefaultCircuitBreakerRuleManager.removeExcludedResource(resourceNameI);
        assertFalse(DegradeRuleManager.hasConfig(resourceNameI));
        assertNotNull(DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resourceNameI));

    }

    @Test
    public void testLoadRules() {
        DegradeRule rule = mock(DegradeRule.class);
        List<DegradeRule> ruleList = new ArrayList<DegradeRule>();
        ruleList.add(rule);
        assertTrue(DefaultCircuitBreakerRuleManager.loadRules(ruleList));
        assertFalse(DefaultCircuitBreakerRuleManager.loadRules(ruleList));
    }

    @Test
    public void testLoadRulesUseDifferentCircuitBreakers() throws Exception {
        String resA = "resA";
        String resB = "resB";
        List<CircuitBreaker> cbsForResourceA = DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resA);
        assertNotNull(cbsForResourceA);
        List<CircuitBreaker> cbsForResourceB = DefaultCircuitBreakerRuleManager.getDefaultCircuitBreakers(resB);
        assertNotNull(cbsForResourceB);
        assertNotEquals(cbsForResourceA, cbsForResourceB);
        Field cbsField;
        try {
            cbsField = DefaultCircuitBreakerRuleManager.class.getDeclaredField("circuitBreakers");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new Exception();
        }
        cbsField.setAccessible(true);
        Map<String, List<CircuitBreaker>> cbs = (Map<String, List<CircuitBreaker>>) cbsField.get(
            DefaultCircuitBreakerRuleManager.class);
        assertEquals(2, cbs.size());

        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule(DefaultCircuitBreakerRuleManager.DEFAULT_KEY)
            //rule is different in strategy
            .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
            .setCount(0.1d)
            .setTimeWindow(10)
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        assertTrue(DegradeRuleManager.isValidRule(rule));
        rules.add(rule);
        DefaultCircuitBreakerRuleManager.loadRules(rules);
        try {
            cbsField = DefaultCircuitBreakerRuleManager.class.getDeclaredField("circuitBreakers");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new Exception();
        }
        cbsField.setAccessible(true);
        Map<String, List<CircuitBreaker>> newCbs = (Map<String, List<CircuitBreaker>>) cbsField.get(
            DefaultCircuitBreakerRuleManager.class);
        assertEquals(2, newCbs.size());
        assertNotEquals(cbs, newCbs);

        List<CircuitBreaker> resACbs = newCbs.get(resA);
        assertNotNull(resACbs);
        List<CircuitBreaker> resBCbs = newCbs.get(resB);
        assertNotNull(resBCbs);
        assertNotEquals(resACbs, resBCbs);
    }

}
