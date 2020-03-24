package com.alibaba.csp.sentinel.dashboard.vo.req.rule.flow;

import com.alibaba.csp.sentinel.dashboard.vo.req.MachineReqVo;

/**
 * @author cdfive
 */

//{"grade":1,"strategy":0,"controlBehavior":0,"app":"sentinel-dashboard","ip":"10.3.34.164","port":"8719",
//        "limitApp":"default","clusterMode":false,"clusterConfig":{"thresholdType":0,
//        "fallbackToLocalWhenFail":true},"resource":"aa","count":111}
public class AddFlowRuleReqVo extends MachineReqVo {

    private static final long serialVersionUID = 7843538810403392825L;

    /**资源名称*/
    private String resource;

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

    private ClusterConfigReqVo clusterConfig;

    public static class ClusterConfigReqVo {
        /**集群阈值模式 0-单机均摊 1-总体阈值*/
        private Integer thresholdType;

        /**失败退化,如果Token Server不可用是否退化到单机限流*/
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
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

    public ClusterConfigReqVo getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ClusterConfigReqVo clusterConfig) {
        this.clusterConfig = clusterConfig;
    }
}
