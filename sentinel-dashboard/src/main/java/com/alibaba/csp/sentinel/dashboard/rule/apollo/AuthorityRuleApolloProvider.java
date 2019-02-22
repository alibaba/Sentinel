package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;

import java.util.List;

/***
 * 从Apollo配置中心获取 授权规则 
 * @author Fx_demon 
 */
public class AuthorityRuleApolloProvider implements BaseDynamicRuleApolloProvider<AuthorityRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<String, List<AuthorityRuleEntity>> decoder;

    public AuthorityRuleApolloProvider(ApolloOpenApiClient configService, Converter<String, List<AuthorityRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<AuthorityRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_AUTHORITY_RULES;
    }
}
