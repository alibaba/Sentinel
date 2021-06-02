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
package com.alibaba.csp.sentinel.cluster.server.config;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ServerTransportConfig {

    public static final int DEFAULT_IDLE_SECONDS = 600;

    private int port;
    private int idleSeconds;

    public ServerTransportConfig() {
        this(ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT, DEFAULT_IDLE_SECONDS);
    }

    public ServerTransportConfig(int port, int idleSeconds) {
        this.port = port;
        this.idleSeconds = idleSeconds;
    }

    public int getPort() {
        return port;
    }

    public ServerTransportConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getIdleSeconds() {
        return idleSeconds;
    }

    public ServerTransportConfig setIdleSeconds(int idleSeconds) {
        this.idleSeconds = idleSeconds;
        return this;
    }

    @Override
    public String toString() {
        return "ServerTransportConfig{" +
            "port=" + port +
            ", idleSeconds=" + idleSeconds +
            '}';
    }
}
