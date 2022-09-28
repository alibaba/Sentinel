/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.RuleType;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Configuration
@EnableConfigurationProperties(ApolloProperties.class)
public class ApolloConfig {

    @Resource
    private ApolloProperties apolloProperties;

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        return ApolloOpenApiClient.newBuilder()
                .withPortalUrl(apolloProperties.getPortalUrl())
                .withToken(apolloProperties.getToken())
                .build();

    }


    @Bean
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                     ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.FLOW,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<DegradeRuleEntity> degradeRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                           ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.DEGRADE,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<ParamFlowRuleEntity> paramFlowRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                               ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.PARAM_FLOW,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<SystemRuleEntity> systemRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                         ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.SYSTEM,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<AuthorityRuleEntity> authorityRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                               ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.AUTHORITY,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<GatewayFlowRuleEntity> gatewayFlowRuleDynamicRuleStore(ApolloProperties apolloProperties,
                                                                                   ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.GW_FLOW,
                apolloProperties,
                apolloOpenApiClient
        );
    }

    @Bean
    public DynamicRuleStore<ApiDefinitionEntity> apiDefinitionDynamicRuleStore(ApolloProperties apolloProperties,
                                                                               ApolloOpenApiClient apolloOpenApiClient) {
        return new DynamicRuleApolloStore<>(
                RuleType.GW_API_GROUP,
                apolloProperties,
                apolloOpenApiClient
        );
    }


}
