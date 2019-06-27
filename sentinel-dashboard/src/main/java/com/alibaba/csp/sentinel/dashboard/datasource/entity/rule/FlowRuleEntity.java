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

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Date;

/**
 * @author leyou
 */
public class FlowRuleEntity implements RuleEntity {

    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private String limitApp;
    private String resource;
    /**
     * 0为线程数;1为qps
     */
    private Integer grade;
    private Double count;
    /**
     * 0为直接限流;1为关联限流;2为链路限流
     ***/
    private Integer strategy;
    private String refResource;
    /**
     * 0. default, 1. warm up, 2. rate limiter
     */
    private Integer controlBehavior;
    private Integer warmUpPeriodSec;
    /**
     * max queueing time in rate limiter behavior
     */
    private Integer maxQueueingTimeMs;

    private boolean clusterMode;
    /**
     * Flow rule config for cluster mode.
     */
    private ClusterFlowConfig clusterConfig;

    private Date gmtCreate;
    private Date gmtModified;

    public static FlowRuleEntity fromFlowRule(String app, String ip, Integer port, FlowRule rule) {
        FlowRuleEntity entity = new FlowRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setLimitApp(rule.getLimitApp());
        entity.setResource(rule.getResource());
        entity.setGrade(rule.getGrade());
        entity.setCount(rule.getCount());
        entity.setStrategy(rule.getStrategy());
        entity.setRefResource(rule.getRefResource());
        entity.setControlBehavior(rule.getControlBehavior());
        entity.setWarmUpPeriodSec(rule.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(rule.getMaxQueueingTimeMs());
        entity.setClusterMode(rule.isClusterMode());
        entity.setClusterConfig(rule.getClusterConfig());
        return entity;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public FlowRuleEntity setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public FlowRuleEntity setPort(Integer port) {
        this.port = port;
        return this;
    }

    @Override
    public String getApp() {
        return app;
    }

    public FlowRuleEntity setApp(String app) {
        this.app = app;
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

    public String getLimitApp() {
        return limitApp;
    }

    public FlowRuleEntity setLimitApp(String limitApp) {
        this.limitApp = limitApp;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public FlowRuleEntity setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public FlowRuleEntity setGrade(Integer grade) {
        this.grade = grade;
        return this;
    }

    public Double getCount() {
        return count;
    }

    public FlowRuleEntity setCount(Double count) {
        this.count = count;
        return this;
    }

    public Integer getStrategy() {
        return strategy;
    }

    public FlowRuleEntity setStrategy(Integer strategy) {
        this.strategy = strategy;
        return this;
    }

    public String getRefResource() {
        return refResource;
    }

    public FlowRuleEntity setRefResource(String refResource) {
        this.refResource = refResource;
        return this;
    }

    public Integer getControlBehavior() {
        return controlBehavior;
    }

    public FlowRuleEntity setControlBehavior(Integer controlBehavior) {
        this.controlBehavior = controlBehavior;
        return this;
    }

    public Integer getWarmUpPeriodSec() {
        return warmUpPeriodSec;
    }

    public FlowRuleEntity setWarmUpPeriodSec(Integer warmUpPeriodSec) {
        this.warmUpPeriodSec = warmUpPeriodSec;
        return this;
    }

    public Integer getMaxQueueingTimeMs() {
        return maxQueueingTimeMs;
    }

    public FlowRuleEntity setMaxQueueingTimeMs(Integer maxQueueingTimeMs) {
        this.maxQueueingTimeMs = maxQueueingTimeMs;
        return this;
    }

    public boolean isClusterMode() {
        return clusterMode;
    }

    public FlowRuleEntity setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return this;
    }

    public ClusterFlowConfig getClusterConfig() {
        return clusterConfig;
    }

    public FlowRuleEntity setClusterConfig(ClusterFlowConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public FlowRuleEntity setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public FlowRuleEntity setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public FlowRule toRule() {
        FlowRule flowRule = new FlowRule();
        flowRule.setCount(this.count);
        flowRule.setGrade(this.grade);
        flowRule.setResource(this.resource);
        flowRule.setLimitApp(this.limitApp);
        flowRule.setRefResource(this.refResource);
        flowRule.setStrategy(this.strategy);
        if (this.controlBehavior != null) {
            flowRule.setControlBehavior(controlBehavior);
        }
        if (this.warmUpPeriodSec != null) {
            flowRule.setWarmUpPeriodSec(warmUpPeriodSec);
        }
        if (this.maxQueueingTimeMs != null) {
            flowRule.setMaxQueueingTimeMs(maxQueueingTimeMs);
        }
        flowRule.setClusterMode(clusterMode);
        flowRule.setClusterConfig(clusterConfig);
        return flowRule;
    }

}
