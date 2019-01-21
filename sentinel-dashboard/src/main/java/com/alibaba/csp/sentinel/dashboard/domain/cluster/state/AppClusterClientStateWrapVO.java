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
 * @since 1.4.1
 */
public class AppClusterClientStateWrapVO {

    /**
     * {ip}@{transport_command_port}.
     */
    private String id;

    private Integer commandPort;
    private String ip;

    private ClusterClientStateVO state;

    public String getId() {
        return id;
    }

    public AppClusterClientStateWrapVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AppClusterClientStateWrapVO setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public ClusterClientStateVO getState() {
        return state;
    }

    public AppClusterClientStateWrapVO setState(ClusterClientStateVO state) {
        this.state = state;
        return this;
    }

    public Integer getCommandPort() {
        return commandPort;
    }

    public AppClusterClientStateWrapVO setCommandPort(Integer commandPort) {
        this.commandPort = commandPort;
        return this;
    }

    @Override
    public String toString() {
        return "AppClusterClientStateWrapVO{" +
            "id='" + id + '\'' +
            ", commandPort=" + commandPort +
            ", ip='" + ip + '\'' +
            ", state=" + state +
            '}';
    }
}
