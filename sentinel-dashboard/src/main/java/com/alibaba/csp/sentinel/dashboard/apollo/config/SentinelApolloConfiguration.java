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
package com.alibaba.csp.sentinel.dashboard.apollo.config;

import com.alibaba.csp.sentinel.dashboard.apollo.repository.project.ApolloProjectStore;
import com.alibaba.csp.sentinel.dashboard.apollo.repository.project.ProjectRepository;
import com.alibaba.csp.sentinel.dashboard.apollo.service.ApolloPortalService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.impl.DefaultSentinelApolloServiceImpl;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
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

    private final ApolloPortalService apolloPortalService;

    public SentinelApolloConfiguration(
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            ApolloOpenApiClient apolloOpenApiClient,
            SentinelApolloProperties sentinelApolloProperties,
            ApolloPortalService apolloPortalService) {
        this.sentinelApolloOpenApiProperties = sentinelApolloOpenApiProperties;
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.sentinelApolloProperties = sentinelApolloProperties;
        this.apolloPortalService = apolloPortalService;
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelApolloService sentinelApolloService() {
        return new DefaultSentinelApolloServiceImpl(
                this.sentinelApolloOpenApiProperties,
                this.apolloOpenApiClient,
                this.sentinelApolloProperties,
                this.apolloPortalService
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectRepository projectRepository(@Value("${app.id}") String appId) {
        return new ApolloProjectStore(appId, this.apolloOpenApiClient, this.sentinelApolloOpenApiProperties);
    }

}
