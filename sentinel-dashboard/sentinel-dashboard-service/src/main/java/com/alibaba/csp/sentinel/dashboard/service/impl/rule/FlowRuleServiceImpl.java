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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.FlowRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.FlowRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor.FlowRuleVoConvertor;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class FlowRuleServiceImpl extends BaseRuleService<FlowRuleEntity> implements FlowRuleService {

    @Override
    public List<QueryFlowRuleListRespVo> queryFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<FlowRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> FlowRuleVoConvertor.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addFlowRule(AddFlowRuleReqVo reqVo) throws Exception {
        FlowRuleVoChecker.checkAdd(reqVo);
        FlowRuleEntity rule = FlowRuleVoConvertor.convertAdd(reqVo);
        addRule(reqVo, rule);
    }

    @Override
    public void updateFlowRule(UpdateFlowRuleReqVo reqVo) throws Exception {
        FlowRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo, reqVo.getId(), toUpdateRule -> FlowRuleVoConvertor.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteFlowRule(DeleteFlowRuleReqVo reqVo) throws Exception {
        FlowRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
