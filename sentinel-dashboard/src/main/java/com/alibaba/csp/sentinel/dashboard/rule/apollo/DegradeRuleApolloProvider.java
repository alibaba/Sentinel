package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;

import java.util.List;

 
public class DegradeRuleApolloProvider implements BaseDynamicRuleApolloProvider<DegradeRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<String, List<DegradeRuleEntity>> decoder;

    public DegradeRuleApolloProvider(ApolloOpenApiClient configService, Converter<String, List<DegradeRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<DegradeRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_DEGRADE_RULES;
    }
}