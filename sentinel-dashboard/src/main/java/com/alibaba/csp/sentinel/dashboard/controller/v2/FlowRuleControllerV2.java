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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.AuthUser;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.ResponseCode;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Flow rule controller (v2).
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/v2/flow")
public class FlowRuleControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(FlowRuleControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;

    @Autowired
    @Qualifier("flowRuleZookeeperProvider")
    private DynamicRuleProvider<List<FlowRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("flowRuleZookeeperPublisher")
    private DynamicRulePublisher<List<FlowRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @GetMapping("/rules")
    public Result<List<FlowRuleEntity>> apiQueryMachineRules(HttpServletRequest request, @RequestParam String app) {

        try {
            if (StringUtil.isBlank(app)) {
                return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
            }

            AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(app, PrivilegeType.READ_RULE);
            }

            List<FlowRuleEntity> rules = ruleProvider.getRules(app);
            if (rules != null && !rules.isEmpty()) {
                for (FlowRuleEntity entity : rules) {
                    entity.setApp(app);
                    if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                        entity.setId(entity.getClusterConfig().getFlowId());
                    }
                }
            }
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }
    }


    @PostMapping("/rule")
    public Result<FlowRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody FlowRuleEntity entity) {

        try {
            Result<FlowRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(entity.getApp(), PrivilegeType.WRITE_RULE);
            }

            Date date = new Date();
            entity.setGmtCreate(date)
                    .setGmtModified(date)
                    .setLimitApp(entity.getLimitApp().trim())
                    .setResource(entity.getResource().trim())
                    .setId(null);


            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to add flow rule", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    public Result<FlowRuleEntity> apiUpdateRule(HttpServletRequest request,
                                                @PathVariable("id") Long id, @RequestBody FlowRuleEntity entity) {

        try {
            if (id == null || id <= 0) {
                return Result.ofFail(ResponseCode.fail, "Invalid id");
            }
            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "invalid body");
            }
            FlowRuleEntity oldEntity = repository.findById(id);
            if (oldEntity == null) {
                return Result.ofFail(ResponseCode.fail, "id " + id + " does not exist");
            }

            AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(oldEntity.getApp(), PrivilegeType.WRITE_RULE);
            }

            entity.setApp(oldEntity.getApp()).setIp(oldEntity.getIp()).setPort(oldEntity.getPort());
            Result<FlowRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            entity.setGmtCreate(oldEntity.getGmtCreate()).setGmtModified(new Date()).setId(id);
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "save entity fail");
            }
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to update flow rule", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return Result.ofFail(ResponseCode.fail, "Invalid id");
        }
        FlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        AuthUser authUser = authService.getAuthUser(request);
        if (authUser != null) {
            authUser.authTarget(oldEntity.getApp(), PrivilegeType.DELETE_RULE);
        }
        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Exception e) {
            return Result.ofFail(ResponseCode.fail, e.getMessage());
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<FlowRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }

    private <R> Result<R> checkEntity(FlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(ResponseCode.fail, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(ResponseCode.fail, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(ResponseCode.fail, "resource can't be null or empty");
        }
        if (entity.getGrade() == null) {
            return Result.ofFail(ResponseCode.fail, "grade can't be null");
        }
        if (entity.getGrade() != 0 && entity.getGrade() != 1) {
            return Result.ofFail(ResponseCode.fail, "grade must be 0 or 1, but " + entity.getGrade() + " got");
        }
        if (entity.getCount() == null || entity.getCount() < 0) {
            return Result.ofFail(ResponseCode.fail, "count should be at lease zero");
        }
        if (entity.getStrategy() == null) {
            return Result.ofFail(ResponseCode.fail, "strategy can't be null");
        }
        if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            return Result.ofFail(ResponseCode.fail, "refResource can't be null or empty when strategy!=0");
        }
        if (entity.getControlBehavior() == null) {
            return Result.ofFail(ResponseCode.fail, "controlBehavior can't be null");
        }
        int controlBehavior = entity.getControlBehavior();
        if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
            return Result.ofFail(ResponseCode.fail, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(ResponseCode.fail, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (entity.isClusterMode() && entity.getClusterConfig() == null) {
            return Result.ofFail(ResponseCode.fail, "cluster config should be valid");
        }
        return null;
    }

}
