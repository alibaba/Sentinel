package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.service.impl.DefaultSentinelApolloServiceImpl;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelApolloAutoConfiguration {

    private final SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties;

    private final ApolloOpenApiClient apolloOpenApiClient;

    public SentinelApolloAutoConfiguration(
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            ApolloOpenApiClient apolloOpenApiClient
    ) {
        this.sentinelApolloOpenApiProperties = sentinelApolloOpenApiProperties;
        this.apolloOpenApiClient = apolloOpenApiClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelApolloService sentinelApolloService() {
        return new DefaultSentinelApolloServiceImpl(this.apolloOpenApiClient);
    }

}
