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
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author gaunyu
 */
public class WebFlowRuleManagerTest {

    @Test
    public void testLoadAndGetGatewayRules() {
        Set<WebFlowRule> rules = new HashSet<WebFlowRule>();
        String mseRoute = "mse_route";
        WebFlowRule rule1 = new WebFlowRule(mseRoute)
            .setCount(500d);
        WebFlowRule rule2 = new WebFlowRule(mseRoute)
            .setCount(20d)
            .setBurst(5)
            .setParamItem(new WebParamItem()
                .setParseStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            );
        WebFlowRule rule3 = new WebFlowRule("complex_route_ZZZ")
            .setCount(10d)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(600)
            .setParamItem(new WebParamItem()
                .setParseStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName("X-Sentinel-Flag")
            );
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        WebFlowRuleManager.loadRules(rules);

        List<ParamFlowRule> convertedRules = WebFlowRuleManager.getConvertedParamRules(mseRoute);
        assertNotNull(convertedRules);
        assertTrue(WebFlowRuleManager.getRulesForResource(mseRoute).contains(rule1));
        assertTrue(WebFlowRuleManager.getRulesForResource(mseRoute).contains(rule2));
    }

    @Test
    public void testIsValidRule() {
        WebFlowRule bad1 = new WebFlowRule();
        WebFlowRule bad2 = new WebFlowRule("abc");
        WebFlowRule bad3 = new WebFlowRule("abc")
            .setParamItem(new WebParamItem()
                .setParseStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_URL_PARAM));
        WebFlowRule bad4 = new WebFlowRule("abc")
            .setParamItem(new WebParamItem()
                .setParseStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName("p")
                .setPattern("def")
                .setMatchStrategy(-1)
            );
        WebFlowRule good1 = new WebFlowRule("abc")
                .setCount(1d);
        WebFlowRule good2 = new WebFlowRule("abc")
                .setCount(1d)
            .setParamItem(new WebParamItem().setParseStrategy(0));
        WebFlowRule good3 = new WebFlowRule("abc")
                .setCount(1d)
            .setParamItem(new WebParamItem()
                .setMatchStrategy(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName("Origin")
                .setPattern("def"));
        assertFalse(WebFlowRuleManager.isValidRule(bad1));
        assertFalse(WebFlowRuleManager.isValidRule(bad2));
        assertFalse(WebFlowRuleManager.isValidRule(bad3));
        assertFalse(WebFlowRuleManager.isValidRule(bad4));

        assertTrue(WebFlowRuleManager.isValidRule(good1));
        assertTrue(WebFlowRuleManager.isValidRule(good2));
        assertTrue(WebFlowRuleManager.isValidRule(good3));
    }

    @Before
    public void setUp() {
        WebFlowRuleManager.loadRules(new HashSet<WebFlowRule>());
    }

    @After
    public void tearDown() {
        WebFlowRuleManager.loadRules(new HashSet<WebFlowRule>());
    }
}