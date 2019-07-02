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

import com.alibaba.csp.sentinel.slots.system.SystemRule;

import java.util.Date;

/**
 * @author leyou
 */
public class SystemRuleEntity implements RuleEntity {

    private Long id;

    private String app;
    private String ip;
    private Integer port;
    private Double avgLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;

    private Date gmtCreate;
    private Date gmtModified;

    public static SystemRuleEntity fromSystemRule(String app, String ip, Integer port, SystemRule rule) {
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setAvgLoad(rule.getHighestSystemLoad());
        entity.setAvgRt(rule.getAvgRt());
        entity.setMaxThread(rule.getMaxThread());
        entity.setQps(rule.getQps());
        return entity;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public SystemRuleEntity setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public SystemRuleEntity setPort(Integer port) {
        this.port = port;
        return this;
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

    public SystemRuleEntity setApp(String app) {
        this.app = app;
        return this;
    }

    public Double getAvgLoad() {
        return avgLoad;
    }

    public SystemRuleEntity setAvgLoad(Double avgLoad) {
        this.avgLoad = avgLoad;
        return this;
    }

    public Long getAvgRt() {
        return avgRt;
    }

    public SystemRuleEntity setAvgRt(Long avgRt) {
        this.avgRt = avgRt;
        return this;
    }

    public Long getMaxThread() {
        return maxThread;
    }

    public SystemRuleEntity setMaxThread(Long maxThread) {
        this.maxThread = maxThread;
        return this;
    }

    public Double getQps() {
        return qps;
    }

    public SystemRuleEntity setQps(Double qps) {
        this.qps = qps;
        return this;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public SystemRuleEntity setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public SystemRuleEntity setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public SystemRule toRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(avgLoad);
        rule.setAvgRt(avgRt);
        rule.setMaxThread(maxThread);
        rule.setQps(qps);
        return rule;
    }
}
