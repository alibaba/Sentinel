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
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "rule",name = "provider",havingValue = "zookeeper",matchIfMissing = false)
public class ZookeeperConfig {
    public String serverAddr;
    
    @Bean
    public ZookeeperConfigService configService(){
        CuratorFramework zkClient =
                CuratorFrameworkFactory.newClient(serverAddr,
                        new ExponentialBackoffRetry(ZookeeperConfigUtil.SLEEP_TIME, ZookeeperConfigUtil.RETRY_TIMES));
        zkClient.start();
        return new ZookeeperConfigService(zkClient);
    }
    
    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }
    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public DynamicRuleProvider flowRuleProvider(RuleConfigService configService,
                                                Converter<String,List<FlowRuleEntity>> converter){
        return new AbstractDynamicRuleProvider((appName)-> configService.getConfig((String) appName,
                ZookeeperConfigUtil.FLOW_RULE),converter);
    }

    @Bean
    public DynamicRulePublisher flowRulePublisher(RuleConfigService configService,
                                                  Converter<List<FlowRuleEntity>,String> converter){
        return new AbstractDynamicRulePublisher((appId, rules)->
                configService.publishConfig((String)appId,ZookeeperConfigUtil.FLOW_RULE,(String)rules)
                ,converter);
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
    public DynamicRuleProvider degradeRuleProvider(RuleConfigService configService,
                                                   Converter<String, List<DegradeRuleEntity>> converter){
        return new AbstractDynamicRuleProvider((appName)-> configService.getConfig((String) appName,
                ZookeeperConfigUtil.DEGRADE_RULE),converter);
    }

    @Bean
    public DynamicRulePublisher degradeRulePublisher(RuleConfigService configService,Converter<List<DegradeRuleEntity>,String> converter){
        return new AbstractDynamicRulePublisher((appId,rules)->{
            configService.publishConfig((String)appId,ZookeeperConfigUtil.DEGRADE_RULE,(String)rules);
        },converter);
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
    public DynamicRuleProvider paramFlowRuleProvider(RuleConfigService configService,
                                                     Converter<String, List<ParamFlowRuleEntity>> converter){
        return new AbstractDynamicRuleProvider((appName)-> configService.getConfig((String) appName,
                ZookeeperConfigUtil.PARAM_RULE),converter);
    }
    @Bean
    public DynamicRulePublisher paramFlowRulePublisher(RuleConfigService configService,Converter<List<ParamFlowRuleEntity>,String> converter){
        return new AbstractDynamicRulePublisher((appId,rules)->{
            configService.publishConfig((String)appId,ZookeeperConfigUtil.PARAM_RULE,(String)rules);
        },converter);
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
    public DynamicRuleProvider systemRuleProvider(RuleConfigService configService,
                                                  Converter<String, List<SystemRuleEntity>> converter){
        return new AbstractDynamicRuleProvider((appName)-> configService.getConfig((String) appName,
                ZookeeperConfigUtil.SYSTEM_RULE),converter);
    }
    @Bean
    public DynamicRulePublisher systemRulePublisher(RuleConfigService configService,Converter<List<SystemRuleEntity>,String> converter){
        return new AbstractDynamicRulePublisher((appId,rules)->{
            configService.publishConfig((String)appId,ZookeeperConfigUtil.SYSTEM_RULE,(String)rules);
        },converter);
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
    public DynamicRuleProvider authorityRuleProvider(RuleConfigService configService,
                                                     Converter<String, List<AuthorityRuleEntity>> converter){
        return new AbstractDynamicRuleProvider((appName)-> configService.getConfig((String) appName,
                ZookeeperConfigUtil.AUTHORITY_RULE),converter);
    }

    @Bean
    public DynamicRulePublisher authorityRulePublisher(RuleConfigService configService,Converter<List<AuthorityRuleEntity>,String> converter){
        return new AbstractDynamicRulePublisher((appId,rules)->{
            configService.publishConfig((String)appId,ZookeeperConfigUtil.AUTHORITY_RULE,(String)rules);
        },converter);
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
