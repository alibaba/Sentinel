/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.webflow.rule;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author guanyu
 */
public class WebFlowRuleConverterTest {

    @Test
    public void testConvertToFlowRule() {
        WebFlowRule rule = new WebFlowRule("routeId1")
                .setId(1L)
                .setCount(10d)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(1000);
        FlowRule flowRule = WebFlowRuleConverter.toFlowRule(rule);
        assertEquals(rule.getResource(), flowRule.getResource());
        assertEquals(rule.getCount(), flowRule.getCount(), 0.01);
        assertEquals(rule.getControlBehavior(), flowRule.getControlBehavior());
        assertEquals(rule.getMaxQueueingTimeoutMs(), flowRule.getMaxQueueingTimeMs());
    }

    @Test
    public void testConvertAndApplyToParamRule() {
        WebFlowRule routeRule1 = new WebFlowRule("routeId1")
                .setId(1L)
                .setCount(2d)
                .setBurst(2)
                .setParamItem(new WebParamItem()
                .setParseStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            );
        ParamFlowRule paramRule = WebFlowRuleConverter.applyToParamRule(routeRule1);
        assertEquals(routeRule1.getResource(), paramRule.getResource());
        assertEquals(routeRule1.getCount(), paramRule.getCount(), 0.01);
        assertEquals(routeRule1.getControlBehavior(), paramRule.getControlBehavior());
        assertEquals(routeRule1.getBurst(), paramRule.getBurstCount());
        assertNull(paramRule.getParamIdx());
        assertNotNull(paramRule.getParamKey());
    }
}