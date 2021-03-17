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
