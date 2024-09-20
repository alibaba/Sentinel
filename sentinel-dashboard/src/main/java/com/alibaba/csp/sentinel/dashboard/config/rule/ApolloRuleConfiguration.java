package com.alibaba.csp.sentinel.dashboard.config.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.RuleType;
import com.alibaba.csp.sentinel.dashboard.rule.aop.SentinelApiClientAspect;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloOpenApiClientProvider;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.DynamicRuleApolloStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@Configuration
@ConditionalOnProperty(prefix = "rule.store", name = "type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloProperties.class)
public class ApolloRuleConfiguration {

    @Resource
    private ApolloProperties apolloProperties;

    @Bean
    public ApolloOpenApiClientProvider apolloOpenApiClientProvider() {
        return new ApolloOpenApiClientProvider(apolloProperties.getPortalUrl(), apolloProperties.getToken());
    }


    @Bean
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.FLOW,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<DegradeRuleEntity> degradeRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.DEGRADE,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<ParamFlowRuleEntity> paramFlowRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.PARAM_FLOW,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<SystemRuleEntity> systemRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.SYSTEM,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<AuthorityRuleEntity> authorityRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.AUTHORITY,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<GatewayFlowRuleEntity> gatewayFlowRuleDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.GW_FLOW,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public DynamicRuleStore<ApiDefinitionEntity> apiDefinitionDynamicRuleStore(ApolloOpenApiClientProvider apolloOpenApiClientProvider) {
        return new DynamicRuleApolloStore<>(
                RuleType.GW_API_GROUP,
                apolloProperties,
                apolloOpenApiClientProvider
        );
    }

    @Bean
    public SentinelApiClientAspect sentinelApiClientAspect() {
        return new SentinelApiClientAspect();
    }

}

