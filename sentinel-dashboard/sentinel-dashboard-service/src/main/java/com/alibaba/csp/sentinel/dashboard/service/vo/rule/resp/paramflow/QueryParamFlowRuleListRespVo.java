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
package com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.paramflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author cdfive
 */
public class QueryParamFlowRuleListRespVo implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String app;

    private String ip;

    private Integer port;

    private String resource;

    private Integer paramIdx;

    private Integer grade;

    private Double count;

    private Long durationInSec;

    private Boolean clusterMode;

    private ParamFlowClusterConfigRespVo clusterConfig;

    private List<ParamFlowItemRespVo> paramFlowItemList;

    private Date gmtCreate;

    private Date gmtModified;

    public static class ParamFlowClusterConfigRespVo {
        private Integer thresholdType;

        private Boolean fallbackToLocalWhenFail;

        public Integer getThresholdType() {
            return thresholdType;
        }

        public void setThresholdType(Integer thresholdType) {
            this.thresholdType = thresholdType;
        }

        public Boolean getFallbackToLocalWhenFail() {
            return fallbackToLocalWhenFail;
        }

        public void setFallbackToLocalWhenFail(Boolean fallbackToLocalWhenFail) {
            this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        }
    }

    public static class ParamFlowItemRespVo {
        private String object;

        private Integer count;

        private String classType;

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getClassType() {
            return classType;
        }

        public void setClassType(String classType) {
            this.classType = classType;
        }
    }

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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
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

    public ParamFlowClusterConfigRespVo getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ParamFlowClusterConfigRespVo clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public List<ParamFlowItemRespVo> getParamFlowItemList() {
        return paramFlowItemList;
    }

    public void setParamFlowItemList(List<ParamFlowItemRespVo> paramFlowItemList) {
        this.paramFlowItemList = paramFlowItemList;
    }

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
}
