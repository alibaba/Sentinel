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

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class CircuitBreakingIntegrationTest extends AbstractTimeBasedTest {

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
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 5;
        int maxRt = 50;
        int statIntervalMs = 20000;
        int minRequestAmount = 10;
        String res = "CircuitBreakingIntegrationTest_testSlowRequestMode";
        EventObserverRegistry.getInstance().addStateChangeObserver(res, observer);
        DegradeRuleManager.loadRules(Arrays.asList(
            new DegradeRule(res).setTimeWindow(retryTimeoutSec).setCount(maxRt)
                .setStatIntervalMs(statIntervalMs).setMinRequestAmount(minRequestAmount)
                .setSlowRatioThreshold(0.8d).setGrade(0)
        ));

        // Try first N requests where N = minRequestAmount.
        for (int i = 0; i < minRequestAmount; i++) {
            if (i < 7) {
                assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
            } else {
                assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(-20, -10)));
            }
        }

        // Till now slow ratio should be 70%.
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));
        // Circuit breaker has transformed to OPEN since here.
        verify(observer)
            .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), anyDouble());
        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(res).get(0).currentState());
        assertFalse(entryAndSleepFor(res, 1));

        sleepSecond(1);
        assertFalse(entryAndSleepFor(res, 1));
        sleepSecond(retryTimeoutSec);
        // Test HALF-OPEN to OPEN.
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));

        verify(observer)
            .onStateChange(eq(State.OPEN), eq(State.HALF_OPEN), any(DegradeRule.class), nullable(Double.class));
        verify(observer)
            .onStateChange(eq(State.HALF_OPEN), eq(State.OPEN), any(DegradeRule.class), anyDouble());
        // Wait for next retry timeout;
        reset(observer);
        sleepSecond(retryTimeoutSec + 1);
        assertTrue(entryAndSleepFor(res, maxRt - ThreadLocalRandom.current().nextInt(10, 20)));
        verify(observer)
            .onStateChange(eq(State.OPEN), eq(State.HALF_OPEN), any(DegradeRule.class), nullable(Double.class));
        verify(observer)
            .onStateChange(eq(State.HALF_OPEN), eq(State.CLOSED), any(DegradeRule.class), nullable(Double.class));
        // Now circuit breaker has been closed.
        assertTrue(entryAndSleepFor(res, maxRt + ThreadLocalRandom.current().nextInt(10, 20)));

        EventObserverRegistry.getInstance().removeStateChangeObserver(res);
    }

    @Test
    public void testExceptionRatioMode() throws Exception {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 5;
        double maxRatio = 0.5;
        int statIntervalMs = 25000;
        final int minRequestAmount = 10;
        String res = "CircuitBreakingIntegrationTest_testExceptionRatioMode";
        EventObserverRegistry.getInstance().addStateChangeObserver(res, observer);
        DegradeRuleManager.loadRules(Arrays.asList(
            new DegradeRule(res).setTimeWindow(retryTimeoutSec).setCount(maxRatio)
                .setStatIntervalMs(statIntervalMs).setMinRequestAmount(minRequestAmount)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
        ));

        // Try first N requests where N = minRequestAmount.
        for (int i = 0; i < minRequestAmount - 1; i++) {
            if (i < 6) {
                assertTrue(entryWithErrorIfPresent(res, new IllegalArgumentException()));
            } else {
                assertTrue(entryWithErrorIfPresent(res, null));
            }
        }

        // Till now slow ratio should be 60%.
        assertTrue(entryWithErrorIfPresent(res, new IllegalArgumentException()));
        // Circuit breaker has transformed to OPEN since here.
        assertEquals(State.OPEN, DegradeRuleManager.getCircuitBreakers(res).get(0).currentState());
        assertFalse(entryWithErrorIfPresent(res, null));

        sleepSecond(2);
        assertFalse(entryWithErrorIfPresent(res, null));
        sleepSecond(retryTimeoutSec);
        // Test HALF-OPEN to OPEN.
        assertTrue(entryWithErrorIfPresent(res, new IllegalArgumentException()));
        verify(observer)
            .onStateChange(eq(State.OPEN), eq(State.HALF_OPEN), any(DegradeRule.class), nullable(Double.class));
        verify(observer)
            .onStateChange(eq(State.HALF_OPEN), eq(State.OPEN), any(DegradeRule.class), anyDouble());
        // Wait for next retry timeout;
        reset(observer);
        sleepSecond(retryTimeoutSec + 1);
        assertTrue(entryWithErrorIfPresent(res, null));
        verify(observer)
            .onStateChange(eq(State.OPEN), eq(State.HALF_OPEN), any(DegradeRule.class), nullable(Double.class));
        verify(observer)
            .onStateChange(eq(State.HALF_OPEN), eq(State.CLOSED), any(DegradeRule.class), nullable(Double.class));
        // Now circuit breaker has been closed.
        assertTrue(entryWithErrorIfPresent(res, new IllegalArgumentException()));

        EventObserverRegistry.getInstance().removeStateChangeObserver(res);
    }

    @Test
    public void testExceptionCountMode() throws Throwable {
        // TODO
    }
    
    private void verifyState(List<CircuitBreaker> breakers, int target) {
        int state = 0;
        for (CircuitBreaker breaker : breakers) {
            if (breaker.currentState() == State.OPEN) {
                state ++;
            } else if (breaker.currentState() == State.HALF_OPEN) {
                state --;
            } else {
                state -= 2;
            }
        }
        assertEquals(target, state);
    }
    
    @Test
    public void testMultipleHalfOpenedBreakers() throws Exception {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        setCurrentMillis(System.currentTimeMillis() / 1000 * 1000);
        int retryTimeoutSec = 2;
        int maxRt = 50;
        int statIntervalMs = 20000;
        int minRequestAmount = 1;
        String res = "CircuitBreakingIntegrationTest_testMultipleHalfOpenedBreakers";
        EventObserverRegistry.getInstance().addStateChangeObserver(res, observer);
        // initial two rules
        DegradeRuleManager.loadRules(Arrays.asList(
            new DegradeRule(res).setTimeWindow(retryTimeoutSec).setCount(maxRt)
                .setStatIntervalMs(statIntervalMs).setMinRequestAmount(minRequestAmount)
                .setSlowRatioThreshold(0.8d).setGrade(0),
            new DegradeRule(res).setTimeWindow(retryTimeoutSec * 2).setCount(maxRt)
                .setStatIntervalMs(statIntervalMs).setMinRequestAmount(minRequestAmount)
                .setSlowRatioThreshold(0.8d).setGrade(0)
        ));
        assertTrue(entryAndSleepFor(res, 100));
        // they are open now
        for (CircuitBreaker breaker : DegradeRuleManager.getCircuitBreakers(res)) {
            assertEquals(CircuitBreaker.State.OPEN, breaker.currentState());
        }
        
        sleepSecond(3);
        
        for (int i = 0; i < 10; i ++) {
            assertFalse(entryAndSleepFor(res, 100));
        }
        // Now one is in open state while the other experiences open -> half-open -> open
        verifyState(DegradeRuleManager.getCircuitBreakers(res), 2);
        
        sleepSecond(3);
        
        // They will all recover
        for (int i = 0; i < 10; i ++) {
            assertTrue(entryAndSleepFor(res, 1));
        }
        
        verifyState(DegradeRuleManager.getCircuitBreakers(res), -4);
    }
    
}
