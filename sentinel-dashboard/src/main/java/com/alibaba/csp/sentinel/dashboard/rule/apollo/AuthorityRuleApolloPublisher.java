package com.alibaba.csp.sentinel.dashboard.rule.apollo;
 
import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;

import java.util.List;

/***
 * 
 * @author Fx_demon
 *
 */
public class AuthorityRuleApolloPublisher implements BaseDynamicRuleApolloPublisher<AuthorityRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<List<AuthorityRuleEntity>, String> encoder;

    public AuthorityRuleApolloPublisher(ApolloOpenApiClient configService, Converter<List<AuthorityRuleEntity>, String> encoder) {
        this.configService = configService;
        this.encoder = encoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<List<AuthorityRuleEntity>, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_AUTHORITY_RULES;
    }
}