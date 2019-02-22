package com.alibaba.csp.sentinel.dashboard.rule.apollo;
 
import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;

import java.util.List;

import org.springframework.stereotype.Component;

/***
 * 
 * @author Fx_demon
 *
 */
@Component
public class FlowRuleApolloPublisher implements BaseDynamicRuleApolloPublisher<FlowRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<List<FlowRuleEntity>, String> encoder;

    public FlowRuleApolloPublisher(ApolloOpenApiClient configService, Converter<List<FlowRuleEntity>, String> encoder) {
        this.configService = configService;
        this.encoder = encoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<List<FlowRuleEntity>, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_FLOW_RULES;
    }
}