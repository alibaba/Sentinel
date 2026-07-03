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
package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Zhao
 */
@SuppressWarnings("unchecked")
public class GatewayParamParserTest {

    private final Predicate<GatewayFlowRule> routeIdPredicate = new Predicate<GatewayFlowRule>() {
        @Override
        public boolean test(GatewayFlowRule e) {
            return e.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID;
        }
    };
    private final Predicate<GatewayFlowRule> apiNamePredicate = new Predicate<GatewayFlowRule>() {
        @Override
        public boolean test(GatewayFlowRule e) {
            return e.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME;
        }
    };

    @Test
    public void testParseParametersNoParamItem() {
        RequestItemParser<Object> itemParser = mock(RequestItemParser.class);
        GatewayParamParser<Object> parser = new GatewayParamParser<>(itemParser);
        // Create a fake request.
        Object request = new Object();
        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        String routeId1 = "my_test_route_A";
        rules.add(new GatewayFlowRule(routeId1)
            .setCount(5)
            .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule(routeId1)
            .setCount(10)
            .setControlBehavior(2)
            .setMaxQueueingTimeoutMs(1000)
        );
        GatewayRuleManager.loadRules(rules);

        Object[] params = parser.parseParameterFor(routeId1, request, routeIdPredicate);
        assertThat(params.length).isEqualTo(1);
    }

    @Test
    public void testParseParametersWithItems() {
        RequestItemParser<Object> itemParser = mock(RequestItemParser.class);
        GatewayParamParser<Object> paramParser = new GatewayParamParser<>(itemParser);
        // Create a fake request.
        Object request = new Object();

        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        final String routeId1 = "my_test_route_A";
        final String api1 = "my_test_route_B";
        final String headerName = "X-Sentinel-Flag";
        final String paramName = "p";
        final String cookieName = "myCookie";
        GatewayFlowRule routeRuleNoParam = new GatewayFlowRule(routeId1)
            .setCount(10)
            .setIntervalSec(10);
        GatewayFlowRule routeRule1 = new GatewayFlowRule(routeId1)
            .setCount(2)
            .setIntervalSec(2)
            .setBurst(2)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            );
        GatewayFlowRule routeRule2 = new GatewayFlowRule(routeId1)
            .setCount(10)
            .setIntervalSec(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(600)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName(headerName)
            );
        GatewayFlowRule routeRule3 = new GatewayFlowRule(routeId1)
            .setCount(20)
            .setIntervalSec(1)
            .setBurst(5)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName(paramName)
            );
        GatewayFlowRule routeRule4 = new GatewayFlowRule(routeId1)
            .setCount(120)
            .setIntervalSec(10)
            .setBurst(30)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HOST)
            );
        GatewayFlowRule routeRule5 = new GatewayFlowRule(routeId1)
            .setCount(50)
            .setIntervalSec(30)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_COOKIE)
                .setFieldName(cookieName)
            );
        GatewayFlowRule apiRule1 = new GatewayFlowRule(api1)
            .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
            .setCount(5)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName(paramName)
            );
        rules.add(routeRule1);
        rules.add(routeRule2);
        rules.add(routeRule3);
        rules.add(routeRule4);
        rules.add(routeRule5);
        rules.add(routeRuleNoParam);
        rules.add(apiRule1);
        GatewayRuleManager.loadRules(rules);

        final String expectedHost = "hello.test.sentinel";
        final String expectedAddress = "66.77.88.99";
        final String expectedHeaderValue1 = "Sentinel";
        final String expectedUrlParamValue1 = "17";
        final String expectedCookieValue1 = "Sentinel-Foo";

        mockClientHostAddress(itemParser, expectedAddress);
        Map<String, String> expectedHeaders = new HashMap<String, String>() {{
            put(headerName, expectedHeaderValue1); put("Host", expectedHost);
        }};
        mockHeaders(itemParser, expectedHeaders);
        mockSingleUrlParam(itemParser, paramName, expectedUrlParamValue1);
        mockSingleCookie(itemParser, cookieName, expectedCookieValue1);

        Object[] params = paramParser.parseParameterFor(routeId1, request, routeIdPredicate);
        // Param length should be 6 (5 with parameters, 1 normal flow with generated constant)
        assertThat(params.length).isEqualTo(6);
        assertThat(params[routeRule1.getParamItem().getIndex()]).isEqualTo(expectedAddress);
        assertThat(params[routeRule2.getParamItem().getIndex()]).isEqualTo(expectedHeaderValue1);
        assertThat(params[routeRule3.getParamItem().getIndex()]).isEqualTo(expectedUrlParamValue1);
        assertThat(params[routeRule4.getParamItem().getIndex()]).isEqualTo(expectedHost);
        assertThat(params[routeRule5.getParamItem().getIndex()]).isEqualTo(expectedCookieValue1);
        assertThat(params[params.length - 1]).isEqualTo(SentinelGatewayConstants.GATEWAY_DEFAULT_PARAM);

        assertThat(paramParser.parseParameterFor(api1, request, routeIdPredicate).length).isZero();

        String expectedUrlParamValue2 = "fs";
        mockSingleUrlParam(itemParser, paramName, expectedUrlParamValue2);
        params = paramParser.parseParameterFor(api1, request, apiNamePredicate);
        assertThat(params.length).isEqualTo(1);
        assertThat(params[apiRule1.getParamItem().getIndex()]).isEqualTo(expectedUrlParamValue2);
    }

    @Test
    public void testParseParametersWithEmptyItemPattern() {
        RequestItemParser<Object> itemParser = mock(RequestItemParser.class);
        GatewayParamParser<Object> paramParser = new GatewayParamParser<>(itemParser);
        // Create a fake request.
        Object request = new Object();
        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        final String routeId = "my_test_route_DS(*H";
        final String headerName = "X-Sentinel-Flag";
        GatewayFlowRule routeRule1 = new GatewayFlowRule(routeId)
            .setCount(10)
            .setIntervalSec(2)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName(headerName)
                .setPattern("")
                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT)
            );
        rules.add(routeRule1);
        GatewayRuleManager.loadRules(rules);

        mockSingleHeader(itemParser, headerName, "Sent1nel");
        Object[] params = paramParser.parseParameterFor(routeId, request, routeIdPredicate);
        assertThat(params.length).isEqualTo(1);
        // Empty pattern should not take effect.
        assertThat(params[routeRule1.getParamItem().getIndex()]).isEqualTo("Sent1nel");
    }

    @Test
    public void testParseParametersWithItemPatternMatching() {
        RequestItemParser<Object> itemParser = mock(RequestItemParser.class);
        GatewayParamParser<Object> paramParser = new GatewayParamParser<>(itemParser);
        // Create a fake request.
        Object request = new Object();

        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        final String routeId1 = "my_test_route_F&@";
        final String api1 = "my_test_route_E5K";
        final String headerName = "X-Sentinel-Flag";
        final String paramName = "p";

        String nameEquals = "Wow";
        String nameContains = "warn";
        String valueRegex = "\\d+";
        GatewayFlowRule routeRule1 = new GatewayFlowRule(routeId1)
            .setCount(10)
            .setIntervalSec(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(600)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName(headerName)
                .setPattern(nameEquals)
                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT)
            );
        GatewayFlowRule routeRule2 = new GatewayFlowRule(routeId1)
            .setCount(20)
            .setIntervalSec(1)
            .setBurst(5)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName(paramName)
                .setPattern(nameContains)
                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS)
            );
        GatewayFlowRule apiRule1 = new GatewayFlowRule(api1)
            .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
            .setCount(5)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName(paramName)
                .setPattern(valueRegex)
                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_REGEX)
            );
        rules.add(routeRule1);
        rules.add(routeRule2);
        rules.add(apiRule1);
        GatewayRuleManager.loadRules(rules);

        mockSingleHeader(itemParser, headerName, nameEquals);
        mockSingleUrlParam(itemParser, paramName, nameContains);
        Object[] params = paramParser.parseParameterFor(routeId1, request, routeIdPredicate);
        assertThat(params.length).isEqualTo(2);
        assertThat(params[routeRule1.getParamItem().getIndex()]).isEqualTo(nameEquals);
        assertThat(params[routeRule2.getParamItem().getIndex()]).isEqualTo(nameContains);

        mockSingleHeader(itemParser, headerName, nameEquals + "_foo");
        mockSingleUrlParam(itemParser, paramName, nameContains + "_foo");
        params = paramParser.parseParameterFor(routeId1, request, routeIdPredicate);
        assertThat(params.length).isEqualTo(2);
        assertThat(params[routeRule1.getParamItem().getIndex()])
            .isEqualTo(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
        assertThat(params[routeRule2.getParamItem().getIndex()])
            .isEqualTo(nameContains + "_foo");

        mockSingleHeader(itemParser, headerName, "foo");
        mockSingleUrlParam(itemParser, paramName, "foo");
        params = paramParser.parseParameterFor(routeId1, request, routeIdPredicate);
        assertThat(params.length).isEqualTo(2);
        assertThat(params[routeRule1.getParamItem().getIndex()])
            .isEqualTo(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
        assertThat(params[routeRule2.getParamItem().getIndex()])
            .isEqualTo(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);

        mockSingleUrlParam(itemParser, paramName, "23");
        params = paramParser.parseParameterFor(api1, request, apiNamePredicate);
        assertThat(params.length).isEqualTo(1);
        assertThat(params[apiRule1.getParamItem().getIndex()]).isEqualTo("23");

        mockSingleUrlParam(itemParser, paramName, "some233");
        params = paramParser.parseParameterFor(api1, request, apiNamePredicate);
        assertThat(params.length).isEqualTo(1);
        assertThat(params[apiRule1.getParamItem().getIndex()])
            .isEqualTo(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    private void mockClientHostAddress(/*@Mock*/ RequestItemParser parser, String address) {
        when(parser.getRemoteAddress(any())).thenReturn(address);
    }

    private void mockHeaders(/*@Mock*/ RequestItemParser parser, Map<String, String> headerMap) {
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            when(parser.getHeader(any(), eq(e.getKey()))).thenReturn(e.getValue());
        }
    }

    private void mockUrlParams(/*@Mock*/ RequestItemParser parser, Map<String, String> paramMap) {
        for (Map.Entry<String, String> e : paramMap.entrySet()) {
            when(parser.getUrlParam(any(), eq(e.getKey()))).thenReturn(e.getValue());
        }
    }

    private void mockSingleUrlParam(/*@Mock*/ RequestItemParser parser, String key, String value) {
        when(parser.getUrlParam(any(), eq(key))).thenReturn(value);
    }

    private void mockSingleHeader(/*@Mock*/ RequestItemParser parser, String key, String value) {
        when(parser.getHeader(any(), eq(key))).thenReturn(value);
    }

    private void mockSingleCookie(/*@Mock*/ RequestItemParser parser, String key, String value) {
        when(parser.getCookieValue(any(), eq(key))).thenReturn(value);
    }

    @Before
    public void setUp() {
        GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<ApiDefinition>());
        GatewayRuleManager.loadRules(new HashSet<GatewayFlowRule>());
        GatewayRegexCache.clear();
    }

    @After
    public void tearDown() {
        GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<ApiDefinition>());
        GatewayRuleManager.loadRules(new HashSet<GatewayFlowRule>());
        GatewayRegexCache.clear();
    }
}
