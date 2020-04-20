package com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;

import java.util.Date;

public class DegradeRuleVoConvertor {

    public static QueryDegradeRuleListRespVo convertList(DegradeRuleEntity entity) {
        QueryDegradeRuleListRespVo vo = new QueryDegradeRuleListRespVo();
        vo.setId(entity.getId());
        vo.setApp(entity.getApp());
        vo.setIp(entity.getIp());
        vo.setPort(entity.getPort());
        vo.setResource(entity.getResource());
        vo.setGrade(entity.getGrade());
        vo.setCount(entity.getCount());
        vo.setTimeWindow(entity.getTimeWindow());
        return vo;
    }

    public static DegradeRuleEntity convertAdd(AddDegradeRuleReqVo reqVo) {
        DegradeRuleEntity entity = new DegradeRuleEntity();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());

        // TODO

        return entity;
    }

    public static void convertUpdate(UpdateDegradeRuleReqVo reqVo, DegradeRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setGrade(reqVo.getGrade());
        toUpdateRuleEntity.setCount(reqVo.getCount());
        toUpdateRuleEntity.setTimeWindow(reqVo.getTimeWindow());
        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
