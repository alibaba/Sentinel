package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;

import java.util.List;

/***
 * 
 * @author Fx_demon
 */
public class DegradeRuleApolloPublisher implements BaseDynamicRuleApolloPublisher<DegradeRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<List<DegradeRuleEntity>, String> encoder;

    public DegradeRuleApolloPublisher(ApolloOpenApiClient configService,
                                     Converter<List<DegradeRuleEntity>, String> encoder) {
        this.configService = configService;
        this.encoder = encoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<List<DegradeRuleEntity>, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_DEGRADE_RULES;
    }
}