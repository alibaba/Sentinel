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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.mockito.MockedStatic;

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
}