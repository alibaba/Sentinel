package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;

import java.util.List;

/***
 * 从Apollo配置中心获取限流规则 
 * @author Fx_demon
 */
public class SystemRuleApolloProvider implements BaseDynamicRuleApolloProvider<SystemRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<String, List<SystemRuleEntity>> decoder;

    public SystemRuleApolloProvider(ApolloOpenApiClient configService, Converter<String, List<SystemRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<SystemRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_SYSTEM_FLOW_RULES;
    }
}
