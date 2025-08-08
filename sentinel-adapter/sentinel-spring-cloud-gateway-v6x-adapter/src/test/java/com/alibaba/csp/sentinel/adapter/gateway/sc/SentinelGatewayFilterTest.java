package com.alibaba.csp.sentinel.adapter.gateway.sc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link SentinelGatewayFilter}.
 *
 * @author Eric Zhao
 */
public class SentinelGatewayFilterTest {

    @Test
    public void testPickMatchingApiDefinitions() {
        // Mock a request.
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);
        RequestPath requestPath = mock(RequestPath.class);
        when(request.getPath()).thenReturn(requestPath);

        // Prepare API definitions.
        Set<ApiDefinition> apiDefinitions = new HashSet<>();
        String apiName1 = "some_customized_api";
        ApiDefinition api1 = new ApiDefinition(apiName1)
            .setPredicateItems(Collections.singleton(
                new ApiPathPredicateItem().setPattern("/product/**")
                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
            ));
        String apiName2 = "another_customized_api";
        ApiDefinition api2 = new ApiDefinition(apiName2)
            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                add(new ApiPathPredicateItem().setPattern("/something"));
                add(new ApiPathPredicateItem().setPattern("/other/**")
                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
            }});
        apiDefinitions.add(api1);
        apiDefinitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(apiDefinitions);
        SentinelGatewayFilter filter = new SentinelGatewayFilter();

        when(requestPath.value()).thenReturn("/product/123");
        Set<String> matchingApis = filter.pickMatchingApiDefinitions(exchange);
        assertThat(matchingApis.size()).isEqualTo(1);
        assertThat(matchingApis.contains(apiName1)).isTrue();

        when(requestPath.value()).thenReturn("/products");
        assertThat(filter.pickMatchingApiDefinitions(exchange).size()).isZero();

        when(requestPath.value()).thenReturn("/something");
        matchingApis = filter.pickMatchingApiDefinitions(exchange);
        assertThat(matchingApis.size()).isEqualTo(1);
        assertThat(matchingApis.contains(apiName2)).isTrue();

        when(requestPath.value()).thenReturn("/other/foo/3");
        matchingApis = filter.pickMatchingApiDefinitions(exchange);
        assertThat(matchingApis.size()).isEqualTo(1);
        assertThat(matchingApis.contains(apiName2)).isTrue();
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