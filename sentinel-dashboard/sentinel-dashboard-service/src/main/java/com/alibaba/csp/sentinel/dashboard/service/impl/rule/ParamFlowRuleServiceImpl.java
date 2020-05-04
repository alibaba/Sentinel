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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.ParamFlowRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.ParamFlowRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.converter.ParamFlowRuleVoConverter;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.AddParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.DeleteParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.UpdateParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.paramflow.QueryParamFlowRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class ParamFlowRuleServiceImpl extends AbstractRuleService<ParamFlowRuleEntity> implements ParamFlowRuleService {

    @Override
    public List<QueryParamFlowRuleListRespVo> queryParamFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<ParamFlowRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> ParamFlowRuleVoConverter.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addParamFlowRule(AddParamFlowRuleReqVo reqVo) throws Exception {
        ParamFlowRuleVoChecker.checkAdd(reqVo);
        ParamFlowRuleEntity rules = ParamFlowRuleVoConverter.convertAdd(reqVo);
        addRule(reqVo, rules);
    }

    @Override
    public void updateParamFlowRule(UpdateParamFlowRuleReqVo reqVo) throws Exception {
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo, reqVo.getId(), toUpdateRule -> ParamFlowRuleVoConverter.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteParamFlowRule(DeleteParamFlowRuleReqVo reqVo) throws Exception {
        ParamFlowRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
