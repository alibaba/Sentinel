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

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.alibaba.csp.sentinel.config.SentinelConfig.GLOBAL_RULE_SWITCH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link DegradeRuleManager}.
 *
 * @author Eric Zhao
 */
public class DegradeRuleManagerTest {

    @Before
    public void setUp() {
        clean();
    }

    @After
    public void tearDown() {
        clean();
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
                .setTimeWindow(15)
                .setRtSlowRequestAmount(0);
        DegradeRule rule6 = new DegradeRule("Sentinel")
                .setCount(0.93d)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setTimeWindow(20)
                .setMinRequestAmount(0);
        assertFalse(DegradeRuleManager.isValidRule(rule1));
        assertFalse(DegradeRuleManager.isValidRule(rule2));
        assertFalse(DegradeRuleManager.isValidRule(rule3));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(1.0d)));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(0.0d)));
        assertFalse(DegradeRuleManager.isValidRule(rule4));
        assertFalse(DegradeRuleManager.isValidRule(rule5));
        assertFalse(DegradeRuleManager.isValidRule(rule6));
    }

    @Test
    public void testDefaultGradeRule() {

        String resource = "test-grade-rt";
        SentinelConfig.setConfig(GLOBAL_RULE_SWITCH, "on");

        //create default degrade for resource
        DegradeRuleManager.setDefaultDegrade(resource);
        assertTrue(DegradeRuleManager.hasConfig(resource));
        DegradeRule defaultRule = DegradeRuleManager.getRules(resource).iterator().next();
        assertTrue(DegradeRuleManager.isValidRule(defaultRule));

        DegradeRule rule2 = new DegradeRule("xx")
                .setCount(0.1)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setTimeWindow(2);
        DegradeRuleManager.loadRules(Arrays.asList(rule2));
        assertTrue(DegradeRuleManager.isValidRule(rule2));
        assertTrue(DegradeRuleManager.hasConfig("xx"));

        //load other resource rule, the test-grade-rt rule still exist
        assertTrue(DegradeRuleManager.hasConfig(resource));

        DegradeRule rule3 = new DegradeRule(resource)
                .setCount(0.5)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setTimeWindow(2);
        DegradeRuleManager.loadRules(Arrays.asList(rule2, rule3));

        assertTrue(DegradeRuleManager.hasConfig(resource));
        assertTrue(DegradeRuleManager.hasConfig("xx"));
        assertTrue(DegradeRuleManager.getRules(resource).contains(rule3));
        assertFalse(DegradeRuleManager.getRules(resource).contains(defaultRule));


    }

    private void clean() {
        SentinelConfig.setConfig(GLOBAL_RULE_SWITCH, "off");
        DegradeRuleManager.loadRules(null);
    }
}