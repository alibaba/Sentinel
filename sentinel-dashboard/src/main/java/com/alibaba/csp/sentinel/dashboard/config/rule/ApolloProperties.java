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

package com.alibaba.csp.sentinel.dashboard.config.rule;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@ConfigurationProperties(prefix = "apollo.sentinel")
public class ApolloProperties {

    private String portalUrl;

    private String token;

    private String operator;

    private String appId;

    private String env;

    private String clusterName = "default";

    private String namespace = "application";

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(final String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(final String appId) {
        this.appId = appId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(final String env) {
        this.env = env;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }
}
