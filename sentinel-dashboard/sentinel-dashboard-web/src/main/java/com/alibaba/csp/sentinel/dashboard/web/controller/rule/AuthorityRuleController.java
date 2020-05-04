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
package com.alibaba.csp.sentinel.dashboard.web.controller.rule;

import com.alibaba.csp.sentinel.dashboard.service.api.rule.AuthorityRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.DeleteAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.authority.QueryAuthorityRuleListRespVo;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authority rule controller.
 *
 * @author cdfive
 */
@RestController
@RequestMapping("/authority")
public class AuthorityRuleController {

    @Autowired
    private AuthorityRuleService authorityRuleService;

    @GetMapping("/rules")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<QueryAuthorityRuleListRespVo>> queryFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<QueryAuthorityRuleListRespVo> rules = authorityRuleService.queryAuthorityRuleList(reqVo);
        return Result.ofSuccess(rules);
    }

    @PostMapping("/rule")
    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> addFlowRule(@RequestBody AddAuthorityReqVo reqVo) throws Exception {
        authorityRuleService.addAuthorityRule(reqVo);
        return Result.ofSuccess(null);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> updateFlowRule(@PathVariable("id") Long id, @RequestBody UpdateAuthorityReqVo reqVo) throws Exception {
        reqVo.setId(id);
        authorityRuleService.updateAuthorityRule(reqVo);
        return Result.ofSuccess();
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<?> deleteFlowRule(@PathVariable("id") Long id, @RequestBody DeleteAuthorityReqVo reqVo) throws Exception {
        reqVo.setId(id);
        authorityRuleService.deleteAuthorityRule(reqVo);
        return Result.ofSuccess(null);
    }
}
