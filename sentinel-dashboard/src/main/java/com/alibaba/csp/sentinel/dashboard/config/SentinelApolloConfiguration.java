package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.service.impl.DefaultSentinelApolloServiceImpl;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentinelApolloProperties.class)
public class SentinelApolloConfiguration {

    private final SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final SentinelApolloProperties sentinelApolloProperties;

    public SentinelApolloConfiguration(
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            ApolloOpenApiClient apolloOpenApiClient,
            SentinelApolloProperties sentinelApolloProperties
    ) {
        this.sentinelApolloOpenApiProperties = sentinelApolloOpenApiProperties;
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.sentinelApolloProperties = sentinelApolloProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelApolloService sentinelApolloService() {
        return new DefaultSentinelApolloServiceImpl(this.sentinelApolloOpenApiProperties, this.apolloOpenApiClient, this.sentinelApolloProperties);
    }

}
