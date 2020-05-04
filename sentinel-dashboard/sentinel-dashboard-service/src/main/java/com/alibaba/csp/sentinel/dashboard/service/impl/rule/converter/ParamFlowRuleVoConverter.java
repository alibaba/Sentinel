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

import com.alibaba.csp.sentinel.dashboard.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.AddParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.UpdateParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.paramflow.QueryParamFlowRuleListRespVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cdfive
 */
public class ParamFlowRuleVoConverter {

    public static QueryParamFlowRuleListRespVo convertList(ParamFlowRuleEntity entity) {
        QueryParamFlowRuleListRespVo vo = new QueryParamFlowRuleListRespVo();
        vo.setId(entity.getId());
        vo.setApp(entity.getApp());
        vo.setIp(entity.getIp());
        vo.setPort(entity.getPort());

        vo.setResource(entity.getResource());
        vo.setParamIdx(entity.getParamIdx());
        vo.setGrade(entity.getGrade());
        vo.setCount(entity.getCount());
        vo.setDurationInSec(entity.getDurationInSec());

        vo.setClusterMode(entity.getClusterMode());
        if (entity.getClusterMode() != null && entity.getClusterMode().booleanValue()) {
            ParamFlowRuleEntity.ParamFlowClusterConfigEntity clusterConfig = entity.getClusterConfig();
            if (clusterConfig != null) {
                QueryParamFlowRuleListRespVo.ParamFlowClusterConfigRespVo clusterConfigVo = new QueryParamFlowRuleListRespVo.ParamFlowClusterConfigRespVo();
                clusterConfigVo.setThresholdType(clusterConfig.getThresholdType());
                clusterConfigVo.setFallbackToLocalWhenFail(clusterConfig.getFallbackToLocalWhenFail());
                vo.setClusterConfig(clusterConfigVo);
            }
        }

        List<ParamFlowRuleEntity.ParamFlowItemEntity> itemEntities = entity.getParamFlowItemList();
        if (itemEntities != null) {
            List<QueryParamFlowRuleListRespVo.ParamFlowItemRespVo> itemVos = new ArrayList<>();
            for (ParamFlowRuleEntity.ParamFlowItemEntity itemEntity : itemEntities) {
                QueryParamFlowRuleListRespVo.ParamFlowItemRespVo itemVo = new QueryParamFlowRuleListRespVo.ParamFlowItemRespVo();
                itemVo.setObject(itemEntity.getObject());
                itemVo.setCount(itemEntity.getCount());
                itemVo.setClassType(itemEntity.getClassType());
                itemVos.add(itemVo);
            }
            vo.setParamFlowItemList(itemVos);
        }

        vo.setGmtCreate(entity.getGmtCreate());
        vo.setGmtModified(entity.getGmtModified());
        return vo;
    }

    public static ParamFlowRuleEntity convertAdd(AddParamFlowRuleReqVo reqVo) {
        ParamFlowRuleEntity entity = new ParamFlowRuleEntity();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());

        entity.setResource(reqVo.getResource());
        entity.setParamIdx(reqVo.getParamIdx());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setDurationInSec(reqVo.getDurationInSec());

        entity.setClusterMode(reqVo.getClusterMode());
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ParamFlowRuleEntity.ParamFlowClusterConfigEntity clusterConfigEntity = new ParamFlowRuleEntity.ParamFlowClusterConfigEntity();
            clusterConfigEntity.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterConfigEntity.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
            entity.setClusterConfig(clusterConfigEntity);
        }

        List<AddParamFlowRuleReqVo.ParamFlowItemReqVo> paramFlowItemReqVos = reqVo.getParamFlowItemList();
        if (paramFlowItemReqVos != null) {
            List<ParamFlowRuleEntity.ParamFlowItemEntity> itemEntities = new ArrayList<>();
            for (AddParamFlowRuleReqVo.ParamFlowItemReqVo paramFlowItemReqVo : paramFlowItemReqVos) {
                ParamFlowRuleEntity.ParamFlowItemEntity itemEntity = new ParamFlowRuleEntity.ParamFlowItemEntity();
                itemEntity.setObject(paramFlowItemReqVo.getObject());
                itemEntity.setCount(paramFlowItemReqVo.getCount());
                itemEntity.setClassType(paramFlowItemReqVo.getClassType());
                itemEntities.add(itemEntity);
            }
            entity.setParamFlowItemList(itemEntities);
        }

        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
        return entity;
    }

    public static void convertUpdate(UpdateParamFlowRuleReqVo reqVo, ParamFlowRuleEntity toUpdateRuleEntity) {
        toUpdateRuleEntity.setParamIdx(reqVo.getParamIdx());
        toUpdateRuleEntity.setGrade(reqVo.getGrade());
        toUpdateRuleEntity.setCount(reqVo.getCount());
        toUpdateRuleEntity.setDurationInSec(reqVo.getDurationInSec());

        toUpdateRuleEntity.setClusterMode(reqVo.getClusterMode());
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ParamFlowRuleEntity.ParamFlowClusterConfigEntity clusterConfigEntity = new ParamFlowRuleEntity.ParamFlowClusterConfigEntity();
            clusterConfigEntity.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterConfigEntity.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
            toUpdateRuleEntity.setClusterConfig(clusterConfigEntity);
        }

        List<AddParamFlowRuleReqVo.ParamFlowItemReqVo> paramFlowItemReqVos = reqVo.getParamFlowItemList();
        if (paramFlowItemReqVos != null) {
            List<ParamFlowRuleEntity.ParamFlowItemEntity> itemEntities = new ArrayList<>();
            for (AddParamFlowRuleReqVo.ParamFlowItemReqVo paramFlowItemReqVo : paramFlowItemReqVos) {
                ParamFlowRuleEntity.ParamFlowItemEntity itemEntity = new ParamFlowRuleEntity.ParamFlowItemEntity();
                itemEntity.setObject(paramFlowItemReqVo.getObject());
                itemEntity.setCount(paramFlowItemReqVo.getCount());
                itemEntity.setClassType(paramFlowItemReqVo.getClassType());
                itemEntities.add(itemEntity);
            }
            toUpdateRuleEntity.setParamFlowItemList(itemEntities);
        }

        toUpdateRuleEntity.setGmtModified(new Date());
    }
}
