/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;

/**
 * @author cdfive
 */
public class QuerySystemRuleListRespVo implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String app;

    private String ip;

    private Integer port;

    private Double highestSystemLoad;

    private Long avgRt;

    private Long maxThread;

    private Double qps;

    private Double highestCpuUsage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
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

    public Double getHighestSystemLoad() {
        return highestSystemLoad;
    }

    public void setHighestSystemLoad(Double highestSystemLoad) {
        this.highestSystemLoad = highestSystemLoad;
    }

    public Long getAvgRt() {
        return avgRt;
    }

    public void setAvgRt(Long avgRt) {
        this.avgRt = avgRt;
    }

    public Long getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(Long maxThread) {
        this.maxThread = maxThread;
    }

    public Double getQps() {
        return qps;
    }

    public void setQps(Double qps) {
        this.qps = qps;
    }

    public Double getHighestCpuUsage() {
        return highestCpuUsage;
    }

    public void setHighestCpuUsage(Double highestCpuUsage) {
        this.highestCpuUsage = highestCpuUsage;
    }
}
