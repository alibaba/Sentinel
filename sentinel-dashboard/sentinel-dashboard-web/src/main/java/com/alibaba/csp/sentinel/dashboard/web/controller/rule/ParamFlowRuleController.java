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

import com.alibaba.csp.sentinel.dashboard.service.api.rule.ParamFlowRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.AddParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.DeleteParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.UpdateParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.paramflow.QueryParamFlowRuleListRespVo;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ParamFlow rule controller.
 *
 * @author cdfive
 */
@RestController
@RequestMapping("/paramFlow")
public class ParamFlowRuleController {

    @Autowired
    private ParamFlowRuleService paramFlowRuleService;

    @GetMapping("/rules")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<QueryParamFlowRuleListRespVo>> queryFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<QueryParamFlowRuleListRespVo> rules = paramFlowRuleService.queryParamFlowRuleList(reqVo);
        return Result.ofSuccess(rules);
    }

    @PostMapping("/rule")
    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> addFlowRule(@RequestBody AddParamFlowRuleReqVo reqVo) throws Exception {
        paramFlowRuleService.addParamFlowRule(reqVo);
        return Result.ofSuccess();
    }

    @PutMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> updateFlowRule(@PathVariable("id") Long id, @RequestBody UpdateParamFlowRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        paramFlowRuleService.updateParamFlowRule(reqVo);
        return Result.ofSuccess();
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<?> deleteFlowRule(@PathVariable("id") Long id, @RequestBody DeleteParamFlowRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        paramFlowRuleService.deleteParamFlowRule(reqVo);
        return Result.ofSuccess();
    }
}
