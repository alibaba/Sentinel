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
package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class KieServerInfo{
    protected String hostname;
    protected String ip;
    protected int port;
    protected long heartbeatVersion;
    protected long lastHeartbeat;
    protected boolean healthy;
    protected String sentinelVersion;

    private String project;
    private String app;
    private String server;
    private String environment;
    private String serverVersion;

    @Override
    public int hashCode(){
        return Objects.hash(project, app, server, environment, serverVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof KieServerInfo)) { return false; }
        KieServerInfo that = (KieServerInfo)o;
        return Objects.equals(project, that.project) &&
                Objects.equals(app, that.app) &&
                Objects.equals(server, that.server)&&
                Objects.equals(environment, that.environment)&&
                Objects.equals(serverVersion, that.serverVersion);
    }
}
