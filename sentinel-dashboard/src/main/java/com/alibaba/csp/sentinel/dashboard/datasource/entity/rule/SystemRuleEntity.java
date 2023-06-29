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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.system.SystemMetricType;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import java.util.Date;

/**
 * @author leyou
 * @author guozhong.huang
 */
public class SystemRuleEntity implements RuleEntity {

    private Long id;

    private String app;
    private String ip;
    private Integer port;
    private Double highestSystemLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;
    private Double highestCpuUsage;

    private Date gmtCreate;
    private Date gmtModified;

    public static SystemRuleEntity fromSystemRule(String app, String ip, Integer port, SystemRule rule) {
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);

        switch (rule.getSystemMetricType()) {
            case CPU_USAGE:
                entity.setHighestCpuUsage(rule.getTriggerCount());
                break;
            case LOAD:
                entity.setHighestSystemLoad(rule.getTriggerCount());
                break;
            case AVG_RT:
                entity.setAvgRt((long) rule.getTriggerCount());
                break;
            case CONCURRENCY:
                entity.setMaxThread((long) rule.getTriggerCount());
                break;
            case INBOUND_QPS:
                entity.setQps(rule.getTriggerCount());
                break;
            default:
                break;
        }
        return entity;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
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

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public SystemRule toRule() {
        SystemRule rule = new SystemRule();
        if (this.highestSystemLoad != null && this.highestSystemLoad <= 0) {
            rule.setTriggerCount(this.highestSystemLoad);
            rule.setSystemMetricType(SystemMetricType.LOAD);
            return rule;
        }

        if (this.highestCpuUsage != null && this.highestCpuUsage <= 0) {
            rule.setTriggerCount(this.highestCpuUsage);
            rule.setSystemMetricType(SystemMetricType.CPU_USAGE);
            return rule;
        }

        if (this.avgRt != null && this.avgRt > 0) {
            rule.setTriggerCount(this.avgRt);
            rule.setSystemMetricType(SystemMetricType.AVG_RT);
            return rule;
        }

        if (this.maxThread != null && this.maxThread > 0) {
            rule.setTriggerCount(this.maxThread);
            rule.setSystemMetricType(SystemMetricType.CONCURRENCY);
            return rule;

        }

        if (this.qps != null && this.qps > 0) {
            rule.setTriggerCount(this.qps);
            rule.setSystemMetricType(SystemMetricType.INBOUND_QPS);
            return rule;
        }
        return rule;
    }
}
