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
import com.alibaba.csp.sentinel.dashboard.client.CommandNotFoundException;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.SentinelVersion;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.ResponseCode;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.util.VersionUtils;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RestController
@RequestMapping("/v2/paramFlow")
public class ParamFlowRuleControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(ParamFlowRuleControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<ParamFlowRuleEntity> repository;

    @Autowired
    @Qualifier("paramFlowRuleZookeeperProvider")
    private DynamicRuleProvider<List<ParamFlowRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("paramFlowRuleZookeeperPublisher")
    private DynamicRulePublisher<List<ParamFlowRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Autowired
    private AppManagement appManagement;

    private final SentinelVersion version020 = new SentinelVersion().setMinorVersion(2);


    @GetMapping("/rules")
    public Result<List<ParamFlowRuleEntity>> apiQueryMachineRules(HttpServletRequest request, String app) {

        try {

            if (StringUtil.isBlank(app)) {
                return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
            }

            if (!checkIfSupported(app)) {
                return unsupportedVersion();
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);
            }

            List<ParamFlowRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (ExecutionException ex) {
            logger.error("Error when querying parameter flow rules", ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return Result.ofThrowable(ResponseCode.fail, ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when querying parameter flow rules", throwable);
            return Result.ofFail(ResponseCode.fail, throwable.getMessage());
        }
    }

    @PostMapping("/rule")
    public Result<ParamFlowRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody ParamFlowRuleEntity entity) {

        try {
            Result<ParamFlowRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            Date date = new Date();
            entity.getRule().setResource(entity.getResource().trim());
            entity.setGmtCreate(date).setGmtModified(date).setId(null);

            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (ExecutionException ex) {
            logger.error("Error when adding new parameter flow rules", ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return Result.ofThrowable(ResponseCode.fail, ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when adding new parameter flow rules", throwable);
            return Result.ofFail(ResponseCode.fail, throwable.getMessage());
        }

        return Result.ofSuccess(entity);

    }

    @PutMapping("/rule/{id}")
    public Result<ParamFlowRuleEntity> apiUpdateRule(HttpServletRequest request,
                                                     @PathVariable("id") Long id, @RequestBody ParamFlowRuleEntity entity) {

        try {
            if (id == null || id <= 0) {
                return Result.ofFail(ResponseCode.fail, "Invalid id");
            }
            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "invalid body");
            }
            ParamFlowRuleEntity oldEntity = repository.findById(id);
            if (oldEntity == null) {
                return Result.ofFail(ResponseCode.fail, "id " + id + " does not exist");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            if (authUser != null) {
                authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
            }

            entity.setApp(oldEntity.getApp());
            Result<ParamFlowRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }
            entity.getRule().setResource(entity.getResource().trim());
            entity.setGmtCreate(oldEntity.getGmtCreate()).setGmtModified(new Date()).setId(id);
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(ResponseCode.fail, "save entity fail");
            }
            publishRules(oldEntity.getApp());
        } catch (ExecutionException ex) {
            logger.error("Error when updating parameter flow rules, id=" + id, ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return Result.ofThrowable(ResponseCode.fail, ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when updating parameter flow rules, id=" + id, throwable);
            return Result.ofFail(ResponseCode.fail, throwable.getMessage());
        }
        return Result.ofSuccess(entity);
    }


    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            return Result.ofFail(ResponseCode.fail, "Invalid id");
        }
        ParamFlowRuleEntity oldEntity = repository.findById(id);
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
        } catch (Exception e) {
            return Result.ofFail(ResponseCode.fail, e.getMessage());
        }
        return Result.ofSuccess(id);
    }


    private boolean checkIfSupported(String app) {
        try {
            return Optional.ofNullable(appManagement.getDetailApp(app))
                    .flatMap(e -> e.getMachines().stream().filter(MachineInfo::isHealthy).findFirst())
                    .flatMap(m -> VersionUtils.parseVersion(m.getVersion())
                            .map(v -> v.greaterOrEqual(version020)))
                    .orElse(true);
            // If error occurred or cannot retrieve machine info, return true.
        } catch (Exception ex) {
            return true;
        }
    }

    private boolean isNotSupported(Throwable ex) {
        return ex instanceof CommandNotFoundException;
    }

    private <R> Result<R> unsupportedVersion() {
        return Result.ofFail(ResponseCode.un_supported_version,
                "Sentinel client not supported for parameter flow control (unsupported version or dependency absent)");
    }

    private void publishRules(@NonNull String app) throws Exception {
        List<ParamFlowRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }

    private <R> Result<R> checkEntity(ParamFlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(ResponseCode.fail, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
        }

        checkIfSupported(entity.getApp());

        if (entity.getRule() == null) {
            return Result.ofFail(ResponseCode.fail, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(ResponseCode.fail, "resource name cannot be null or empty");
        }
        if (entity.getCount() < 0) {
            return Result.ofFail(ResponseCode.fail, "count should be valid");
        }
        if (entity.getGrade() != RuleConstant.FLOW_GRADE_QPS) {
            return Result.ofFail(ResponseCode.fail, "Unknown mode (blockGrade) for parameter flow control");
        }
        if (entity.getParamIdx() == null || entity.getParamIdx() < 0) {
            return Result.ofFail(ResponseCode.fail, "paramIdx should be valid");
        }
        if (entity.getDurationInSec() <= 0) {
            return Result.ofFail(ResponseCode.fail, "durationInSec should be valid");
        }
        if (entity.getControlBehavior() < 0) {
            return Result.ofFail(ResponseCode.fail, "controlBehavior should be valid");
        }
        return null;
    }


}
