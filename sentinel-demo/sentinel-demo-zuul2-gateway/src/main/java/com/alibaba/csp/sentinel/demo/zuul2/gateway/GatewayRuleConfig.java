package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.HashSet;
import java.util.Set;

public class GatewayRuleConfig {

    public void doInit() {
        // Prepare some gateway rules and API definitions (only for demo).
        // It's recommended to leverage dynamic data source or the Sentinel dashboard to push the rules.
        initCustomizedApis();
        initGatewayRules();
    }

    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("some_customized_api")
            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                add(new ApiPathPredicateItem().setPattern("/images"));
                add(new ApiPathPredicateItem().setPattern("/comments")
                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
            }});
        ApiDefinition api2 = new ApiDefinition("another_customized_api")
            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                add(new ApiPathPredicateItem().setPattern("/**")
                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
            }});
        definitions.add(api1);
        definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("images")
            .setCount(10)
            .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("images")
            .setCount(2)
            .setIntervalSec(2)
            .setBurst(2)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            )
        );
        rules.add(new GatewayFlowRule("comments")
            .setCount(3)
            .setIntervalSec(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            .setMaxQueueingTimeoutMs(6000)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName("X-Sentinel-Flag")
            )
        );
        rules.add(new GatewayFlowRule("comments")
            .setCount(1)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName("pa")
            )
        );

        rules.add(new GatewayFlowRule("some_customized_api")
            .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
            .setCount(5)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                .setFieldName("pn")
            )
        );
        GatewayRuleManager.loadRules(rules);
    }
}