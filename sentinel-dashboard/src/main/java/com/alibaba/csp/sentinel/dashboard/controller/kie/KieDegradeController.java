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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKieProvider;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKiePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
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
 * Degrade rule controller.
 *
 * @author Sherlockhan
 */
@RestController
@RequestMapping("/kie/degrade")
public class KieDegradeController {

    private final Logger logger = LoggerFactory.getLogger(KieDegradeController.class);

    @Autowired
    @Qualifier("degradeRuleKieProvider")
    private RuleKieProvider<List<DegradeRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("degradeRuleKiePublisher")
    private RuleKiePublisher<List<DegradeRuleEntity>> rulePublisher;

    @GetMapping("/rules")
    public Result<List<DegradeRuleEntity>> apiQueryRules(@RequestParam(name = "serverId") String serverId) {
        if (StringUtil.isEmpty(serverId)) {
            return Result.ofFail(-1, "Id can't be null or empty");
        }

        try {
            List<DegradeRuleEntity> rules = ruleProvider.getRules(serverId);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying degrade rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/{serverId}/rule")
    public Result<DegradeRuleEntity> apiAddRule(@PathVariable("serverId") String serverId,
                                                @RequestBody DegradeRuleEntity entity) {
        Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        try{
            rulePublisher.add(serverId, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            logger.error("Error when add degrade rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/{serverId}/rule")
    public Result<DegradeRuleEntity> apiUpdateRule(@PathVariable("serverId") String serverId,
                                                   @RequestBody DegradeRuleEntity entity) {
        Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            rulePublisher.update(serverId, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when update degrade rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @DeleteMapping("/{serverId}/rule/{ruleId}")
    public Result<String> delete(@PathVariable("serverId") String serverId,
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

    private <R> Result<R> checkEntityInternal(DegradeRuleEntity entity) {
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be blank");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        Double threshold = entity.getCount();
        if (threshold == null || threshold < 0) {
            return Result.ofFail(-1, "invalid threshold: " + threshold);
        }
        Integer recoveryTimeoutSec = entity.getTimeWindow();
        if (recoveryTimeoutSec == null || recoveryTimeoutSec <= 0) {
            return Result.ofFail(-1, "recoveryTimeout should be positive");
        }
        Integer strategy = entity.getGrade();
        if (strategy == null) {
            return Result.ofFail(-1, "circuit breaker strategy cannot be null");
        }
        if (strategy < CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType()
            || strategy > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
            return Result.ofFail(-1, "Invalid circuit breaker strategy: " + strategy);
        }
        if (entity.getMinRequestAmount()  == null || entity.getMinRequestAmount() <= 0) {
            return Result.ofFail(-1, "Invalid minRequestAmount");
        }
        if (entity.getStatIntervalMs() == null || entity.getStatIntervalMs() <= 0) {
            return Result.ofFail(-1, "Invalid statInterval");
        }
        if (strategy == RuleConstant.DEGRADE_GRADE_RT) {
            Double slowRatio = entity.getSlowRatioThreshold();
            if (slowRatio == null) {
                return Result.ofFail(-1, "SlowRatioThreshold is required for slow request ratio strategy");
            } else if (slowRatio < 0 || slowRatio > 1) {
                return Result.ofFail(-1, "SlowRatioThreshold should be in range: [0.0, 1.0]");
            }
        } else if (strategy == RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) {
            if (threshold > 1) {
                return Result.ofFail(-1, "Ratio threshold should be in range: [0.0, 1.0]");
            }
        }

        return null;
    }
}
