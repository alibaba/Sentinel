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
package com.alibaba.csp.sentinel.dashboard.repository.redis;

import org.apache.commons.lang.StringUtils;
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
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "redis")
@PropertySource(value = {"classpath:repository/redis.properties", "file:redis.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProperties.class);

    private static final String DEFAULT_HOST = "localhost";

    private static final Integer DEFAULT_PORT = 6379;

    private static final Integer DEFAULT_DATABASE = 0;

    private static final Long DEFAULT_TIMEOUT_MS = 60000L;

    private static final String DEFAULT_CHANNEL_SUFFIX = "";

    private String host;

    private Integer port;

    private String password;

    private Integer database;

    private Long timeoutMs;

    private String channelSuffix;

    public void logInfo() {
        LOGGER.info(StringUtils.center("Use Redis Repository", 50, "-"));
        LOGGER.info("Redis Info: ");
        LOGGER.info("host={}", host != null ? host : DEFAULT_HOST);
        LOGGER.info("port={}", port != null ? port : DEFAULT_PORT);
        LOGGER.info("password={}", password);
        LOGGER.info("database={}", database != null ? database : DEFAULT_DATABASE);
        LOGGER.info("timeoutMs={}", timeoutMs != null ? timeoutMs : DEFAULT_TIMEOUT_MS);
        LOGGER.info("channelSuffix={}", channelSuffix != null ? channelSuffix : DEFAULT_CHANNEL_SUFFIX);
    }

    public String getHost() {
        return host != null ? host : DEFAULT_HOST;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getDatabase() {
        return database != null ? database : DEFAULT_DATABASE;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }

    public Long getTimeoutMs() {
        return timeoutMs != null ? timeoutMs : DEFAULT_TIMEOUT_MS;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getChannelSuffix() {
        return channelSuffix != null ? channelSuffix : DEFAULT_CHANNEL_SUFFIX;
    }

    public void setChannelSuffix(String channelSuffix) {
        this.channelSuffix = channelSuffix;
    }
}
