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
package com.alibaba.csp.sentinel.dashboard.web;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

/**
 * Sentinel dashboard application.
 *
 * @author Carpenter Lee
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.csp.sentinel.dashboard")
public class DashboardApplication {

    public static final String redisHost = "localhost";

    public static final Integer redisPort = 6379;

    public static final String dashboardServer = "localhost:8080";

    public static void main(String[] args) {
        triggerSentinelInit();
        initRedisDataSource("dashboard", 8719);
        SpringApplication.run(DashboardApplication.class, args);
    }

    private static void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }

    public static void initRedisDataSource(String appName, Integer port) {
        // Set app name
        System.setProperty(SentinelConfig.APP_NAME_PROP_KEY, appName);
        // Set dashboard server address
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, dashboardServer);
        // Get client ip
        String ip = TransportConfig.getHeartbeatClientIp();
        // Set transport port
        TransportConfig.setRuntimePort(port);

        // Init redis connection config
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.builder().withHost(redisHost).withPort(redisPort).build();

        // Init RedisDataSource for flow rules
        String flowRuleKey = buildRuleKey(appName, ip, port, "flow");
        RedisDataSource<List<FlowRule>> flowRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, flowRuleKey, flowRuleKey, new Converter<String, List<FlowRule>>() {
            @Override
            public List<FlowRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                });
            }
        });
        FlowRuleManager.register2Property(flowRuleRedisDataSource.getProperty());

        // Init RedisDataSource for degrade rules
        String degradeRuleKey = buildRuleKey(appName, ip, port, "degrade");
        RedisDataSource<List<DegradeRule>> degradeRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, degradeRuleKey, degradeRuleKey, new Converter<String, List<DegradeRule>>() {
            @Override
            public List<DegradeRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                });
            }
        });
        DegradeRuleManager.register2Property(degradeRuleRedisDataSource.getProperty());

        // Init RedisDataSource for system rules
        String systemRuleKey = buildRuleKey(appName, ip, port, "system");
        RedisDataSource<List<SystemRule>> systemRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, systemRuleKey, systemRuleKey, new Converter<String, List<SystemRule>>() {
            @Override
            public List<SystemRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<SystemRule>>() {
                });
            }
        });
        SystemRuleManager.register2Property(systemRuleRedisDataSource.getProperty());

        // Init RedisDataSource for authority rules
        String authorityRuleKey = buildRuleKey(appName, ip, port, "authority");
        RedisDataSource<List<AuthorityRule>> authorityRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, authorityRuleKey, authorityRuleKey, new Converter<String, List<AuthorityRule>>() {
            @Override
            public List<AuthorityRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {
                });
            }
        });
        AuthorityRuleManager.register2Property(authorityRuleRedisDataSource.getProperty());

        // Init RedisDataSource for paramFlow rules
        String paramFlowRuleKey = buildRuleKey(appName, ip, port, "paramFlow");
        RedisDataSource<List<ParamFlowRule>> paramFlowRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, paramFlowRuleKey, paramFlowRuleKey, new Converter<String, List<ParamFlowRule>>() {
            @Override
            public List<ParamFlowRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {
                });
            }
        });
        ParamFlowRuleManager.register2Property(paramFlowRuleRedisDataSource.getProperty());
    }

    public static String buildRuleKey(String appName, String ip, Integer port, String ruleType) {
        return join("-", appName, ip, String.valueOf(port), ruleType, "rules");
    }

    public static String join(String separator, String... values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String value : values) {
            if (!first) {
                sb.append(separator);
            } else {
                first = false;
            }
            sb.append(value);
        }
        return sb.toString();
    }
}
