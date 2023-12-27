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
package com.alibaba.csp.sentinel.dashboard.entity.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;
import java.util.List;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
public class ParamFlowRuleEntity implements RuleEntity {

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

    private ParamFlowClusterConfigEntity clusterConfig;

    private List<ParamFlowItemEntity> paramFlowItemList;

    private Date gmtCreate;

    private Date gmtModified;

    public static class ParamFlowClusterConfigEntity {
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

    public static class ParamFlowItemEntity {
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

    @Override
    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
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

    public ParamFlowClusterConfigEntity getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ParamFlowClusterConfigEntity clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public List<ParamFlowItemEntity> getParamFlowItemList() {
        return paramFlowItemList;
    }

    public void setParamFlowItemList(List<ParamFlowItemEntity> paramFlowItemList) {
        this.paramFlowItemList = paramFlowItemList;
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
    public Rule toRule() {
        return null;
    }

    public static ParamFlowRuleEntity fromAuthorityRule(String app, String ip, Integer port, ParamFlowRule rule) {
        ParamFlowRuleEntity entity = new ParamFlowRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        return entity;
    }

    //
//
//    public ParamFlowRuleEntity() {
//    }
//
//    public ParamFlowRuleEntity(ParamFlowRule rule) {
//        AssertUtil.notNull(rule, "Authority rule should not be null");
//        this.rule = rule;
//    }
//
//    public static ParamFlowRuleEntity fromAuthorityRule(String app, String ip, Integer port, ParamFlowRule rule) {
//        ParamFlowRuleEntity entity = new ParamFlowRuleEntity(rule);
//        entity.setApp(app);
//        entity.setIp(ip);
//        entity.setPort(port);
//        return entity;
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public String getLimitApp() {
//        return rule.getLimitApp();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public String getResource() {
//        return rule.getResource();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public int getGrade() {
//        return rule.getGrade();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public Integer getParamIdx() {
//        return rule.getParamIdx();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public double getCount() {
//        return rule.getCount();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public List<ParamFlowItem> getParamFlowItemList() {
//        return rule.getParamFlowItemList();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public int getControlBehavior() {
//        return rule.getControlBehavior();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public int getMaxQueueingTimeMs() {
//        return rule.getMaxQueueingTimeMs();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public int getBurstCount() {
//        return rule.getBurstCount();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public long getDurationInSec() {
//        return rule.getDurationInSec();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public boolean isClusterMode() {
//        return rule.isClusterMode();
//    }
//
//    @JsonIgnore
//    @JSONField(serialize = false)
//    public ParamFlowClusterConfig getClusterConfig() {
//        return rule.getClusterConfig();
//    }
}
