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

import com.alibaba.csp.sentinel.dashboard.service.api.rule.FlowRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.web.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Flow rule controller.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/flow")
public class FlowRuleController {

    @Autowired
    private FlowRuleService flowRuleService;

    @GetMapping("/rules")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<QueryFlowRuleListRespVo>> queryFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<QueryFlowRuleListRespVo> list = flowRuleService.queryFlowRuleList(reqVo);
        return Result.ofSuccess(list);
    }

    @PostMapping("/rule")
    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> addFlowRule(@RequestBody AddFlowRuleReqVo reqVo) throws Exception {
        flowRuleService.addFlowRule(reqVo);
        return Result.ofSuccess(null);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<?> updateFlowRule(@PathVariable("id") Long id, @RequestBody UpdateFlowRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        flowRuleService.updateFlowRule(reqVo);
        return Result.ofSuccess(null);
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<?> apiDeleteRule(@PathVariable("id") Long id, @RequestBody DeleteFlowRuleReqVo reqVo) throws Exception {
        reqVo.setId(id);
        flowRuleService.deleteFlowRule(reqVo);
        return Result.ofSuccess(null);
    }
}
