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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.util.function.BiConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Eric Zhao
 */
public class ExceptionCircuitBreakerTest extends AbstractTimeBasedTest {

    @Before
    public void setUp() {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testRecordErrorOrSuccess() throws BlockException {
        try (MockedStatic<TimeUtil> mocked = super.mockTimeUtil()) {
            String resource = "testRecordErrorOrSuccess";
            int retryTimeoutMillis = 10 * 1000;
            int retryTimeout = retryTimeoutMillis / 1000;
            DegradeRule rule = new DegradeRule("abc")
                    .setCount(0.2d)
                    .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                    .setStatIntervalMs(20 * 1000)
                    .setTimeWindow(retryTimeout)
                    .setMinRequestAmount(1);
            rule.setResource(resource);
            DegradeRuleManager.loadRules(Arrays.asList(rule));

            assertTrue(entryAndSleepFor(mocked, resource, 10));

            assertTrue(entryWithErrorIfPresent(mocked, resource, new IllegalArgumentException())); // -> open
            assertFalse(entryWithErrorIfPresent(mocked, resource, new IllegalArgumentException()));
            assertFalse(entryAndSleepFor(mocked, resource, 100));
            sleep(mocked, retryTimeoutMillis / 2);
            assertFalse(entryAndSleepFor(mocked, resource, 100));
            sleep(mocked, retryTimeoutMillis / 2);
            assertTrue(entryWithErrorIfPresent(mocked, resource, new IllegalArgumentException())); // -> half -> open
            assertFalse(entryAndSleepFor(mocked, resource, 100));
            assertFalse(entryAndSleepFor(mocked, resource, 100));
            sleep(mocked, retryTimeoutMillis);
            assertTrue(entryAndSleepFor(mocked, resource, 100)); // -> half -> closed
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
            assertTrue(entryWithErrorIfPresent(mocked, resource, new IllegalArgumentException()));
            assertTrue(entryAndSleepFor(mocked, resource, 100));
        }
    }

    @Test
    public void testMaxErrorRatioThreshold() {
        try (MockedStatic<TimeUtil> mocked = super.mockTimeUtil()) {
            String resource = "testMaxErrorRatioThreshold";
            DegradeRule rule = new DegradeRule("resource")
                    .setCount(1)
                    .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                    .setMinRequestAmount(3)
                    .setStatIntervalMs(5000)
                    .setTimeWindow(5);
            rule.setResource(resource);
            DegradeRuleManager.loadRules(Collections.singletonList(rule));

            assertTrue(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));
            assertTrue(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));
            assertTrue(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));

            // should be blocked, cause 3/3 requests' rt is bigger than max rt.
            assertFalse(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));
            assertFalse(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));

            sleep(mocked, 5000);

            assertTrue(entryWithErrorIfPresent(mocked, resource, new RuntimeException()));
        }
    }

    @Test
    public void testHalfOpenRollbackUpdatesRetryTimestamp() {
        try (MockedStatic<TimeUtil> mocked = super.mockTimeUtil()) {
            DegradeRule rule = new DegradeRule("abc")
                .setCount(0.2d)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setStatIntervalMs(20 * 1000)
                .setTimeWindow(10)
                .setMinRequestAmount(1);

            AbstractCircuitBreaker cb = new ExceptionCircuitBreaker(rule);

            Context context = new Context(null, "ctx");
            Entry entry = Mockito.mock(Entry.class);
            context.setCurEntry(entry);

            // Capture the terminate handler attached during the OPEN -> HALF_OPEN
            // transition so that we can trigger it manually afterwards.
            AtomicReference<BiConsumer<Context, Entry>> handlerRef = new AtomicReference<>();
            Mockito.doAnswer(invocation -> {
                handlerRef.set(invocation.getArgument(0));
                return null;
            }).when(entry).whenTerminate(Mockito.any());

            // Trigger the circuit to OPEN first, then let the recovery window pass.
            cb.transformToOpen(1.0d);
            assertEquals(CircuitBreaker.State.OPEN, cb.currentState());
            sleep(mocked, 10 * 1000 + 1000);

            assertTrue(cb.tryPass(context));
            assertEquals(CircuitBreaker.State.HALF_OPEN, cb.currentState());

            // Simulate the probing request being blocked by an upcoming rule,
            // which triggers the terminate handler when the entry exits.
            Mockito.when(entry.getBlockError()).thenReturn(new DegradeException(rule.getLimitApp(), rule));
            handlerRef.get().accept(context, entry);

            assertEquals(CircuitBreaker.State.OPEN, cb.currentState());
            // The retry timestamp must be updated after rollback; otherwise the next
            // request would trigger a new probe immediately and keep hitting the
            // downstream in a tight loop.
            assertFalse(cb.retryTimeoutArrived());
        }
    }

    @Test
    public void testHalfOpenProbeTimeoutFallback() {
        try (MockedStatic<TimeUtil> mocked = super.mockTimeUtil()) {
            // Start from a non-zero timestamp so that the probe start time is
            // distinguishable from the "not started" state (0).
            setCurrentMillis(mocked, 1_000_000L);

            DegradeRule rule = new DegradeRule("abc")
                .setCount(0.2d)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setStatIntervalMs(20 * 1000)
                .setTimeWindow(10)
                .setMinRequestAmount(1);

            AbstractCircuitBreaker cb = new ExceptionCircuitBreaker(rule);

            Context context = new Context(null, "ctx");
            Entry entry = Mockito.mock(Entry.class);
            context.setCurEntry(entry);

            // Trigger the circuit to OPEN first, then let the recovery window pass.
            cb.transformToOpen(1.0d);
            assertEquals(CircuitBreaker.State.OPEN, cb.currentState());
            sleep(mocked, 10 * 1000 + 1000);

            assertTrue(cb.tryPass(context));
            assertEquals(CircuitBreaker.State.HALF_OPEN, cb.currentState());

            // The probing request gets stuck and never completes.
            // Advance time beyond the recovery timeout.
            sleep(mocked, 10 * 1000 + 1000);

            // The next request detects the stuck probe and falls back to OPEN.
            assertFalse(cb.tryPass(context));
            assertEquals(CircuitBreaker.State.OPEN, cb.currentState());

            // After another recovery window, probing is allowed again.
            sleep(mocked, 10 * 1000);
            assertTrue(cb.tryPass(context));
            assertEquals(CircuitBreaker.State.HALF_OPEN, cb.currentState());
        }
    }
}