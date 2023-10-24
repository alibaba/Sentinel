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
package com.alibaba.csp.sentinel.dashboard.repository.nacos;

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
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "nacos")
@PropertySource(value = {"classpath:repository/nacos.properties", "file:nacos.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "nacos")
public class NacosProperties {

    private static Logger LOGGER = LoggerFactory.getLogger(NacosProperties.class);

    private final static String DEFAULT_SERVER_ADDR = "localhost";

    private final static String DEFAULT_NAMESPACE = "public";

    private final static String DEFAULT_SENTINEL_GROUP = "SENTINEL_GROUP";

    private final static Long DEFAULT_READ_TIMEOUT_MS = 3_000L;

    private String serverAddr;

    private String namespace;

    private String sentinelGroup;

    private Long readTimeoutMs;

    public void logInfo() {
        LOGGER.info(StringUtils.center("Use Nacos Repository", 50, "-"));
        LOGGER.info("Nacos Info: ");
        LOGGER.info("serverAddr={}", serverAddr != null ? serverAddr : DEFAULT_SERVER_ADDR);
        LOGGER.info("namespace={}", namespace != null ? namespace : DEFAULT_NAMESPACE);
        LOGGER.info("sentinelGroup={}", sentinelGroup != null ? sentinelGroup : DEFAULT_SENTINEL_GROUP);
        LOGGER.info("readTimeoutMs={}", readTimeoutMs != null ? readTimeoutMs : DEFAULT_READ_TIMEOUT_MS);
    }

    public String getServerAddr() {
        return serverAddr != null ? serverAddr : DEFAULT_SERVER_ADDR;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSentinelGroup() {
        return sentinelGroup != null ? sentinelGroup : DEFAULT_SENTINEL_GROUP;
    }

    public void setSentinelGroup(String sentinelGroup) {
        this.sentinelGroup = sentinelGroup;
    }

    public Long getReadTimeoutMs() {
        return readTimeoutMs != null ? readTimeoutMs : DEFAULT_READ_TIMEOUT_MS;
    }

    public void setReadTimeoutMs(Long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
