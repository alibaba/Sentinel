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

import java.util.List;
import java.util.Properties;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Configuration
public class NacosConfig {

    @Value("${spring.cloud.nacos.config.server-addr:localhost:8848}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.config.namespace:}")
    private String namespace;

    @Value("${spring.cloud.nacos.config.username:}")
    private String username;

    @Value("${spring.cloud.nacos.config.password:}")
    private String password;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public Converter<List<FlowRule>, String> flowRuleEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRule>> flowRuleDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<List<FlowRule>>() {});
    }

    @Bean
    public Converter<List<DegradeRule>, String> degradeRuleEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<DegradeRule>> degradeRuleDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<List<DegradeRule>>() {});
    }

    @Bean
    public ConfigService nacosConfigService() throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (!org.springframework.util.StringUtils.isEmpty(namespace)) {
            properties.put("namespace", namespace);
        }
        if (!org.springframework.util.StringUtils.isEmpty(username)) {
            properties.put("username", username);
        }
        if (!org.springframework.util.StringUtils.isEmpty(password)) {
            properties.put("password", password);
        }
        return ConfigFactory.createConfigService(properties);
    }
}