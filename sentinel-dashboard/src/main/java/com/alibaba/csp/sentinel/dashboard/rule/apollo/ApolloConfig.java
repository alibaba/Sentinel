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
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigRegistrar;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Configuration
@ConditionalOnProperty(prefix = "rule",name = "provider",havingValue = "apollo",matchIfMissing = false)
@ConfigurationProperties(prefix = "rule.configserver.appollo")
@EnableApolloConfig
public class ApolloConfig {

    private String appId;
    private String token;
    private String env;
    private String cluster;
    private String namespace;
    private String serverAddr;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public FlowRuleApolloProvider ruleProvider(){
        return new FlowRuleApolloProvider(appId,env,cluster,namespace);
    }

    @Bean
    public FlowRuleApolloPublisher rulePublisher(){
        return new FlowRuleApolloPublisher(appId,env,cluster,namespace);
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
            .withPortalUrl(serverAddr)
            .withToken(token)
            .build();
        return client;

    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
