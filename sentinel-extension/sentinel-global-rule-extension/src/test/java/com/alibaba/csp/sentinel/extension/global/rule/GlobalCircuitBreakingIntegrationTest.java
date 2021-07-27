package com.alibaba.csp.sentinel.extension.global.rule;/*
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

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.extension.global.rule.config.GlobalRuleConfig;
import com.alibaba.csp.sentinel.extension.global.rule.degrade.GlobalDegradeRule;
import com.alibaba.csp.sentinel.extension.global.rule.degrade.GlobalDegradeRuleSelector;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class GlobalCircuitBreakingIntegrationTest extends AbstractTimeBasedTest {

    @Before
    public void setUp() {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testSlowRequestMode() throws Exception {
        SentinelConfig.setConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_DEGRADE_RULE, "true");

        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 5;
        int maxRt = 50;
        int statIntervalMs = 20000;
        int minRequestAmount = 10;

        String normalRuleLimitResourceName = "normal_rule";
        String globalRuleLimitResourceName = "[\\s\\S]*";

        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(normalRuleLimitResourceName);
        degradeRule.setTimeWindow(retryTimeoutSec);
        degradeRule.setCount(maxRt);
        degradeRule.setStatIntervalMs(statIntervalMs);
        degradeRule.setMinRequestAmount(minRequestAmount);
        degradeRule.setSlowRatioThreshold(0.8d);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        GlobalDegradeRule globalDegradeRule = new GlobalDegradeRule();
        globalDegradeRule.setResource(globalRuleLimitResourceName);
        globalDegradeRule.setTimeWindow(retryTimeoutSec);
        globalDegradeRule.setCount(maxRt);
        globalDegradeRule.setStatIntervalMs(statIntervalMs);
        globalDegradeRule.setMinRequestAmount(minRequestAmount);
        globalDegradeRule.setSlowRatioThreshold(0.8d);
        globalDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule, globalDegradeRule));

        // Try first N requests where N = minRequestAmount.
        for (int i = 0; i < minRequestAmount; i++) {
            if (i < 7) {
                assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
            } else {
                assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(-20, -10)));
            }
        }

        // Till now slow ratio should be 70%.
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));

        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.OPEN, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());

        assertFalse(entryAndSleepFor(normalRuleLimitResourceName, 1));

        sleepSecond(1);
        assertFalse(entryAndSleepFor(normalRuleLimitResourceName, 1));

        sleepSecond(retryTimeoutSec);
        // Test HALF-OPEN to OPEN.
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.OPEN, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());
        assertFalse(entryAndSleepFor(normalRuleLimitResourceName, 1));

        // Wait for next retry timeout;
        sleepSecond(retryTimeoutSec + 1);
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, maxRt - ThreadLocalRandom.current().nextInt(10, 20)));
        assertEquals(State.CLOSED, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.CLOSED, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());
        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, 1));

    }

    @Test
    public void testExceptionRatioModeNormal() throws Exception {
        SentinelConfig.setConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_DEGRADE_RULE, "true");

        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 5;
        double maxRatio = 0.5;
        int statIntervalMs = 25000;
        final int minRequestAmount = 10;

        String normalRuleLimitResourceName = "normal_rule";
        String globalRuleLimitResourceName = "[\\s\\S]*";

        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(normalRuleLimitResourceName);
        degradeRule.setTimeWindow(retryTimeoutSec);
        degradeRule.setCount(maxRatio);
        degradeRule.setStatIntervalMs(statIntervalMs);
        degradeRule.setMinRequestAmount(minRequestAmount);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);

        GlobalDegradeRule globalDegradeRule = new GlobalDegradeRule();
        globalDegradeRule.setResource(globalRuleLimitResourceName);
        globalDegradeRule.setTimeWindow(retryTimeoutSec);
        globalDegradeRule.setCount(maxRatio);
        globalDegradeRule.setStatIntervalMs(statIntervalMs);
        globalDegradeRule.setMinRequestAmount(minRequestAmount);
        globalDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule, globalDegradeRule));

        // because open merge switch, so count should be equals 2
        List<CircuitBreaker> matchRules = new GlobalDegradeRuleSelector().select("normal_rule");
        assertEquals("select rule fail", 2, matchRules.size());

        // Try first N requests where N = minRequestAmount.
        for (int i = 0; i < minRequestAmount - 1; i++) {
            if (i < 6) {
                assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, new IllegalArgumentException()));
            } else {
                assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, null));
            }
        }

        // Till now slow ratio should be 60%.
        assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, new IllegalArgumentException()));

        /**
         * Circuit breaker has transformed to OPEN since here.
         */
        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.OPEN, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());

        assertFalse(entryWithErrorIfPresent(normalRuleLimitResourceName, null));

        sleepSecond(2);
        assertFalse(entryWithErrorIfPresent(normalRuleLimitResourceName, null));

        sleepSecond(retryTimeoutSec);

        // Test HALF-OPEN to OPEN.
        assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, new IllegalArgumentException()));
        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.OPEN, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());
        assertFalse(entryWithErrorIfPresent(normalRuleLimitResourceName, new IllegalArgumentException()));

        sleepSecond(retryTimeoutSec);
        // Test HALF-OPEN to CLOSE.
        assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, null));
        assertEquals(State.CLOSED, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.CLOSED, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());
        assertTrue(entryWithErrorIfPresent(normalRuleLimitResourceName, new IllegalArgumentException()));
    }

    @Test
    public void testMultipleHalfOpenedBreakers() throws Exception {
        SentinelConfig.setConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_DEGRADE_RULE, "true");

        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 2;
        int maxRt = 50;
        int statIntervalMs = 20000;
        int minRequestAmount = 1;

        // initial two rules
        String normalRuleLimitResourceName = "normal_rule";
        String globalRuleLimitResourceName = "[\\s\\S]*";

        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(normalRuleLimitResourceName);
        degradeRule.setTimeWindow(retryTimeoutSec * 2);
        degradeRule.setCount(maxRt);
        degradeRule.setStatIntervalMs(statIntervalMs);
        degradeRule.setMinRequestAmount(minRequestAmount);
        degradeRule.setSlowRatioThreshold(0.8d);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        GlobalDegradeRule globalDegradeRule = new GlobalDegradeRule();
        globalDegradeRule.setResource(globalRuleLimitResourceName);
        globalDegradeRule.setTimeWindow(retryTimeoutSec * 2);
        globalDegradeRule.setCount(maxRt);
        globalDegradeRule.setStatIntervalMs(statIntervalMs);
        globalDegradeRule.setMinRequestAmount(minRequestAmount);
        globalDegradeRule.setSlowRatioThreshold(0.8d);
        globalDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule, globalDegradeRule));

        assertTrue(entryAndSleepFor(normalRuleLimitResourceName, 100));

        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName).get(0).currentState());
        assertEquals(State.OPEN, GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName).get(0).currentState());

        List<CircuitBreaker> normalCircuitBreakers = DegradeRuleManager.getCircuitBreakers(normalRuleLimitResourceName);
        List<CircuitBreaker> globalCircuitBreakers = GlobalRuleManager.getGlobalDegradeRules().get(globalRuleLimitResourceName);

        ArrayList<CircuitBreaker> allCircuitBreakers = new ArrayList<>(normalCircuitBreakers);
        allCircuitBreakers.addAll(globalCircuitBreakers);

        // they are open now
        for (CircuitBreaker breaker : allCircuitBreakers) {
            assertEquals(State.OPEN, breaker.currentState());
        }

        sleepSecond(3);

        for (int i = 0; i < 10; i++) {
            assertFalse(entryAndSleepFor(normalRuleLimitResourceName, 100));
        }
        // Now one is in open state while the other experiences open -> half-open -> open
        verifyState(allCircuitBreakers, 2);

        sleepSecond(3);

        // They will all recover
        for (int i = 0; i < 10; i++) {
            assertTrue(entryAndSleepFor(normalRuleLimitResourceName, 1));
        }

        verifyState(allCircuitBreakers, -4);
    }

    private void verifyState(List<CircuitBreaker> breakers, int target) {
        int state = 0;
        for (CircuitBreaker breaker : breakers) {
            if (breaker.currentState() == State.OPEN) {
                state++;
            } else if (breaker.currentState() == State.HALF_OPEN) {
                state--;
            } else {
                state -= 2;
            }
        }
        assertEquals(target, state);
    }
}
