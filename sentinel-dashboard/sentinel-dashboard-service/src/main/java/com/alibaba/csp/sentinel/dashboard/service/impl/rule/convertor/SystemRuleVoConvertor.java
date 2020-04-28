package com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;

import java.util.Date;

/**
 * @author cdfive
 */
public class SystemRuleVoConvertor {

    public static QuerySystemRuleListRespVo convertList(SystemRuleEntity entity) {
        QuerySystemRuleListRespVo vo = new QuerySystemRuleListRespVo();
        vo.setId(entity.getId());
        vo.setApp(entity.getApp());
        vo.setIp(entity.getIp());
        vo.setPort(entity.getPort());
        vo.setHighestSystemLoad(entity.getHighestSystemLoad());
        vo.setAvgRt(entity.getAvgRt());
        vo.setMaxThread(entity.getMaxThread());
        vo.setQps(entity.getQps());
        vo.setHighestCpuUsage(entity.getHighestCpuUsage());
        return vo;
    }

    public static SystemRuleEntity convertAdd(AddSystemRuleReqVo reqVo) {
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());
        entity.setHighestSystemLoad(reqVo.getHighestSystemLoad());
        entity.setAvgRt(reqVo.getAvgRt());
        entity.setMaxThread(reqVo.getMaxThread());
        entity.setQps(reqVo.getQps());
        entity.setHighestCpuUsage(reqVo.getHighestCpuUsage());
        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
        return entity;
    }

    public static void convertUpdate(UpdateSystemRuleReqVo reqVo, SystemRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setHighestSystemLoad(reqVo.getHighestSystemLoad());
        toUpdateRuleEntity.setAvgRt(reqVo.getAvgRt());
        toUpdateRuleEntity.setMaxThread(reqVo.getMaxThread());
        toUpdateRuleEntity.setQps(reqVo.getQps());
        toUpdateRuleEntity.setHighestCpuUsage(reqVo.getHighestCpuUsage());
        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
