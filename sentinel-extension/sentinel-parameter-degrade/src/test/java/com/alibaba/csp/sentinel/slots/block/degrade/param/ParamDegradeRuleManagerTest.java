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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link ParamDegradeRuleManager}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamDegradeRuleManagerTest {

    @Before
    public void setUp() {
        ParamDegradeRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        ParamDegradeRuleManager.loadRules(null);
    }

    @Test
    public void testLoadRules() {
        final String resA = "resA";
        ParamDegradeRule ruleA = new ParamDegradeRule(resA).setParamIdx(0);
        ruleA.setCount(1d);
        ruleA.setSlowRatioThreshold(0.9d);
        ruleA.setTimeWindow(20);
        ruleA.setStatIntervalMs(20000);

        ParamDegradeItem item = new ParamDegradeItem();
        item.setObject("1");
        item.setCount(2d);
        item.setClassType("java.util.String");

        ruleA.setParamDegradeItemList(Collections.singletonList(item));

        ParamDegradeRuleManager.loadRules(Collections.singletonList(ruleA));

        CircuitBreaker cb = ParamDegradeRuleManager.getCircuitBreakers(resA, ParamDegradeRuleManager.getItemKey(ruleA, item)).get(0);

        assertEquals(ruleA.getResource(), cb.getRule().getResource());
        assertEquals(item.getCount(), cb.getRule().getCount(), 0.1d);
        assertEquals(ruleA.getSlowRatioThreshold(), cb.getRule().getSlowRatioThreshold(), 0.1d);
        assertEquals(ruleA.getTimeWindow(), cb.getRule().getTimeWindow());
        assertEquals(ruleA.getStatIntervalMs(), cb.getRule().getStatIntervalMs());

        assertEquals(ruleA.getResource(), ParamDegradeRuleManager.getRules().get(0).getResource());

        assertTrue(ParamDegradeRuleManager.hasRules(ruleA.getResource()));
    }

    @Test
    public void testValidRule() {

        assertFalse(ParamDegradeRuleManager.isValidRule(null));
        assertFalse(ParamDegradeRuleManager.isValidRule(new ParamDegradeRule()));

        final String resA = "resA";
        ParamDegradeRule ruleA = new ParamDegradeRule(resA).setParamIdx(0);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setCount(1d);
        ruleA.setSlowRatioThreshold(0.9d);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setTimeWindow(20);
        ruleA.setStatIntervalMs(-1000);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setStatIntervalMs(20000);

        ruleA.setSlowRatioThreshold(-0.9d);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setSlowRatioThreshold(0.9d);

        ruleA.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        ruleA.setCount(1.1d);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setCount(1d);
        ruleA.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        ruleA.setGrade(-1);
        assertFalse(ParamDegradeRuleManager.isValidRule(ruleA));
        ruleA.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        ParamDegradeItem item = new ParamDegradeItem();
        item.setObject("1");
        item.setCount(2d);
        item.setClassType("java.util.String");

        ruleA.setParamDegradeItemList(Collections.singletonList(item));

        assertTrue(ParamDegradeRuleManager.isValidRule(ruleA));

        ruleA.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        assertTrue(ParamDegradeRuleManager.isValidRule(ruleA));
    }
}
