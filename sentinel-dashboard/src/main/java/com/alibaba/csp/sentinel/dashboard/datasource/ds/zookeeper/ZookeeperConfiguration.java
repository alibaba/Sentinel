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
package com.alibaba.csp.sentinel.dashboard.datasource.ds.zookeeper;

import com.alibaba.csp.sentinel.dashboard.datasource.ds.DataSourceProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
@Configuration
@ConditionalOnProperty(prefix = DataSourceProperties.PREFIX_DATASOURCE, name = DataSourceProperties.NAME_PROVIDER, havingValue = DataSourceProperties.VALUE_PROVIDER_ZOOKEEPER, matchIfMissing = false)
public class ZookeeperConfiguration {

    @Autowired
    private ZookeeperProperties zookeeperProperties;

    @Bean
    public CuratorFramework zkClient() {
        ExponentialBackoffRetry exponentialBackoffRetry =  new ExponentialBackoffRetry(zookeeperProperties.getBaseSleepTime(), zookeeperProperties.getMaxRetries(), zookeeperProperties.getMaxSleepTime());
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient("localhhost:2181", exponentialBackoffRetry);
        zkClient.start();
        return zkClient;
    }
}