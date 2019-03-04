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
package com.alibaba.csp.sentinel.dashboard.domain.cluster;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterClientInfoVO {

    private String serverHost;
    private Integer serverPort;

    private Integer clientState;

    private Integer requestTimeout;

    public String getServerHost() {
        return serverHost;
    }

    public ClusterClientInfoVO setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public ClusterClientInfoVO setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public Integer getClientState() {
        return clientState;
    }

    public ClusterClientInfoVO setClientState(Integer clientState) {
        this.clientState = clientState;
        return this;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public ClusterClientInfoVO setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterClientInfoVO{" +
            "serverHost='" + serverHost + '\'' +
            ", serverPort=" + serverPort +
            ", clientState=" + clientState +
            ", requestTimeout=" + requestTimeout +
            '}';
    }
}
