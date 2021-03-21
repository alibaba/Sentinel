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

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentinelApolloOpenApiProperties.class)
public class SentinelApolloOpenApiConfiguration {

    private final SentinelApolloOpenApiProperties properties;

    public SentinelApolloOpenApiConfiguration(SentinelApolloOpenApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        ApolloOpenApiClient apolloOpenApiClient = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(this.properties.getPortalUrl())
                .withToken(this.properties.getToken())
                .withConnectTimeout(this.properties.getConnectTimeout())
                .withReadTimeout(this.properties.getReadTimeout())
                .build();
        return apolloOpenApiClient;
    }
}
