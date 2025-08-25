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
package com.alibaba.csp.sentinel.adapter.gateway.sc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Zhao
 */
public class SpringCloudGatewayParamParserTest {

    private final GatewayParamParser<ServerWebExchange> paramParser = new GatewayParamParser<>(
        new ServerWebExchangeItemParser()
    );

    @Test
    public void testParseParametersNoParamItem() {
        // Mock a request.
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        String routeId1 = "my_test_route_A";
        rules.add(new GatewayFlowRule(routeId1)
            .setCount(5)
            .setIntervalSec(1)
        );
        GatewayRuleManager.loadRules(rules);

        Object[] params = paramParser.parseParameterFor(routeId1, exchange,
            e -> e.getResourceMode() == 0);
        assertThat(params.length).isEqualTo(1);
    }

    @Test
    public void testParseParametersWithItems() {
        // Mock a request.
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);
        RequestPath requestPath = mock(RequestPath.class);
        when(request.getPath()).thenReturn(requestPath);

        // Prepare gateway rules.
        Set<GatewayFlowRule> rules = new HashSet<>();
        String routeId1 = "my_test_route_A";
        String api1 = "my_test_route_B";
        String headerName = "X-Sentinel-Flag";
        String paramName = "p";
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
        rules.add(apiRule1);
        GatewayRuleManager.loadRules(rules);

        String expectedHost = "hello.test.sentinel";
        String expectedAddress = "66.77.88.99";
        String expectedHeaderValue1 = "Sentinel";
        String expectedUrlParamValue1 = "17";
        mockClientHostAddress(request, expectedAddress);
        Map<String, String> expectedHeaders = new HashMap<String, String>() {{
            put(headerName, expectedHeaderValue1); put("Host", expectedHost);
        }};
        mockHeaders(request, expectedHeaders);
        mockSingleUrlParam(request, paramName, expectedUrlParamValue1);
        Object[] params = paramParser.parseParameterFor(routeId1, exchange, e -> e.getResourceMode() == 0);
        assertThat(params.length).isEqualTo(4);
        assertThat(params[routeRule1.getParamItem().getIndex()]).isEqualTo(expectedAddress);
        assertThat(params[routeRule2.getParamItem().getIndex()]).isEqualTo(expectedHeaderValue1);
        assertThat(params[routeRule3.getParamItem().getIndex()]).isEqualTo(expectedUrlParamValue1);
        assertThat(params[routeRule4.getParamItem().getIndex()]).isEqualTo(expectedHost);

        assertThat(paramParser.parseParameterFor(api1, exchange, e -> e.getResourceMode() == 0).length).isZero();

        String expectedUrlParamValue2 = "fs";
        mockSingleUrlParam(request, paramName, expectedUrlParamValue2);
        params = paramParser.parseParameterFor(api1, exchange, e -> e.getResourceMode() == 1);
        assertThat(params.length).isEqualTo(1);
        assertThat(params[apiRule1.getParamItem().getIndex()]).isEqualTo(expectedUrlParamValue2);
    }

    private void mockClientHostAddress(/*@Mock*/ ServerHttpRequest request, String address) {
        InetSocketAddress socketAddress = mock(InetSocketAddress.class);
        when(request.getRemoteAddress()).thenReturn(socketAddress);
        InetAddress inetAddress = mock(InetAddress.class);
        when(inetAddress.getHostAddress()).thenReturn(address);
        when(socketAddress.getAddress()).thenReturn(inetAddress);
    }

    private void mockHeaders(/*@Mock*/ ServerHttpRequest request, Map<String, String> headerMap) {
        HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getHeaders()).thenReturn(headers);
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            when(headers.getFirst(e.getKey())).thenReturn(e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void mockUrlParams(/*@Mock*/ ServerHttpRequest request, Map<String, String> paramMap) {
        MultiValueMap<String, String> urlParams = mock(MultiValueMap.class);
        when(request.getQueryParams()).thenReturn(urlParams);
        for (Map.Entry<String, String> e : paramMap.entrySet()) {
            when(urlParams.getFirst(e.getKey())).thenReturn(e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void mockSingleUrlParam(/*@Mock*/ ServerHttpRequest request, String key, String value) {
        MultiValueMap<String, String> urlParams = mock(MultiValueMap.class);
        when(request.getQueryParams()).thenReturn(urlParams);
        when(urlParams.getFirst(key)).thenReturn(value);
    }

    private void mockSingleHeader(/*@Mock*/ ServerHttpRequest request, String key, String value) {
        HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(key)).thenReturn(value);
    }

    @Before
    public void setUp() {
        GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<>());
        GatewayRuleManager.loadRules(new HashSet<>());
    }

    @After
    public void tearDown() {
        GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<>());
        GatewayRuleManager.loadRules(new HashSet<>());
    }
}
