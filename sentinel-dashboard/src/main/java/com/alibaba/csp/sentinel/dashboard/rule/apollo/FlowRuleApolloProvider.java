package com.alibaba.csp.sentinel.dashboard.rule.apollo;

 

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;

import java.util.List;

import org.springframework.stereotype.Component;

/***
 * 从Apollo配置中心获取限流规则 
 * @author Fx_demon
 */
@Component
public class FlowRuleApolloProvider implements BaseDynamicRuleApolloProvider<FlowRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<String, List<FlowRuleEntity>> decoder;

    public FlowRuleApolloProvider(ApolloOpenApiClient configService, Converter<String, List<FlowRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<FlowRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_FLOW_RULES;
    }
}
