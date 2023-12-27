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

import com.alibaba.csp.sentinel.dashboard.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.authority.QueryAuthorityRuleListRespVo;

import java.util.Date;

/**
 * @author cdfive
 */
public class AuthorityRuleVoConverter {

    public static QueryAuthorityRuleListRespVo convertList(AuthorityRuleEntity entity) {
        QueryAuthorityRuleListRespVo vo = new QueryAuthorityRuleListRespVo();
        vo.setId(entity.getId());
        vo.setApp(entity.getApp());
        vo.setIp(entity.getIp());
        vo.setPort(entity.getPort());
        vo.setResource(entity.getResource());
        vo.setLimitApp(entity.getLimitApp());
        vo.setStrategy(entity.getStrategy());
        return vo;
    }

    public static AuthorityRuleEntity convertAdd(AddAuthorityReqVo reqVo) {
        AuthorityRuleEntity entity = new AuthorityRuleEntity();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());
        entity.setResource(reqVo.getResource());
        entity.setLimitApp(reqVo.getLimitApp());
        entity.setStrategy(reqVo.getStrategy());
        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
        return entity;
    }

    public static void convertUpdate(UpdateAuthorityReqVo reqVo, AuthorityRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setLimitApp(reqVo.getLimitApp());
        toUpdateRuleEntity.setStrategy(reqVo.getStrategy());
        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
