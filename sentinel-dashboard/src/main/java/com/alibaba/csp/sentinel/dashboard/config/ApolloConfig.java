/**
 * coder4j.cn
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author buhao
 * @version ApolloConfig.java, v 0.1 2019-09-06 10:56 buhao
 */
@Configuration
public class ApolloConfig {

    @Value("${apollo.portal.url}")
    private String apolloPortalUrl;
    @Value("${apollo.application.token}")
    private String apolloApplicationToken;

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        return ApolloOpenApiClient.newBuilder()
                .withPortalUrl(apolloPortalUrl)
                .withToken(apolloApplicationToken)
                .build();
    }

}