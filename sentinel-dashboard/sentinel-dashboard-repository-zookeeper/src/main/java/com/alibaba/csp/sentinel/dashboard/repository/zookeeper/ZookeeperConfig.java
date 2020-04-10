package com.alibaba.csp.sentinel.dashboard.repository.zookeeper;

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

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "zookeeper")
@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    public ZookeeperConfig() {
        System.out.println("ZookeeperConfig");
    }

    @Autowired
    private ZookeeperProperties zookeeperProperties;

    @Bean
    public CuratorFramework zkClient() {
        String connectString = zookeeperProperties.getConnectString();

        Integer baseSleepTimeMs = zookeeperProperties.getBaseSleepTimeMs();

        Integer maxRetries = zookeeperProperties.getMaxRetries();

        CuratorFramework zkClient =
                CuratorFrameworkFactory.newClient(connectString,
                        new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
        zkClient.start();

        return zkClient;
    }
}
