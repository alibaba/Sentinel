package com.alibaba.csp.sentinel.dashboard.rule.apollo;
 
import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;

import java.util.List;

/***
 * 
 * @author Fx_demon
 *
 */
public class SystemRuleApolloPublisher implements BaseDynamicRuleApolloPublisher<SystemRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<List<SystemRuleEntity>, String> encoder;

    public SystemRuleApolloPublisher(ApolloOpenApiClient configService, Converter<List<SystemRuleEntity>, String> encoder) {
        this.configService = configService;
        this.encoder = encoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<List<SystemRuleEntity>, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_SYSTEM_FLOW_RULES;
    }
}