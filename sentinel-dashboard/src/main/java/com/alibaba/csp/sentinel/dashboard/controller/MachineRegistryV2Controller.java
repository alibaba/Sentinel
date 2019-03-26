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
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.discovery.ApolloClientManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping(value = "/registryV2", produces = MediaType.APPLICATION_JSON_VALUE)
public class MachineRegistryV2Controller {

    private final Logger logger = LoggerFactory.getLogger(MachineRegistryV2Controller.class);

    @Autowired
    private AppManagement appManagement;

    @ResponseBody
    @RequestMapping("/machine")
    public Result<?> receiveHeartBeat(ApolloMachineInfo apolloMachineInfo, String v) {
        if (apolloMachineInfo.getApp() == null) {
            apolloMachineInfo.setApp(MachineDiscovery.UNKNOWN_APP_NAME);
        }
        if (apolloMachineInfo.getIp() == null) {
            return Result.ofFail(-1, "ip can't be null");
        }
        if (apolloMachineInfo.getPort() == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        if (apolloMachineInfo.getPort() == -1) {
            logger.info("Receive heartbeat from {} but port not set yet", apolloMachineInfo.getIp());
            return Result.ofFail(-1, "your port not set yet");
        }
        String sentinelVersion = StringUtil.isEmpty(v) ? "unknown" : v;
        String version = apolloMachineInfo.getVersion();
        try {
            long timestamp = version == null ? System.currentTimeMillis() : Long.parseLong(version);
            apolloMachineInfo.setTimestamp(new Date(timestamp));
            apolloMachineInfo.setVersion(sentinelVersion);
            ApolloClientManagement.createClient(apolloMachineInfo);
            appManagement.addMachine(apolloMachineInfo);
            return Result.ofSuccessMsg("success");
        } catch (Exception e) {
            logger.error("Receive heartbeat error", e);
            return Result.ofFail(-1, e.getMessage());
        }
    }
}
