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
package com.taobao.csp.sentinel.dashboard.domain.cluster;

import java.util.List;
import java.util.Set;

import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterServerStateVO {

    private ServerTransportConfig transport;
    private ServerFlowConfig flow;
    private Set<String> namespaceSet;

    private Integer port;
    private List<ConnectionGroupVO> connection;

    public ServerTransportConfig getTransport() {
        return transport;
    }

    public ClusterServerStateVO setTransport(
        ServerTransportConfig transport) {
        this.transport = transport;
        return this;
    }

    public ServerFlowConfig getFlow() {
        return flow;
    }

    public ClusterServerStateVO setFlow(ServerFlowConfig flow) {
        this.flow = flow;
        return this;
    }

    public Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public ClusterServerStateVO setNamespaceSet(Set<String> namespaceSet) {
        this.namespaceSet = namespaceSet;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ClusterServerStateVO setPort(Integer port) {
        this.port = port;
        return this;
    }

    public List<ConnectionGroupVO> getConnection() {
        return connection;
    }

    public ClusterServerStateVO setConnection(
        List<ConnectionGroupVO> connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterServerStateVO{" +
            "transport=" + transport +
            ", flow=" + flow +
            ", namespaceSet=" + namespaceSet +
            ", port=" + port +
            ", connection=" + connection +
            '}';
    }
}
