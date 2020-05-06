/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.zookeeper;

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "zookeeper")
@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    @Autowired
    private ZookeeperProperties zookeeperProperties;

    @PostConstruct
    public void init() {
        zookeeperProperties.logInfo();
    }

    @Bean
    public CuratorFramework zkClient() {
        String connectString = zookeeperProperties.getConnectString();
        AssertUtil.assertNotBlank(connectString, "Zookeeper Client init failed, connectString can't be blank");

        Integer baseSleepTimeMs = zookeeperProperties.getBaseSleepTimeMs();
        AssertUtil.notNull(connectString, "Zookeeper Client init failed, baseSleepTimeMs can't be null");

        Integer maxRetries = zookeeperProperties.getMaxRetries();
        AssertUtil.notNull(connectString, "Zookeeper Client init failed, maxRetries can't be null");

        try {
            CuratorFramework zkClient = CuratorFrameworkFactory.newClient(connectString, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
            zkClient.start();
            LOGGER.info("Zookeeper client init success");
            return zkClient;
        } catch (Throwable e) {
            LOGGER.info("Zookeeper client init error", e);
            throw e;
        }
    }
}
