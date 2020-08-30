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
package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

/**
 * @author cdfive
 */
public class AddDegradeRuleReqVo extends MachineReqVo {

    /**资源名称*/
    private String resource;

    /**熔断策略 0-慢调用比例 1-异常比例 2-异常数*/
    private Integer grade;

    /**阈值*/
    private Double count;

    /**慢调用比例阈值*/
    private Double slowRatioThreshold;

    /**熔断时长,单位:秒*/
    private Integer timeWindow;

    /**最小请求数目*/
    private Integer minRequestAmount;

    /**统计窗口时长,单位:秒*/
    private Integer statIntervalMs;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
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

    public Double getSlowRatioThreshold() {
        return slowRatioThreshold;
    }

    public void setSlowRatioThreshold(Double slowRatioThreshold) {
        this.slowRatioThreshold = slowRatioThreshold;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Integer getMinRequestAmount() {
        return minRequestAmount;
    }

    public void setMinRequestAmount(Integer minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
    }

    public Integer getStatIntervalMs() {
        return statIntervalMs;
    }

    public void setStatIntervalMs(Integer statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
    }
}
