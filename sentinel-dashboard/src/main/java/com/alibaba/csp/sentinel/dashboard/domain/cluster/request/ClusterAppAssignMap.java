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

import java.util.Set;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterAppAssignMap {

    private String machineId;
    private String ip;
    private Integer port;

    private Boolean belongToApp;

    private Set<String> clientSet;

    private Set<String> namespaceSet;
    private Double maxAllowedQps;

    public String getMachineId() {
        return machineId;
    }

    public ClusterAppAssignMap setMachineId(String machineId) {
        this.machineId = machineId;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public ClusterAppAssignMap setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ClusterAppAssignMap setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Set<String> getClientSet() {
        return clientSet;
    }

    public ClusterAppAssignMap setClientSet(Set<String> clientSet) {
        this.clientSet = clientSet;
        return this;
    }

    public Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public ClusterAppAssignMap setNamespaceSet(Set<String> namespaceSet) {
        this.namespaceSet = namespaceSet;
        return this;
    }

    public Boolean getBelongToApp() {
        return belongToApp;
    }

    public ClusterAppAssignMap setBelongToApp(Boolean belongToApp) {
        this.belongToApp = belongToApp;
        return this;
    }

    public Double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public ClusterAppAssignMap setMaxAllowedQps(Double maxAllowedQps) {
        this.maxAllowedQps = maxAllowedQps;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterAppAssignMap{" +
            "machineId='" + machineId + '\'' +
            ", ip='" + ip + '\'' +
            ", port=" + port +
            ", belongToApp=" + belongToApp +
            ", clientSet=" + clientSet +
            ", namespaceSet=" + namespaceSet +
            ", maxAllowedQps=" + maxAllowedQps +
            '}';
    }
}
