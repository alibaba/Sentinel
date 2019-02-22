package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;

import java.util.List;

/***
 * 从Apollo配置中心获取 规则 
 * @author Fx_demon
 */
public class ParamFlowRuleApolloProvider implements BaseDynamicRuleApolloProvider<ParamFlowRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<String, List<ParamFlowRuleEntity>> decoder;

    public ParamFlowRuleApolloProvider(ApolloOpenApiClient configService, Converter<String, List<ParamFlowRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<ParamFlowRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_PARAM_FLOW_RULES;
    }
}
