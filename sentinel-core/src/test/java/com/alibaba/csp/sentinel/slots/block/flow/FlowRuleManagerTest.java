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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link FlowRuleManager}.
 *
 * @author Lovnx
 */
public class FlowRuleManagerTest {

    @Test
    public void testAddRule() {
        FlowRule rule1 = new FlowRule("newRule");
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setCount(1);
        rule1.setLimitApp("default");
        rule1.setStrategy(RuleConstant.STRATEGY_DIRECT);
        FlowRuleManager.addRule(rule1);
        assertTrue(FlowRuleManager.hasConfig("newRule"));

        FlowRule rule2 = new FlowRule("abc");
        rule2.setCount(-1);
        FlowRuleManager.addRule(rule2);
        assertFalse(FlowRuleManager.hasConfig("abc"));

        FlowRule rule3 = new FlowRule("newRule");
        rule3.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule3.setCount(1);
        rule3.setLimitApp("default");
        rule3.setStrategy(RuleConstant.STRATEGY_DIRECT);
        FlowRuleManager.addRule(rule3);
        List<FlowRule> rules = FlowRuleManager.getRules();
        for (FlowRule r : rules){
            assertTrue(r == rule1 || r == rule3);
        }
    }
}