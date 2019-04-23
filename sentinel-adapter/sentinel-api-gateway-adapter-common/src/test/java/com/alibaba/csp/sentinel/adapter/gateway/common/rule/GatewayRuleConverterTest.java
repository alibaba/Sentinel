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
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class GatewayRuleConverterTest {

    @Test
    public void testConvertToFlowRule() {
        GatewayFlowRule rule = new GatewayFlowRule("routeId1")
            .setCount(10)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(1000);
        FlowRule flowRule = GatewayRuleConverter.toFlowRule(rule);
        assertEquals(rule.getResource(), flowRule.getResource());
        assertEquals(rule.getCount(), flowRule.getCount(), 0.01);
        assertEquals(rule.getControlBehavior(), flowRule.getControlBehavior());
        assertEquals(rule.getMaxQueueingTimeoutMs(), flowRule.getMaxQueueingTimeMs());
    }

    @Test
    public void testConvertAndApplyToParamRule() {
        GatewayFlowRule routeRule1 = new GatewayFlowRule("routeId1")
            .setCount(2)
            .setIntervalSec(2)
            .setBurst(2)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            );
        int idx = 1;
        ParamFlowRule paramRule = GatewayRuleConverter.applyToParamRule(routeRule1, idx);
        assertEquals(routeRule1.getResource(), paramRule.getResource());
        assertEquals(routeRule1.getCount(), paramRule.getCount(), 0.01);
        assertEquals(routeRule1.getControlBehavior(), paramRule.getControlBehavior());
        assertEquals(routeRule1.getIntervalSec(), paramRule.getDurationInSec());
        assertEquals(routeRule1.getBurst(), paramRule.getBurstCount());
        assertEquals(idx, (int)paramRule.getParamIdx());
        assertEquals(idx, (int)routeRule1.getParamItem().getIndex());
    }
}