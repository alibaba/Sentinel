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

import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.kie.KieServerInfoVo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/registry", produces = MediaType.APPLICATION_JSON_VALUE)
public class MachineRegistryController {

    private final Logger logger = LoggerFactory.getLogger(MachineRegistryController.class);

    @Autowired
    private AppManagement appManagement;

    @Autowired
    private KieServerManagement kieManagement;

    @ResponseBody
    @RequestMapping("/machine")
    public Result<?> receiveHeartBeat(String app, @RequestParam(value = "app_type", required = false, defaultValue = "0") Integer appType, Long version, String v, String hostname, String ip, Integer port) {
        if (app == null) {
            app = MachineDiscovery.UNKNOWN_APP_NAME;
        }
        if (ip == null) {
            return Result.ofFail(-1, "ip can't be null");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        if (port == -1) {
            logger.info("Receive heartbeat from " + ip + " but port not set yet");
            return Result.ofFail(-1, "your port not set yet");
        }
        String sentinelVersion = StringUtil.isEmpty(v) ? "unknown" : v;
        version = version == null ? System.currentTimeMillis() : version;
        try {
            MachineInfo machineInfo = new MachineInfo();
            machineInfo.setApp(app);
            machineInfo.setAppType(appType);
            machineInfo.setHostname(hostname);
            machineInfo.setIp(ip);
            machineInfo.setPort(port);
            machineInfo.setHeartbeatVersion(version);
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            machineInfo.setVersion(sentinelVersion);
            appManagement.addMachine(machineInfo);
            return Result.ofSuccessMsg("success");
        } catch (Exception e) {
            logger.error("Receive heartbeat error", e);
            return Result.ofFail(-1, e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping("/kieServer")
    public Result<?> receiveServerHeartBeat(@RequestBody KieServerInfoVo kieServerInfoVo){
        if (kieServerInfoVo.getIp() == null) {
            return Result.ofFail(-1, "ip can't be null");
        }
        if (kieServerInfoVo.getPort() == -1) {
            logger.info("Receive heartbeat from " + kieServerInfoVo.getIp() + " but port not set yet");
            return Result.ofFail(-1, "your port not set yet");
        }
        String sentinelVersion = StringUtil.isEmpty(kieServerInfoVo.getSentinelVersion()) ?
                "unknown" : kieServerInfoVo.getSentinelVersion();
        long heartbeatVersion = kieServerInfoVo.getHeartbeatVersion() == null ?
                System.currentTimeMillis() : kieServerInfoVo.getHeartbeatVersion();

        try{
            KieServerLabel label = KieServerLabel.builder()
                    .app(kieServerInfoVo.getApp())
                    .project(kieServerInfoVo.getProject())
                    .service(kieServerInfoVo.getService())
                    .serverVersion(kieServerInfoVo.getServerVersion())
                    .environment(kieServerInfoVo.getEnvironment())
                    .build();

            MachineInfo machineInfo = new MachineInfo();
            machineInfo.setIp(kieServerInfoVo.getIp());
            machineInfo.setPort(kieServerInfoVo.getPort());
            machineInfo.setHeartbeatVersion(heartbeatVersion);
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            machineInfo.setVersion(sentinelVersion);

            kieManagement.addMachineInfo(label, machineInfo);

            logger.info(String.format("Register to dashboard, %s.", machineInfo));
            return Result.ofSuccessMsg("success");
        }catch (Exception e){
            logger.error("Receive heartbeat error", e);
            return Result.ofFail(-1, e.getMessage());
        }
    }
}
