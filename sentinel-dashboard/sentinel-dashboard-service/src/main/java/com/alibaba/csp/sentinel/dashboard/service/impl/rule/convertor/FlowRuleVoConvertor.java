package com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;

import java.util.Date;

/**
 * @author cdfive
 */
public class FlowRuleVoConvertor {

    public static QueryFlowRuleListRespVo convertList(FlowRuleEntity entity) {
        QueryFlowRuleListRespVo vo = new QueryFlowRuleListRespVo();
        vo.setId(entity.getId());
        vo.setApp(entity.getApp());
        vo.setIp(entity.getIp());
        vo.setPort(entity.getPort());
        vo.setResource(entity.getResource());
        vo.setLimitApp(entity.getLimitApp());
        vo.setGrade(entity.getGrade());
        vo.setCount(entity.getCount());
        vo.setStrategy(entity.getStrategy());
        vo.setRefResource(entity.getRefResource());
        vo.setControlBehavior(entity.getControlBehavior());
        vo.setWarmUpPeriodSec(entity.getWarmUpPeriodSec());
        vo.setMaxQueueingTimeMs(entity.getMaxQueueingTimeMs());
        vo.setClusterMode(entity.isClusterMode());
        vo.setClusterConfig(entity.getClusterConfig());
        vo.setGmtCreate(entity.getGmtCreate());
        vo.setGmtModified(entity.getGmtModified());
        return vo;
    }

    public static FlowRuleEntity convertAdd(AddFlowRuleReqVo reqVo) {
        FlowRuleEntity entity = new FlowRuleEntity();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());
        entity.setResource(reqVo.getResource());
        entity.setLimitApp(reqVo.getLimitApp());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setStrategy(reqVo.getStrategy());
        entity.setControlBehavior(reqVo.getControlBehavior());
        entity.setRefResource(reqVo.getRefResource());
        entity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
        entity.setClusterMode(reqVo.getClusterMode());
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
            clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
            entity.setClusterConfig(clusterFlowConfig);
        }
        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
        return entity;
    }

    public static void convertUpdate(UpdateFlowRuleReqVo reqVo, FlowRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setLimitApp(reqVo.getLimitApp());
        toUpdateRuleEntity.setGrade(reqVo.getGrade());
        toUpdateRuleEntity.setCount(reqVo.getCount());
        toUpdateRuleEntity.setStrategy(reqVo.getStrategy());
        toUpdateRuleEntity.setControlBehavior(reqVo.getControlBehavior());
        toUpdateRuleEntity.setRefResource(reqVo.getRefResource());
        toUpdateRuleEntity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
        toUpdateRuleEntity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
        toUpdateRuleEntity.setClusterMode(reqVo.getClusterMode());
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
            clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
            toUpdateRuleEntity.setClusterConfig(clusterFlowConfig);
        }
        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
