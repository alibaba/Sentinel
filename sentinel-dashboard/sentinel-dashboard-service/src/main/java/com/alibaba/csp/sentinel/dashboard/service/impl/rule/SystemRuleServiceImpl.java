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

import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.SystemRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.SystemRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.converter.SystemRuleVoConverter;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class SystemRuleServiceImpl extends AbstractRuleService<SystemRuleEntity> implements SystemRuleService {

    @Override
    public List<QuerySystemRuleListRespVo> querySystemRuleList(MachineReqVo reqVo) throws Exception {
        List<SystemRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> SystemRuleVoConverter.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addSystemRule(AddSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkAdd(reqVo);
        addRule(reqVo, SystemRuleVoConverter.convertAdd(reqVo));
    }

    @Override
    public void updateSystemRule(UpdateSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo,reqVo.getId(), toUpdateRule -> SystemRuleVoConverter.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteSystemRule(DeleteSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
