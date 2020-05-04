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
package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

/**
 * @author cdfive
 */
public class UpdateFlowRuleReqVo extends MachineReqVo {

    private static final long serialVersionUID = -6815341766673281757L;

    /**记录id*/
    private Long id;

    /**针对来源*/
    private String limitApp;

    /**阈值类型 0-线程数 1-QPS*/
    private Integer grade;

    /**阈值*/
    private Double count;

    /**流控模式 0-直接 1-关联 2-链路*/
    private Integer strategy;

    /**流控效果 0-快速失败 1-Warm Up 2-排队等待*/
    private Integer controlBehavior;

    /**关联资源名称,当strategy为1-关联*/
    private String refResource;

    /**预热时长,当controlBehavior为1-Warm Up*/
    private Integer warmUpPeriodSec;

    /**超时时间,当controlBehavior为2-排队等待*/
    private Integer maxQueueingTimeMs;

    /**是否集群*/
    private Boolean clusterMode;

    private AddFlowRuleReqVo.ClusterConfigReqVo clusterConfig;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLimitApp() {
        return limitApp;
    }

    public void setLimitApp(String limitApp) {
        this.limitApp = limitApp;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getStrategy() {
        return strategy;
    }

    public void setStrategy(Integer strategy) {
        this.strategy = strategy;
    }

    public Integer getControlBehavior() {
        return controlBehavior;
    }

    public void setControlBehavior(Integer controlBehavior) {
        this.controlBehavior = controlBehavior;
    }

    public String getRefResource() {
        return refResource;
    }

    public void setRefResource(String refResource) {
        this.refResource = refResource;
    }

    public Integer getWarmUpPeriodSec() {
        return warmUpPeriodSec;
    }

    public void setWarmUpPeriodSec(Integer warmUpPeriodSec) {
        this.warmUpPeriodSec = warmUpPeriodSec;
    }

    public Integer getMaxQueueingTimeMs() {
        return maxQueueingTimeMs;
    }

    public void setMaxQueueingTimeMs(Integer maxQueueingTimeMs) {
        this.maxQueueingTimeMs = maxQueueingTimeMs;
    }

    public Boolean getClusterMode() {
        return clusterMode;
    }

    public void setClusterMode(Boolean clusterMode) {
        this.clusterMode = clusterMode;
    }

    public AddFlowRuleReqVo.ClusterConfigReqVo getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(AddFlowRuleReqVo.ClusterConfigReqVo clusterConfig) {
        this.clusterConfig = clusterConfig;
    }
}
