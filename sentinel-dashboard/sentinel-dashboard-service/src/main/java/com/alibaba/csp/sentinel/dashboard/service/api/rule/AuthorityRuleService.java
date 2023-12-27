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
package com.alibaba.csp.sentinel.dashboard.service.api.rule;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.DeleteAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.authority.QueryAuthorityRuleListRespVo;

import java.util.List;

/**
 * @author cdfive
 */
public interface AuthorityRuleService {

    List<QueryAuthorityRuleListRespVo> queryAuthorityRuleList(MachineReqVo reqVo) throws Exception;

    void addAuthorityRule(AddAuthorityReqVo reqVo) throws Exception;

    void updateAuthorityRule(UpdateAuthorityReqVo reqVo) throws Exception;

    void deleteAuthorityRule(DeleteAuthorityReqVo reqVo) throws Exception;
}
