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

import com.alibaba.csp.sentinel.dashboard.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.AuthorityRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.AuthorityRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.converter.AuthorityRuleVoConverter;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.DeleteAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.authority.QueryAuthorityRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class AuthorityRuleServiceImpl extends AbstractRuleService<AuthorityRuleEntity> implements AuthorityRuleService {

    @Override
    public List<QueryAuthorityRuleListRespVo> queryAuthorityRuleList(MachineReqVo reqVo) throws Exception {
        List<AuthorityRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> AuthorityRuleVoConverter.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addAuthorityRule(AddAuthorityReqVo reqVo) throws Exception {
        AuthorityRuleVoChecker.checkAdd(reqVo);
        addRule(reqVo, AuthorityRuleVoConverter.convertAdd(reqVo));
    }

    @Override
    public void updateAuthorityRule(UpdateAuthorityReqVo reqVo) throws Exception {
        AuthorityRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo, reqVo.getId(), toUpdateRule -> AuthorityRuleVoConverter.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteAuthorityRule(DeleteAuthorityReqVo reqVo) throws Exception {
        AuthorityRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
