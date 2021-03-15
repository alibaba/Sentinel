package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloLogicService;
import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.service.impl.SentinelApolloPublicNamespaceServiceImpl;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelApolloAutoConfiguration {

    private final SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties;

    private final SentinelApolloLogicService sentinelApolloLogicService;

    private final ApolloOpenApiClient apolloOpenApiClient;

    public SentinelApolloAutoConfiguration(
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            SentinelApolloLogicService sentinelApolloLogicService,
            ApolloOpenApiClient apolloOpenApiClient
    ) {
        this.sentinelApolloOpenApiProperties = sentinelApolloOpenApiProperties;
        this.sentinelApolloLogicService = sentinelApolloLogicService;
        this.apolloOpenApiClient = apolloOpenApiClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelApolloService sentinelApolloService() {
        return new SentinelApolloPublicNamespaceServiceImpl(this.sentinelApolloOpenApiProperties, this.sentinelApolloLogicService, this.apolloOpenApiClient);
    }

}
