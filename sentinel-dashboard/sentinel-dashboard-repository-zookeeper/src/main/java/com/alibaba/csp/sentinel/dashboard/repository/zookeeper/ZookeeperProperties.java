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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "zookeeper")
@PropertySource(value = {"classpath:repository/zookeeper.properties", "file:zookeeper.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperProperties.class);

    private static final String DEFAULT_CONNECT_STRING = "localhost:2181";

    private static final Integer DEFAULT_BASE_SLEEP_TIME_MS = 1000;

    private static final Integer DEFAULT_MAX_RETRIES = 3;

    private String connectString;

    private Integer baseSleepTimeMs;

    private Integer maxRetries;

    public void logInfo() {
        LOGGER.info("Zookeeper info: ");
        LOGGER.info("connectString={}", connectString != null ? connectString : DEFAULT_CONNECT_STRING + "(default)");
        LOGGER.info("baseSleepTimeMs={}", baseSleepTimeMs != null ? baseSleepTimeMs : DEFAULT_BASE_SLEEP_TIME_MS + "(default)");
        LOGGER.info("maxRetries={}", maxRetries != null ? maxRetries : DEFAULT_MAX_RETRIES + "(default)");
    }

    public String getConnectString() {
        return connectString != null ? connectString : DEFAULT_CONNECT_STRING;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public Integer getBaseSleepTimeMs() {
        return baseSleepTimeMs != null ? baseSleepTimeMs : DEFAULT_BASE_SLEEP_TIME_MS;
    }

    public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public Integer getMaxRetries() {
        return maxRetries != null ? maxRetries : DEFAULT_MAX_RETRIES;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
