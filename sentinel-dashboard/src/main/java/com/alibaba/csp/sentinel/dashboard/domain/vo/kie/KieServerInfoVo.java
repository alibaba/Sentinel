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
package com.alibaba.csp.sentinel.dashboard.domain.vo.kie;

import lombok.Data;

@Data
public class KieServerInfoVo{
    private String id;

    private String hostname;
    private String ip;
    private int port;
    private Long heartbeatVersion;
    private Long lastHeartbeat;
    private boolean healthy;
    private String sentinelVersion;

    private String project;
    private String app;
    private String service;
    private String environment;
    private String serverVersion;
    private String kieAddress;
}
