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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
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
 * System rule controller (v2).
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/kie/system")
public class KieSystemController {

    private final Logger logger = LoggerFactory.getLogger(KieSystemController.class);

    @Autowired
    @Qualifier("systemRuleKieProvider")
    private RuleKieProvider<List<SystemRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("systemRuleKiePublisher")
    private RuleKiePublisher<List<SystemRuleEntity>> rulePublisher;

    @GetMapping("/rules")
    public Result<List<SystemRuleEntity>> apiQueryRules(@RequestParam(name = "serverId") String serverId) {
        if (StringUtil.isEmpty(serverId)) {
            return Result.ofFail(-1, "Id can't be null or empty");
        }

        try {
            List<SystemRuleEntity> rules = ruleProvider.getRules(serverId);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PutMapping("/{serverId}/rule")
    public Result<SystemRuleEntity> updateRules(@PathVariable("serverId") String serverId,
                                                    @RequestBody SystemRuleEntity entity){
        try {
            rulePublisher.update(serverId, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when update flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/{serverId}/rule")
    public Result<SystemRuleEntity> apiAddRule(@PathVariable("serverId") String serverId,
                                               @RequestBody SystemRuleEntity entity) {
        // -1 is a fake value
        if (entity.getHighestSystemLoad() == null) {
            entity.setHighestSystemLoad(-1D);
        } else {
            entity.setResource("highestSystemLoad");
        }

        if (entity.getHighestCpuUsage() == null) {
            entity.setHighestCpuUsage(-1D);
        }else {
            entity.setResource("highestCpuUsage");
        }

        if (entity.getAvgRt() == null) {
            entity.setAvgRt(-1L);
        }else {
            entity.setResource("avgRt");
        }

        if (entity.getMaxThread() == null) {
            entity.setMaxThread(-1L);
        }else {
            entity.setResource("maxThread");
        }

        if (entity.getQps() == null) {
            entity.setQps(-1D);
        }else {
            entity.setResource("qps");
        }

        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try{
            rulePublisher.add(serverId, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            logger.error("Error when add flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }

        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/{serverId}/rule/{ruleId}")
    public Result<String> apiDelRule(@PathVariable("serverId") String serverId,
                                     @PathVariable("ruleId") String ruleId){
        try{
            rulePublisher.delete(serverId, ruleId);
            return Result.ofSuccess(ruleId);
        } catch (Throwable throwable) {
            logger.error("Error when delete rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }
}
