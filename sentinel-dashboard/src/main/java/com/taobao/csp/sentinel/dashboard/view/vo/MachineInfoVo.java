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
package com.taobao.csp.sentinel.dashboard.view.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.taobao.csp.sentinel.dashboard.discovery.MachineDiscovery;
import com.taobao.csp.sentinel.dashboard.discovery.MachineInfo;

/**
 * @author leyou
 */
public class MachineInfoVo {

    private String app;
    private String hostname;
    private String ip;
    private Integer port;
    private Date timestamp;
    private boolean health;

    private String version;

    public static List<MachineInfoVo> fromMachineInfoList(List<MachineInfo> machines) {
        List<MachineInfoVo> list = new ArrayList<>();
        for (MachineInfo machine : machines) {
            list.add(fromMachineInfo(machine));
        }
        return list;
    }

    public static MachineInfoVo fromMachineInfo(MachineInfo machine) {
        MachineInfoVo vo = new MachineInfoVo();
        vo.setApp(machine.getApp());
        vo.setHostname(machine.getHostname());
        vo.setIp(machine.getIp());
        vo.setPort(machine.getPort());
        vo.setTimestamp(machine.getTimestamp());
        vo.setVersion(machine.getVersion());
        if (System.currentTimeMillis() - machine.getTimestamp().getTime() < MachineDiscovery.MAX_CLIENT_LIVE_TIME_MS) {
            vo.setHealth(true);
        }
        return vo;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public MachineInfoVo setVersion(String version) {
        this.version = version;
        return this;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }
}
