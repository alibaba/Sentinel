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
package com.alibaba.csp.sentinel.dubbo.springboot.utils;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ConstStringsTest {

    @Test
    public void flowGradeString() {
        String s = ConstStrings.flowGradeString(RuleConstant.FLOW_GRADE_QPS);
        assertEquals("FLOW_GRADE_QPS", s);
    }

    @Test
    public void degradeGradeString() {
        String s = ConstStrings.degradeGradeString(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        assertEquals("DEGRADE_GRADE_EXCEPTION_RATIO", s);
    }

    @Test
    public void behaviorGradeString() {
        String s = ConstStrings.behaviorGradeString(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        assertEquals("CONTROL_BEHAVIOR_RATE_LIMITER", s);
    }
}