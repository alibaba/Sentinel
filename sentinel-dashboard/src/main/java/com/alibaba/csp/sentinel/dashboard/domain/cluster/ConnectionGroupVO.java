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

import java.util.List;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ConnectionGroupVO {

    private String namespace;
    private List<ConnectionDescriptorVO> connectionSet;
    private Integer connectedCount;

    public String getNamespace() {
        return namespace;
    }

    public ConnectionGroupVO setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public List<ConnectionDescriptorVO> getConnectionSet() {
        return connectionSet;
    }

    public ConnectionGroupVO setConnectionSet(
        List<ConnectionDescriptorVO> connectionSet) {
        this.connectionSet = connectionSet;
        return this;
    }

    public Integer getConnectedCount() {
        return connectedCount;
    }

    public ConnectionGroupVO setConnectedCount(Integer connectedCount) {
        this.connectedCount = connectedCount;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectionGroupVO{" +
            "namespace='" + namespace + '\'' +
            ", connectionSet=" + connectionSet +
            ", connectedCount=" + connectedCount +
            '}';
    }
}
