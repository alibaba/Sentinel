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
package com.alibaba.csp.sentinel.dashboard.domain.cluster.config;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterClientConfig {

    private String serverHost;
    private Integer serverPort;

    private Integer requestTimeout;
    private Integer connectTimeout;

    public String getServerHost() {
        return serverHost;
    }

    public ClusterClientConfig setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public ClusterClientConfig setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public ClusterClientConfig setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public ClusterClientConfig setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterClientConfig{" +
            "serverHost='" + serverHost + '\'' +
            ", serverPort=" + serverPort +
            ", requestTimeout=" + requestTimeout +
            ", connectTimeout=" + connectTimeout +
            '}';
    }
}
