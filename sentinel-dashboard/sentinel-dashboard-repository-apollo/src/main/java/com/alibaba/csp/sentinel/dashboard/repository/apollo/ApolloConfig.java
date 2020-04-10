package com.alibaba.csp.sentinel.dashboard.repository.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloProperties.class)
public class ApolloConfig {

    @Autowired
    private ApolloProperties apolloProperties;

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        String portalUrl = apolloProperties.getPortalUrl();

        String token = apolloProperties.getToken();

        ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portalUrl)
                .withToken(token)
                .build();
        return client;
    }
}
