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
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Configuration
@ConditionalOnProperty(prefix = "rule",name = "provider",havingValue = "nacos",matchIfMissing = false)
@ConfigurationProperties(prefix = "rule.configserver.nacos")
public class NacosConfig {
    private String serverAddr;
    private String namespace;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }
    @Bean
    public FlowRuleNacosProvider flowRuleProvider(){
        return new FlowRuleNacosProvider();
    }
    @Bean
    public FlowRuleNacosPublisher flowRulePublisher(Converter<List<FlowRuleEntity>,String> converter){
        return new FlowRuleNacosPublisher(converter);
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
    public DegradeRuleNacosProvider degradeRuleProvider(){
        return new DegradeRuleNacosProvider();
    }
    @Bean
    public DegradeRuleNacosPublisher degradeRulePublisher(Converter<List<DegradeRuleEntity>,String> converter){
        return new DegradeRuleNacosPublisher(converter);
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
    public ParamFlowRuleNacosProvider paramFlowRuleProvider(){
        return new ParamFlowRuleNacosProvider();
    }
    @Bean
    public ParamFlowRuleNacosPublisher paramFlowRulePublisher(Converter<List<ParamFlowRuleEntity>,String> converter){
        return new ParamFlowRuleNacosPublisher(converter);
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
    public SystemRuleNacosProvider systemRuleProvider(){
        return new SystemRuleNacosProvider();
    }
    @Bean
    public SystemRuleNacosPublisher systemRulePublisher(Converter<List<SystemRuleEntity>,String> converter){
        return new SystemRuleNacosPublisher(converter);
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
    public AuthorityRuleNacosProvider authorityRuleProvider(){
        return new AuthorityRuleNacosProvider();
    }

    @Bean
    public AuthorityRuleNacosPublisher authorityRulePublisher(Converter<List<AuthorityRuleEntity>,String> converter){
        return new AuthorityRuleNacosPublisher(converter);
    }

    @Bean
    public ConfigService nacosConfigService() throws Exception {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        if(StringUtils.isNotBlank(namespace)){
            properties.put(PropertyKeyConst.NAMESPACE,namespace);
        }
        return ConfigFactory.createConfigService(properties);
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
