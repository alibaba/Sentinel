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
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RestController
@RequestMapping("/v2/system")
public class SystemRuleControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(SystemRuleControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<SystemRuleEntity> repository;

    @Autowired
    @Qualifier("systemRuleZookeeperProvider")
    private DynamicRuleProvider<List<SystemRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("systemRuleZookeeperPublisher")
    private DynamicRulePublisher<List<SystemRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;


    @GetMapping("/rules")
    public Result<List<SystemRuleEntity>> apiQueryMachineRules(HttpServletRequest request, String app) {

        try {

            if (StringUtil.isBlank(app)) {
                return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);
            }

            List<SystemRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("query system rules error:", throwable);
            return Result.ofThrowable(throwable);
        }
    }

    @PostMapping("/rule")
    public Result<SystemRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody SystemRuleEntity entity) {

        try {
            Result<SystemRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            Date date = new Date();
            entity.setGmtCreate(date).setGmtModified(date).setId(null);

            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("add system rule error:", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }

        return Result.ofSuccess(entity);

    }


    @PutMapping("/rule/{id}")
    public Result<SystemRuleEntity> apiUpdateRule(HttpServletRequest request,
                                                  @PathVariable("id") Long id, @RequestBody SystemRuleEntity entity) {

        try {
            if (id == null || id <= 0) {
                return Result.ofFail(ResponseCode.fail, "Invalid id");
            }
            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "invalid body");
            }
            SystemRuleEntity oldEntity = repository.findById(id);
            if (oldEntity == null) {
                return Result.ofFail(ResponseCode.fail, "id " + id + " does not exist");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            entity.setApp(oldEntity.getApp()).setIp(oldEntity.getIp()).setPort(oldEntity.getPort());
            Result<SystemRuleEntity> checkResult = checkEntity(entity);
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
            logger.error("Failed to update system rule", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }
        return Result.ofSuccess(entity);
    }


    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            return Result.ofFail(ResponseCode.fail, "Invalid id");
        }
        SystemRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (authUser != null) {
            authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.DELETE_RULE);
        }

        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to delete system rule", throwable);
            return Result.ofFail(ResponseCode.fail, throwable.getMessage());
        }
        return Result.ofSuccess(id);
    }


    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<SystemRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }

    private <R> Result<R> checkEntity(SystemRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(ResponseCode.fail, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
        }
        int notNullCount = countNotNullAndNotNegative(entity.getAvgLoad(), entity.getAvgRt(), entity.getMaxThread(), entity.getQps());
        if (notNullCount != 1) {
            return Result.ofFail(ResponseCode.fail, "only one of [avgLoad, avgRt, maxThread, qps] "
                    + "value must be set >= 0, but " + notNullCount + " values get");
        }

        return null;
    }


    private int countNotNullAndNotNegative(Number... values) {
        int notNullCount = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && values[i].doubleValue() >= 0) {
                notNullCount++;
            }
        }
        return notNullCount;
    }

}
