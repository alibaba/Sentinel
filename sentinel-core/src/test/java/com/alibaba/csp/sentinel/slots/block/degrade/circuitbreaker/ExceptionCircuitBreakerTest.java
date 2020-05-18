/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker.SimpleErrorCounter;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Zhao
 */
public class ExceptionCircuitBreakerTest extends AbstractTimeBasedTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testStateChangeAndTryAcquire() {
        int retryTimeout = 10;
        DegradeRule rule = new DegradeRule("abc")
            .setCount(0.5d)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setStatIntervalMs(20 * 1000)
            .setTimeWindow(retryTimeout)
            .setMinRequestAmount(10);
        LeapArray<SimpleErrorCounter> stat = mock(LeapArray.class);
        SimpleErrorCounter counter = new SimpleErrorCounter();
        WindowWrap<SimpleErrorCounter> bucket = new WindowWrap<>(20000, 0, counter);
        when(stat.currentWindow()).thenReturn(bucket);

        ExceptionCircuitBreaker cb = new ExceptionCircuitBreaker(rule, stat);

        assertTrue(cb.tryPass());
        assertTrue(cb.tryPass());

        setCurrentMillis(System.currentTimeMillis());
        cb.fromCloseToOpen(0.52d);
        assertEquals(State.OPEN, cb.currentState());

        assertFalse(cb.tryPass());
        assertFalse(cb.tryPass());

        // Wait for next retry checkpoint.
        sleepSecond(retryTimeout);
        sleep(100);
        // Try a request to trigger state transformation.
        assertTrue(cb.tryPass());
        assertEquals(State.HALF_OPEN, cb.currentState());

        // Mark this request as error
        cb.onRequestComplete(20, new IllegalArgumentException());
        assertEquals(State.OPEN, cb.currentState());

        // Wait for next retry checkpoint.
        sleepSecond(retryTimeout);
        sleep(100);
        assertTrue(cb.tryPass());
        assertEquals(State.HALF_OPEN, cb.currentState());

        setCurrentMillis(System.currentTimeMillis());
        // Mark this request as success.
        cb.onRequestComplete(20, null);
        assertEquals(State.CLOSED, cb.currentState());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRecordErrorOrSuccess() {
        DegradeRule rule = new DegradeRule("abc")
            .setCount(0.5d)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setStatIntervalMs(20 * 1000)
            .setTimeWindow(10)
            .setMinRequestAmount(10);
        LeapArray<SimpleErrorCounter> stat = mock(LeapArray.class);
        SimpleErrorCounter counter = new SimpleErrorCounter();
        WindowWrap<SimpleErrorCounter> bucket = new WindowWrap<>(20000, 0, counter);
        when(stat.currentWindow()).thenReturn(bucket);

        CircuitBreaker cb = new ExceptionCircuitBreaker(rule, stat);
        cb.onRequestComplete(15, null);

        assertEquals(1L, counter.getTotalCount().longValue());
        assertEquals(0L, counter.getErrorCount().longValue());

        cb.onRequestComplete(15, new IllegalArgumentException());
        assertEquals(2L, counter.getTotalCount().longValue());
        assertEquals(1L, counter.getErrorCount().longValue());
    }
}