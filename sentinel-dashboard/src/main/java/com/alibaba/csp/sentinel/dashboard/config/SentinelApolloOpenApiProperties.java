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
package com.alibaba.csp.sentinel.dashboard.config;

import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@ConfigurationProperties(prefix = "sentinel.apollo.open.api")
@Validated
public class SentinelApolloOpenApiProperties {

    /**
     * default namespace's length limit in apollo.
     * because {@link SentinelApolloPublicProperties#getNamespacePrefix()} may be size 5 ~ 15,
     * so the default value should small enough.
     */
    private static final Integer DEFAULT_NAMESPACE_LENGTH_LIMIT = 32 - 5;

    @NotEmpty
    private String portalUrl;

    @NotEmpty
    private String token;

    /**
     * recommend use current apollo project's user.
     */
    @NotEmpty
    private String operateUser;

    /**
     * which app id in apollo dashboard operates.
     * recommend that distinguish with dashboard's app id.
     */
    @NotEmpty
    private String operatedAppId;

    /**
     * which environment in apollo dashboard operates.
     * recommend that as same as dashboard's environment.
     */
    @NotEmpty
    private String operatedEnv;

    /**
     * which cluster in apollo dashboard operates.
     * recommend that as same as dashboard's cluster.
     */
    @NotEmpty
    private String operatedCluster;

    @PositiveOrZero
    private Integer connectTimeout = ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT;

    @PositiveOrZero
    private Integer readTimeout = ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT;

    /**
     * namespace's length is limited in apollo.
     */
    @Positive
    private Integer namespaceLengthLimit = DEFAULT_NAMESPACE_LENGTH_LIMIT;

    public String getPortalUrl() {
        return portalUrl;
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

    public String getOperateUser() {
        return operateUser;
    }

    public void setOperateUser(String operateUser) {
        this.operateUser = operateUser;
    }

    public String getOperatedAppId() {
        return operatedAppId;
    }

    public void setOperatedAppId(String operatedAppId) {
        this.operatedAppId = operatedAppId;
    }

    public String getOperatedEnv() {
        return operatedEnv;
    }

    public void setOperatedEnv(String operatedEnv) {
        this.operatedEnv = operatedEnv;
    }

    public String getOperatedCluster() {
        return operatedCluster;
    }

    public void setOperatedCluster(String operatedCluster) {
        this.operatedCluster = operatedCluster;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getNamespaceLengthLimit() {
        return namespaceLengthLimit;
    }

    public void setNamespaceLengthLimit(Integer namespaceLengthLimit) {
        this.namespaceLengthLimit = namespaceLengthLimit;
    }
}
