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
 * @author Eric Zhao
 */
public class DegradeRuleTest {

    @Test
    public void testRuleEquals() {
        DegradeRule degradeRule1 = new DegradeRule();
        DegradeRule degradeRule2 = new DegradeRule();

        int minRequestAmount = 20;
        double count = 1.0;
        int timeWindow = 2;
        degradeRule1.setMinRequestAmount(minRequestAmount);
        degradeRule1.setCount(count);
        degradeRule1.setTimeWindow(timeWindow);
        degradeRule1.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        degradeRule2.setMinRequestAmount(minRequestAmount);
        degradeRule2.setCount(count);
        degradeRule2.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule2.setTimeWindow(timeWindow);
        assertEquals(degradeRule1, degradeRule2);

        degradeRule2.setMinRequestAmount(100);
        assertNotEquals(degradeRule1, degradeRule2);
    }
}