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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
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
    private String token;
    private String username;
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
    public FlowRuleApolloProvider flowRuleProvider(){
        return new FlowRuleApolloProvider();
    }
    @Bean
    public FlowRuleApolloPublisher flowRulePublisher(Converter<List<FlowRuleEntity>,String> converter){
        return new FlowRuleApolloPublisher(converter);
    }

    @Bean
    public Converter<List<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }
    @Bean
    public DegradeRuleApolloProvider degradeRuleProvider(){
        return new DegradeRuleApolloProvider();
    }
    @Bean
    public DegradeRuleApolloPublisher degradeRulePublisher(Converter<List<DegradeRuleEntity>,String> converter){
        return new DegradeRuleApolloPublisher(converter);
    }

    @Bean
    public Converter<List<AuthorityRuleEntity>, String> authorityRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<AuthorityRuleEntity>> authorityRuleEntityDecoder() {
        return s -> JSON.parseArray(s, AuthorityRuleEntity.class);
    }
    @Bean
    public AuthorityRuleApolloProvider authorityRuleProvider(){
        return new AuthorityRuleApolloProvider();
    }
    @Bean
    public AuthorityRuleApolloPublisher authorityRulePublisher(Converter<List<AuthorityRuleEntity>,String> converter){
        return new AuthorityRuleApolloPublisher(converter);
    }

    @Bean
    public Converter<List<ParamFlowRuleEntity>, String> paramFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<ParamFlowRuleEntity>> paramFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, ParamFlowRuleEntity.class);
    }
    @Bean
    public ParamFlowRuleApolloProvider paramFlowRuleProvider(){
        return new ParamFlowRuleApolloProvider();
    }
    @Bean
    public ParamFlowRuleApolloPublisher paramFlowRulePublisher(Converter<List<ParamFlowRuleEntity>,String> converter){
        return new ParamFlowRuleApolloPublisher(converter);
    }

    @Bean
    public Converter<List<SystemRuleEntity>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<SystemRuleEntity>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
    }
    @Bean
    public SystemRuleApolloProvider systemRuleProvider(){
        return new SystemRuleApolloProvider();
    }
    @Bean
    public SystemRuleApolloPublisher systemRulePublisher(Converter<List<SystemRuleEntity>,String> converter){
        return new SystemRuleApolloPublisher(converter);
    }

    @Bean
    public ApolloConfigService configService() {
        return new ApolloConfigService(token,username,env,cluster,namespace,serverAddr);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
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
