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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule.converter;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;

import java.util.Date;

public class DegradeRuleVoConverter {

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
        entity.setPort(reqVo.getPort());
        entity.setResource(reqVo.getResource());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setTimeWindow(reqVo.getTimeWindow());
        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
        return entity;
    }

    public static void convertUpdate(UpdateDegradeRuleReqVo reqVo, DegradeRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setGrade(reqVo.getGrade());
        toUpdateRuleEntity.setCount(reqVo.getCount());
        toUpdateRuleEntity.setTimeWindow(reqVo.getTimeWindow());
        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
