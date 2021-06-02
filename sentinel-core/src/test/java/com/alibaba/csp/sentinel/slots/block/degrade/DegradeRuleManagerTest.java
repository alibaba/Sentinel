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
package com.alibaba.csp.sentinel.slots.block.degrade;

import java.util.ArrayList;
import java.util.Arrays;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link DegradeRuleManager}.
 *
 * @author Eric Zhao
 */
public class DegradeRuleManagerTest {

    @Before
    public void setUp() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void loadSameRuleUseSameCircuitBreaker() {
        String resource = "loadSameRuleUseSameCircuitBreaker";
        DegradeRule rule = new DegradeRule(resource)
            .setCount(100)
            .setSlowRatioThreshold(0.9d)
            .setTimeWindow(20)
            .setStatIntervalMs(20000);
        DegradeRuleManager.loadRules(Arrays.asList(rule));
        CircuitBreaker cb = DegradeRuleManager.getCircuitBreakers(resource).get(0);

        DegradeRuleManager.loadRules(Arrays.asList(rule,
            new DegradeRule("abc").setTimeWindow(20).setCount(20).setSlowRatioThreshold(0.8d)));
        assertSame(cb, DegradeRuleManager.getCircuitBreakers(resource).get(0));
    }

    @Test
    public void testIsValidRule() {
        DegradeRule rule1 = new DegradeRule("abc");
        DegradeRule rule2 = new DegradeRule("cde")
            .setCount(100)
            .setGrade(RuleConstant.DEGRADE_GRADE_RT)
            .setTimeWindow(-1);
        DegradeRule rule3 = new DegradeRule("xx")
            .setCount(1.1)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setTimeWindow(2);
        DegradeRule rule4 = new DegradeRule("yy")
            .setCount(-3)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
            .setTimeWindow(2);
        DegradeRule rule5 = new DegradeRule("Sentinel")
            .setCount(97)
            .setGrade(RuleConstant.DEGRADE_GRADE_RT)
            .setSlowRatioThreshold(15)
            .setTimeWindow(15);
        DegradeRule rule6 = new DegradeRule("Sentinel")
            .setCount(0.93d)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setTimeWindow(20)
            .setMinRequestAmount(0);
        DegradeRule rule7 = new DegradeRule("Sentinel")
            .setCount(100)
            .setSlowRatioThreshold(0.8d)
            .setTimeWindow(10)
            .setStatIntervalMs(0)
            .setMinRequestAmount(20);
        assertFalse(DegradeRuleManager.isValidRule(rule1));
        assertFalse(DegradeRuleManager.isValidRule(rule2));
        assertFalse(DegradeRuleManager.isValidRule(rule3));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(1.0d)));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(0.0d)));
        assertFalse(DegradeRuleManager.isValidRule(rule4));
        assertFalse(DegradeRuleManager.isValidRule(rule5));
        assertFalse(DegradeRuleManager.isValidRule(rule6));
        assertFalse(DegradeRuleManager.isValidRule(rule7));
    }
}