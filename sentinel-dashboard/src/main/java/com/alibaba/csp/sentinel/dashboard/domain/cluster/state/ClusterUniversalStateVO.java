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
package com.alibaba.csp.sentinel.dashboard.domain.cluster.state;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterUniversalStateVO {

    private ClusterStateSimpleEntity stateInfo;
    private ClusterClientStateVO client;
    private ClusterServerStateVO server;

    public ClusterClientStateVO getClient() {
        return client;
    }

    public ClusterUniversalStateVO setClient(ClusterClientStateVO client) {
        this.client = client;
        return this;
    }

    public ClusterServerStateVO getServer() {
        return server;
    }

    public ClusterUniversalStateVO setServer(ClusterServerStateVO server) {
        this.server = server;
        return this;
    }

    public ClusterStateSimpleEntity getStateInfo() {
        return stateInfo;
    }

    public ClusterUniversalStateVO setStateInfo(
        ClusterStateSimpleEntity stateInfo) {
        this.stateInfo = stateInfo;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterUniversalStateVO{" +
            "stateInfo=" + stateInfo +
            ", client=" + client +
            ", server=" + server +
            '}';
    }
}
