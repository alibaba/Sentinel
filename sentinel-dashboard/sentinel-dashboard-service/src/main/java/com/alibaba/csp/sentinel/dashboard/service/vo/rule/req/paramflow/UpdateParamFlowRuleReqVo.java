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
package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

import java.util.List;

/**
 * @author cdfive
 */
public class UpdateParamFlowRuleReqVo extends MachineReqVo {

    private Long id;

    private Integer paramIdx;

    private Integer grade;

    private Double count;

    private Long durationInSec;

    private Boolean clusterMode;

    private AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfig;

    private List<AddParamFlowRuleReqVo.ParamFlowItemReqVo> paramFlowItemList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getParamIdx() {
        return paramIdx;
    }

    public void setParamIdx(Integer paramIdx) {
        this.paramIdx = paramIdx;
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

    public Long getDurationInSec() {
        return durationInSec;
    }

    public void setDurationInSec(Long durationInSec) {
        this.durationInSec = durationInSec;
    }

    public Boolean getClusterMode() {
        return clusterMode;
    }

    public void setClusterMode(Boolean clusterMode) {
        this.clusterMode = clusterMode;
    }

    public AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public List<AddParamFlowRuleReqVo.ParamFlowItemReqVo> getParamFlowItemList() {
        return paramFlowItemList;
    }

    public void setParamFlowItemList(List<AddParamFlowRuleReqVo.ParamFlowItemReqVo> paramFlowItemList) {
        this.paramFlowItemList = paramFlowItemList;
    }
}
