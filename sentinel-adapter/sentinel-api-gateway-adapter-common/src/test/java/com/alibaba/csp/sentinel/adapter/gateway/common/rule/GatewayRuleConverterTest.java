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
import java.util.Arrays;
import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 * @author Renyansong
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

    @Test
    public void testConvertAndApplyToMultipleParamRule() {
        GatewayFlowRule routeRule1 = new GatewayFlowRule("routeId1")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                        .setGatewayFieldFlowItemList(
                                Arrays.asList(
                                        new GatewayFieldFlowItem()
                                                .setClassType(String.class.getName())
                                                .setCount(3)
                                                .setObject("192.168.1.7"),
                                        new GatewayFieldFlowItem()
                                                .setClassType(String.class.getName())
                                                .setCount(5)
                                                .setObject("192.168.1.8")
                                )
                        )
                );
        int idx = 1;
        ParamFlowRule paramRule1 = GatewayRuleConverter.applyToParamRule(routeRule1, idx);
        assertEquals(routeRule1.getResource(), paramRule1.getResource());
        assertEquals(routeRule1.getCount(), paramRule1.getCount(), 0.01);
        assertEquals(routeRule1.getControlBehavior(), paramRule1.getControlBehavior());
        assertEquals(routeRule1.getIntervalSec(), paramRule1.getDurationInSec());
        assertEquals(routeRule1.getBurst(), paramRule1.getBurstCount());
        assertEquals(idx, (int)paramRule1.getParamIdx());
        assertEquals(idx, (int)routeRule1.getParamItem().getIndex());
        assertEquals(routeRule1.getParamItem().getGatewayFieldFlowItemList().size(), paramRule1.getParamFlowItemList().size());

        GatewayFlowRule routeRule2 = new GatewayFlowRule("routeId2")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("X-Sentinel-Flag")
                        .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT)
                        .setGatewayFieldFlowItemList(
                                Arrays.asList(
                                        new GatewayFieldFlowItem()
                                                .setClassType(String.class.getName())
                                                .setCount(3)
                                                .setObject("Flag1"),
                                        new GatewayFieldFlowItem()
                                                .setClassType(String.class.getName())
                                                .setCount(5)
                                                .setObject("Flag2")
                                )
                        )
                );
        ParamFlowRule paramRule2 = GatewayRuleConverter.applyToParamRule(routeRule2, idx);
        assertEquals(routeRule2.getResource(), paramRule2.getResource());
        assertEquals(routeRule2.getCount(), paramRule2.getCount(), 0.01);
        assertEquals(routeRule2.getControlBehavior(), paramRule2.getControlBehavior());
        assertEquals(routeRule2.getIntervalSec(), paramRule2.getDurationInSec());
        assertEquals(routeRule2.getBurst(), paramRule2.getBurstCount());
        assertEquals(idx, (int)paramRule2.getParamIdx());
        assertEquals(idx, (int)routeRule2.getParamItem().getIndex());
        assertEquals(routeRule2.getParamItem().getGatewayFieldFlowItemList().size(), paramRule2.getParamFlowItemList().size());

    }

}