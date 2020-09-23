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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKieProvider;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKiePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Flow rule controller (v2).
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/kie/flow")
public class KieFlowController {

    private final Logger logger = LoggerFactory.getLogger(KieFlowController.class);

    @Autowired
    @Qualifier("flowRuleKieProvider")
    private RuleKieProvider<List<FlowRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("flowRuleKiePublisher")
    private RuleKiePublisher<List<FlowRuleEntity>> rulePublisher;

    @GetMapping("/rules")
    public Result<List<FlowRuleEntity>> apiQueryRules(@RequestParam(name = "server_id") String serverId) {
        if (StringUtil.isEmpty(serverId)) {
            return Result.ofFail(-1, "Id can't be null or empty");
        }

        try {
            List<FlowRuleEntity> rules = ruleProvider.getRules(serverId);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PutMapping("/{server_id}/rules")
    public Result<List<FlowRuleEntity>> updateRules(@PathVariable("server_id") String serverId,
                                                    @RequestBody List<FlowRuleEntity> entities){
        try {
            rulePublisher.update(serverId, entities);
            return Result.ofSuccess(entities);
        } catch (Throwable throwable) {
            logger.error("Error when update flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }


    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (entity.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (entity.getGrade() != 0 && entity.getGrade() != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but " + entity.getGrade() + " got");
        }
        if (entity.getCount() == null || entity.getCount() < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        if (entity.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (entity.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = entity.getControlBehavior();
        if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (entity.isClusterMode() && entity.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }
        return null;
    }

    @PostMapping("/{server_id}/rule")
    public Result<FlowRuleEntity> apiAddFlowRule(@PathVariable("server_id") String serverId,
                                                 @RequestBody FlowRuleEntity entity) {
        Result<FlowRuleEntity> checkResult = checkEntityInternal(entity);

        try{
            rulePublisher.add(serverId, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            logger.error("Error when add flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }

        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/{server_id}/rule/{rule_id}")
    public Result<String> apiDelFlowRule(@PathVariable("server_id") String serverId,
                                         @PathVariable("rule_id") String ruleId){
        try{
            rulePublisher.delete(serverId, ruleId);
            return Result.ofSuccess(ruleId);
        } catch (Throwable throwable) {
            logger.error("Error when delete rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }
}
