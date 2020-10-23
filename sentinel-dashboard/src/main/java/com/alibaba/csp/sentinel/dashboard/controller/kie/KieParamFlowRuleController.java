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
package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKieProvider;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKiePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Parameter flow rule controller.
 *
 * @author Sherlockhan
 */
@RestController
@RequestMapping(value = "/kie/paramFlow")
public class KieParamFlowRuleController {

    private final Logger logger = LoggerFactory.getLogger(KieParamFlowRuleController.class);

    @Autowired
    @Qualifier("paramFlowRuleKieProvider")
    private RuleKieProvider<List<ParamFlowRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("paramFlowRuleKiePublisher")
    private RuleKiePublisher<List<ParamFlowRuleEntity>> rulePublisher;

    @GetMapping("/rules")
    public Result<List<ParamFlowRuleEntity>> apiQueryRules(@RequestParam(name = "serverId") String serverId) {
        if (StringUtil.isEmpty(serverId)) {
            return Result.ofFail(-1, "Id can't be null or empty");
        }

        try {
            List<ParamFlowRuleEntity> rules = ruleProvider.getRules(serverId);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying paramFlow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/{serverId}/rule")
    public Result<ParamFlowRuleEntity> apiAddParamFlowRule(@PathVariable("serverId") String serverId,
                                                           @RequestBody ParamFlowRuleEntity entity) {
        Result<ParamFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.getRule().setResource(entity.getResource().trim());
        try{
            rulePublisher.add(serverId, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            logger.error("Error when add degrade rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    private <R> Result<R> checkEntityInternal(ParamFlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (entity.getRule() == null) {
            return Result.ofFail(-1, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource name cannot be null or empty");
        }
        if (entity.getCount() < 0) {
            return Result.ofFail(-1, "count should be valid");
        }
        if (entity.getGrade() != RuleConstant.FLOW_GRADE_QPS) {
            return Result.ofFail(-1, "Unknown mode (blockGrade) for parameter flow control");
        }
        if (entity.getParamIdx() == null || entity.getParamIdx() < 0) {
            return Result.ofFail(-1, "paramIdx should be valid");
        }
        if (entity.getDurationInSec() <= 0) {
            return Result.ofFail(-1, "durationInSec should be valid");
        }
        if (entity.getControlBehavior() < 0) {
            return Result.ofFail(-1, "controlBehavior should be valid");
        }
        return null;
    }

    @PutMapping("/{serverId}/rule")
    public Result<ParamFlowRuleEntity> apiUpdateParamFlowRule(@PathVariable("serverId") String serverId,
                                                              @RequestBody ParamFlowRuleEntity entity) {
        Result<ParamFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            rulePublisher.update(serverId, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when update paramFlow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @DeleteMapping("/{serverId}/rule/{ruleId}")
    public Result<String> apiDeleteRule(@PathVariable("serverId") String serverId,
                                        @PathVariable("ruleId") String ruleId) {
        if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(ruleId)) {
            return Result.ofFail(-1, "id can't be null");
        }

        try{
            rulePublisher.delete(serverId, ruleId);
            return Result.ofSuccess(ruleId);
        } catch (Throwable throwable) {
            logger.error("Error when delete rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }
}
