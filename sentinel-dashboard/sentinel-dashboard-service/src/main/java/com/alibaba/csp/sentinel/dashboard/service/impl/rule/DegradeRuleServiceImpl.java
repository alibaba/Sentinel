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

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.DegradeRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.DegradeRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.converter.DegradeRuleVoConverter;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class DegradeRuleServiceImpl extends AbstractRuleService<DegradeRuleEntity> implements DegradeRuleService {

    @Override
    public List<QueryDegradeRuleListRespVo> queryDegradeRuleList(MachineReqVo reqVo) throws Exception {
        List<DegradeRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> DegradeRuleVoConverter.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addDegradeRule(AddDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkAdd(reqVo);
        DegradeRuleEntity rule = DegradeRuleVoConverter.convertAdd(reqVo);
        addRule(reqVo, rule);
    }

    @Override
    public void updateDegradeRule(UpdateDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo, reqVo.getId(), toUpdateRule -> DegradeRuleVoConverter.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteDegradeRule(DeleteDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
