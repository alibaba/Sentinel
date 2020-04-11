package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;

/**
 * @author cdfive
 */
public class RuleVoConvertor {

    public static QueryFlowRuleListRespVo convert(FlowRuleEntity entity) {
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
}
