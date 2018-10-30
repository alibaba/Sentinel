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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link DegradeRuleManager}.
 *
 * @author Eric Zhao
 */
public class DegradeRuleManagerTest {

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
        assertFalse(DegradeRuleManager.isValidRule(rule1));
        assertFalse(DegradeRuleManager.isValidRule(rule2));
        assertFalse(DegradeRuleManager.isValidRule(rule3));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(1.0d)));
        assertTrue(DegradeRuleManager.isValidRule(rule3.setCount(0.0d)));
        assertFalse(DegradeRuleManager.isValidRule(rule4));
    }
}