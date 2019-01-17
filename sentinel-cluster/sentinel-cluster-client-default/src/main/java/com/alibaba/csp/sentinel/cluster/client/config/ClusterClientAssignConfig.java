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
package com.alibaba.csp.sentinel.cluster.client.config;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterClientAssignConfig {

    private String serverHost;
    private Integer serverPort;

    public ClusterClientAssignConfig() {}

    public ClusterClientAssignConfig(String serverHost, Integer serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public ClusterClientAssignConfig setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public ClusterClientAssignConfig setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterClientAssignConfig{" +
            "serverHost='" + serverHost + '\'' +
            ", serverPort=" + serverPort +
            '}';
    }
}
