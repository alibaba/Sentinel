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
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
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
@RequestMapping(value = "v2/degrade")
public class DegradeRuleControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(DegradeRuleControllerV2.class);

    @Autowired
    @Qualifier("degradeRuleDefaultProvider")
    private DynamicRuleProvider<List<DegradeRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("degradeRuleDefaultPublisher")
    private DynamicRulePublisher<List<DegradeRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Autowired
    private InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository;


    @GetMapping("/rules")
    public Result<List<DegradeRuleEntity>> apiQueryMachineRules(HttpServletRequest request, String app) {
        try {

            if (StringUtil.isBlank(app)) {
                return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);
            }

            List<DegradeRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("query degrade rules error:", throwable);
            return Result.ofThrowable(throwable);
        }
    }


    @PostMapping("/rule")
    public Result<DegradeRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody DegradeRuleEntity entity) {

        DegradeRuleEntity resultEntity = null;
        try {
            Result<DegradeRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            Date date = new Date();
            entity.setGmtCreate(date)
                    .setGmtModified(date)
                    .setResource(entity.getResource().trim())
                    .setLimitApp(entity.getLimitApp().trim())
                    .setApp(entity.getApp().trim())
                    .setId(null);

            resultEntity = repository.save(entity);
            publishRules(resultEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("add degrade rule error:", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }

        return Result.ofSuccess(resultEntity);

    }


    @PutMapping("/rule/{id}")
    public Result<DegradeRuleEntity> apiUpdateRule(HttpServletRequest request,
                                                   @PathVariable("id") Long id, @RequestBody DegradeRuleEntity entity) {
        DegradeRuleEntity resultEntity = null;
        try {
            if (id == null || id < 0) {
                return Result.ofFail(ResponseCode.fail, "id is invalid");
            }

            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "invalid body");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            DegradeRuleEntity oldEntity = repository.findById(id);
            if (oldEntity != null) {
                return Result.ofFail(ResponseCode.fail, "id " + id + " does not exist");
            }

            entity.setApp(oldEntity.getApp());
            Result<DegradeRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            entity.setGmtCreate(oldEntity.getGmtCreate()).setGmtModified(new Date()).setId(id);
            resultEntity = repository.save(entity);
            if (resultEntity == null) {
                return Result.ofFail(ResponseCode.fail, "save entity fail");
            }
            publishRules(resultEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("update  degrade rule  error:", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }

        return Result.ofSuccess(resultEntity);
    }


    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            return Result.ofFail(ResponseCode.fail, "Invalid id");
        }
        DegradeRuleEntity oldEntity = repository.findById(id);
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
            logger.error("delete  degrade rule  error:", throwable);
            return Result.ofFail(ResponseCode.fail, throwable.getMessage());
        }
        return Result.ofSuccess(id);
    }


    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<DegradeRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }


    private <R> Result<R> checkEntity(DegradeRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(ResponseCode.fail, "entity is invalid");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(ResponseCode.fail, "resource can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(ResponseCode.fail, "resource can't be null or empty");
        }
        if (entity.getCount() == null || entity.getCount() < 0) {
            return Result.ofFail(ResponseCode.fail, "count can't be null or less than zero");
        }
        if (entity.getTimeWindow() == null || entity.getTimeWindow() < 0) {
            return Result.ofFail(ResponseCode.fail, "timeWindow can't be null or less than zero");
        }
        if (entity.getGrade() == null || entity.getGrade() < 0) {
            return Result.ofFail(ResponseCode.fail, "grade can't be null or less than zero");
        }
        return null;
    }


}
