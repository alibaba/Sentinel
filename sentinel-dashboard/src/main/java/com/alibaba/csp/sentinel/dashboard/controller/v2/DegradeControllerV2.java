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
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/v2/degrade")
public class DegradeControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(DegradeControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository;

    @Autowired
    @Qualifier("degradeRuleCustomProvider")
    private DynamicRuleProvider<List<DegradeRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("degradeRuleCustomPublisher")
    private DynamicRulePublisher<List<DegradeRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @GetMapping("/rules")
    public Result<List<DegradeRuleEntity>> apiQueryMachineRules(HttpServletRequest request, @RequestParam String app) {
        AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, PrivilegeType.READ_RULE);

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            List<DegradeRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying degrade rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private <R> Result<R> checkEntityInternal(DegradeRuleEntity entity) {
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
        return null;
    }

    @PostMapping("/rule")
    public Result<DegradeRuleEntity> apiAddFlowRule(HttpServletRequest request, @RequestBody DegradeRuleEntity entity) {
        AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(entity.getApp(), PrivilegeType.WRITE_RULE);

        Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to add degrade rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    public Result<DegradeRuleEntity> apiUpdateFlowRule(HttpServletRequest request,
                                                       @PathVariable("id") Long id,
                                                       @RequestBody DegradeRuleEntity entity) {
        AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        DegradeRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofFail(-1, "id " + id + " does not exist");
        }
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        authUser.authTarget(oldEntity.getApp(), PrivilegeType.WRITE_RULE);

        entity.setApp(oldEntity.getApp());
        entity.setIp(oldEntity.getIp());
        entity.setPort(oldEntity.getPort());
        Result<DegradeRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }

        entity.setId(id);
        Date date = new Date();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to update degrade rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {
        AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        DegradeRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        authUser.authTarget(oldEntity.getApp(), PrivilegeType.DELETE_RULE);
        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<DegradeRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }
}
