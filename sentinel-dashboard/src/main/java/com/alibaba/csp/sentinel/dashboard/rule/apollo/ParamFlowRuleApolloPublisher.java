package com.alibaba.csp.sentinel.dashboard.rule.apollo;
 
import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;

import java.util.List;

/***
 * 
 * @author Fx_demon
 *
 */
public class ParamFlowRuleApolloPublisher implements BaseDynamicRuleApolloPublisher<ParamFlowRuleEntity> {
    private ApolloOpenApiClient configService;
    private Converter<List<ParamFlowRuleEntity>, String> encoder;

    public ParamFlowRuleApolloPublisher(ApolloOpenApiClient configService, Converter<List<ParamFlowRuleEntity>, String> encoder) {
        this.configService = configService;
        this.encoder = encoder;
    }

    @Override
    public ApolloOpenApiClient getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<List<ParamFlowRuleEntity>, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public String getKey() {
        return ApolloConfigUtil.KEY_PARAM_FLOW_RULES;
    }
}