/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnClass(ApolloOpenApiClient.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloProperties.class)
public class ApolloConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloConfig.class);

    @Autowired
    private ApolloProperties apolloProperties;

    @PostConstruct
    public void init() {
        apolloProperties.logInfo();
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        String portalUrl = apolloProperties.getPortalUrl();

        String token = apolloProperties.getToken();

        try {
            ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portalUrl)
                .withToken(token)
                .build();
            LOGGER.info("Apollo client init success");
            return client;
        } catch (Throwable e) {
            LOGGER.error("Apollo client init error", e);
            throw e;
        }
    }
}
