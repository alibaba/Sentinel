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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author Eric Zhao
 */
public class DefaultCircuitBreakerSlotTest {

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
        rules.add(rule);
        DefaultCircuitBreakerRuleManager.loadRules(rules);
    }

    @After
    public void tearDown() throws Exception {
        DefaultCircuitBreakerRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testPerformChecking() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DefaultCircuitBreakerSlot defaultCircuitBreakerSlot = mock(DefaultCircuitBreakerSlot.class);
        Context context = mock(Context.class);
        String resA = "resA";
        Method pCMethod = DefaultCircuitBreakerSlot.class.getDeclaredMethod("performChecking", Context.class, ResourceWrapper.class);
        pCMethod.setAccessible(true);
        pCMethod.invoke(defaultCircuitBreakerSlot, context, new StringResourceWrapper(resA, EntryType.IN));
    }

}
