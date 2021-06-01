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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class GatewayRuleManagerTest {

    @Test
    public void testLoadAndGetGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        String ahasRoute = "ahas_route";
        GatewayFlowRule rule1 = new GatewayFlowRule(ahasRoute)
            .setCount(500)
            .setIntervalSec(1);
        GatewayFlowRule rule2 = new GatewayFlowRule(ahasRoute)
            .setCount(20)
            .setIntervalSec(2)
            .setBurst(5)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            );
        GatewayFlowRule rule3 = new GatewayFlowRule("complex_route_ZZZ")
            .setCount(10)
            .setIntervalSec(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(600)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName("X-Sentinel-Flag")
            );
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        GatewayRuleManager.loadRules(rules);

        List<ParamFlowRule> convertedRules = GatewayRuleManager.getConvertedParamRules(ahasRoute);
        assertNotNull(convertedRules);
        assertEquals(0, (int)rule2.getParamItem().getIndex());
        assertEquals(0, (int)rule3.getParamItem().getIndex());
        assertTrue(GatewayRuleManager.getRulesForResource(ahasRoute).contains(rule1));
        assertTrue(GatewayRuleManager.getRulesForResource(ahasRoute).contains(rule2));
    }

    @Test
    public void testIsValidRule() {
        GatewayFlowRule bad1 = new GatewayFlowRule();
        GatewayFlowRule bad2 = new GatewayFlowRule("abc")
            .setIntervalSec(0);
        GatewayFlowRule bad3 = new GatewayFlowRule("abc")
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM));
        GatewayFlowRule bad4 = new GatewayFlowRule("abc")
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName("p")
                .setPattern("def")
                .setMatchStrategy(-1)
            );
        GatewayFlowRule good1 = new GatewayFlowRule("abc");
        GatewayFlowRule good2 = new GatewayFlowRule("abc")
            .setParamItem(new GatewayParamFlowItem().setParseStrategy(0));
        GatewayFlowRule good3 = new GatewayFlowRule("abc")
            .setParamItem(new GatewayParamFlowItem()
                .setMatchStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName("Origin")
                .setPattern("def"));
        assertFalse(GatewayRuleManager.isValidRule(bad1));
        assertFalse(GatewayRuleManager.isValidRule(bad2));
        assertFalse(GatewayRuleManager.isValidRule(bad3));
        assertFalse(GatewayRuleManager.isValidRule(bad4));

        assertTrue(GatewayRuleManager.isValidRule(good1));
        assertTrue(GatewayRuleManager.isValidRule(good2));
        assertTrue(GatewayRuleManager.isValidRule(good3));
    }

    @Before
    public void setUp() {
        GatewayRuleManager.loadRules(new HashSet<GatewayFlowRule>());
    }

    @After
    public void tearDown() {
        GatewayRuleManager.loadRules(new HashSet<GatewayFlowRule>());
    }
}