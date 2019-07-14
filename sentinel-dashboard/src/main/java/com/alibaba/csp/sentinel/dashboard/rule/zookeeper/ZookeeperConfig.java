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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.util.ConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * zookeeper 配置类
 * @author lixiangqian
 */
@Configuration
@ConfigurationProperties(prefix = "sentinel.dashboard.zk")
@ConditionalOnProperty(value = "sentinel.dashboard.zk.enabled", havingValue = "true")
public class ZookeeperConfig implements InitializingBean {

    private String host;
    private String ruleRootPath;

    public static final int RETRY_TIMES = 3;
    public static final int SLEEP_TIME = 1000;


    public final static String RULE_TYPE_FLOW = "flowRule";
    public final static String RULE_TYPE_DEGRADE = "degradeRule";
    public final static String RULE_TYPE_SYSTEM = "systemRule";

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
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
    public Converter<List<SystemRuleEntity>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<SystemRuleEntity>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
    }


    @Bean
    public CuratorFramework zkClient() {
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(host, new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
        zkClient.start();

        return zkClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        host = ConfigUtil.getConfig("sentinel.dashboard.zk.host", host);
        ruleRootPath = ConfigUtil.getConfig("sentinel.dashboard.zk.ruleRootPath", ruleRootPath);
    }

    public String getPath(String appName, String ruleType) {
        StringBuilder stringBuilder = new StringBuilder(ruleRootPath);

        if (StringUtils.isBlank(appName)) {
            return stringBuilder.toString();
        }
        if (appName.startsWith("/")) {
            stringBuilder.append(appName);
        } else {
            stringBuilder.append("/")
                    .append(appName);
        }
        stringBuilder.append("/")
                .append(ruleType);
        return stringBuilder.toString();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setRuleRootPath(String ruleRootPath) {
        this.ruleRootPath = ruleRootPath;
    }
}