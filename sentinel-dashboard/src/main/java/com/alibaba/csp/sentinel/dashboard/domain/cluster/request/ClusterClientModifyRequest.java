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
package com.alibaba.csp.sentinel.dashboard.domain.cluster.request;

import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterClientModifyRequest implements ClusterModifyRequest {

    private String app;
    private String ip;
    private Integer port;

    private Integer mode;
    private ClusterClientConfig clientConfig;

    @Override
    public String getApp() {
        return app;
    }

    public ClusterClientModifyRequest setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public ClusterClientModifyRequest setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public ClusterClientModifyRequest setPort(Integer port) {
        this.port = port;
        return this;
    }

    @Override
    public Integer getMode() {
        return mode;
    }

    public ClusterClientModifyRequest setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public ClusterClientConfig getClientConfig() {
        return clientConfig;
    }

    public ClusterClientModifyRequest setClientConfig(
        ClusterClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }
}
