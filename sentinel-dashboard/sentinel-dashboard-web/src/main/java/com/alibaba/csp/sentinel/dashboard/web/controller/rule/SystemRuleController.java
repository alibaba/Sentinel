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
package com.alibaba.csp.sentinel.dashboard.web.controller.rule;

import com.alibaba.csp.sentinel.dashboard.service.api.rule.SystemRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * System rule controller.
 *
 * @author cdfive
 */
@RestController
@RequestMapping("/system")
public class SystemRuleController {

    @Autowired
    private SystemRuleService systemRuleService;

    @GetMapping("/rules")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<QuerySystemRuleListRespVo>> querySystemRuleList(MachineReqVo reqVo) throws Exception {
        List<QuerySystemRuleListRespVo> rules = systemRuleService.querySystemRuleList(reqVo);
        return Result.ofSuccess(rules);
    }

    @PostMapping("/rule")
    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> addSystemRule(@RequestBody AddSystemRuleReqVo reqVo) throws Exception {
        systemRuleService.addSystemRule(reqVo);
        return Result.ofSuccess(null);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> updateSytemRule(@PathVariable("id") Long id, @RequestBody UpdateSystemRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        systemRuleService.updateSystemRule(reqVo);
        return Result.ofSuccess(null);
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<?> deleteSystemRule(@PathVariable("id") Long id, @RequestBody DeleteSystemRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        systemRuleService.deleteSystemRule(reqVo);
        return Result.ofSuccess(null);
    }
}
