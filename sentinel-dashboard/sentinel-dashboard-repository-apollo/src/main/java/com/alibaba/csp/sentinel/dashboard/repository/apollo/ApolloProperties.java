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
package com.alibaba.csp.sentinel.dashboard.repository.apollo;

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
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "apollo")
@PropertySource(value = {"classpath:repository/apollo.properties", "file:apollo.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "apollo")
public class ApolloProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloProperties.class);

    private static final String DEFAULT_PORTAL_URL = "http://localhost:10034";

    private String portalUrl;

    private String token;

    public void logInfo() {
        LOGGER.info(StringUtils.center("Use Apollo Repository", 50, "-"));
        LOGGER.info("Apollo Info: ");
        LOGGER.info("portalUrl={}", portalUrl != null ? portalUrl : DEFAULT_PORTAL_URL);
        LOGGER.info("token={}", token);
    }

    public String getPortalUrl() {
        return portalUrl != null ? portalUrl : DEFAULT_PORTAL_URL;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
