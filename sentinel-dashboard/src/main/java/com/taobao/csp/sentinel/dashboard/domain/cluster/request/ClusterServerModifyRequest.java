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
package com.taobao.csp.sentinel.dashboard.domain.cluster.request;

import java.util.Set;

import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterServerModifyRequest implements ClusterModifyRequest {

    private String app;
    private String ip;
    private Integer port;

    private Integer mode;
    private ServerFlowConfig flowConfig;
    private ServerTransportConfig transportConfig;
    private Set<String> namespaceSet;

    @Override
    public String getApp() {
        return app;
    }

    public ClusterServerModifyRequest setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public ClusterServerModifyRequest setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public ClusterServerModifyRequest setPort(Integer port) {
        this.port = port;
        return this;
    }

    @Override
    public Integer getMode() {
        return mode;
    }

    public ClusterServerModifyRequest setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public ServerFlowConfig getFlowConfig() {
        return flowConfig;
    }

    public ClusterServerModifyRequest setFlowConfig(
        ServerFlowConfig flowConfig) {
        this.flowConfig = flowConfig;
        return this;
    }

    public ServerTransportConfig getTransportConfig() {
        return transportConfig;
    }

    public ClusterServerModifyRequest setTransportConfig(
        ServerTransportConfig transportConfig) {
        this.transportConfig = transportConfig;
        return this;
    }

    public Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public ClusterServerModifyRequest setNamespaceSet(Set<String> namespaceSet) {
        this.namespaceSet = namespaceSet;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterServerModifyRequest{" +
            "app='" + app + '\'' +
            ", ip='" + ip + '\'' +
            ", port=" + port +
            ", mode=" + mode +
            ", flowConfig=" + flowConfig +
            ", transportConfig=" + transportConfig +
            ", namespaceSet=" + namespaceSet +
            '}';
    }
}
